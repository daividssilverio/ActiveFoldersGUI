package activefolders;

import activefolders.model.FolderModel;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static activefolders.Controller.GetFolderIndex;

/**
 * Created by daividsilverio on 2014-08-08.
 */
public class AFTool {
    public static List<String> GetDestinations(String url) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            final HttpResponse<JsonNode> jsonResponse = Unirest.get(url + "/destinations").asJson();
            list.addAll(jsonResponse.getBody().getObject().keySet());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String CreateNewFolder(String url) {
        String uuid = null;
        try {
            final HttpResponse<String> jsonResponse = Unirest.post(url + "/create_folder").asString();
            if (jsonResponse.getCode() == 201) {
                uuid = jsonResponse.getBody();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return uuid;
    }

    public static Monitor StartSyncThread(Folder folder) {
        return new Monitor(folder);
    }
    
    public static void Upload(Folder folder) {
        PathUploader pathUploader = new PathUploader(folder);
        
        try {
            Files.walkFileTree(Paths.get(folder.getPath()), pathUploader);
            Folder oldFolder = new Folder(folder);
            folder.setLastSync(Instant.now().toString());
            FolderModel.UpdateFolder(oldFolder, folder, GetFolderIndex());
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    
    public static class PathUploader extends SimpleFileVisitor<Path> {
        private Folder folder;

        protected PathUploader(Folder folder) {
            super();
            this.folder = folder;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!file.toFile().getName().equals(".activefolders"))
                try {
                    upload(file);
                } catch (UnirestException e) {
                    e.printStackTrace();
                }

            return FileVisitResult.CONTINUE;
        }

        protected void upload(Path file) throws UnirestException, IOException {
            Unirest.put(getDestinationUrl(file))
                    .body(new String(Files.readAllBytes(file)))
                    .asBinary();
        }

        protected String getDestinationUrl(Path file) {
            return folder.getFolderFilesUploadString() +
                    file.toString().split(folder.getFolderPrefixName())[1];
        }
    }

    public static class PathSynchronizer extends PathUploader {
        private FileTime lastFolderSync;
        private Instant lastSync = null;

        public Instant getLastSync() {
            return lastSync;
        }

        protected PathSynchronizer(Folder folder) {
            super(folder);
            lastFolderSync = FileTime.from(Instant.parse(folder.getLastSync()));
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!file.toFile().getName().equals(".activefolders"))
                if (attrs.lastModifiedTime().compareTo(lastFolderSync) > 0) {
                    try {
                        upload(file);
                        lastSync = Instant.now();
                    } catch (UnirestException e) {
                        e.printStackTrace();
                    }
                }
            return FileVisitResult.CONTINUE;
        }
    }

    public static class Monitor implements Runnable {
        private volatile boolean canceled = false;
        private Folder folder;

        public Monitor(Folder folder) {
            this.folder = folder;
        }

        @Override
        public void run() {

            if (folder.getUuid() == null) {
                String uuid = CreateNewFolder(folder.getUrl());
                if (uuid != null) {
                    Folder oldFolder = new Folder(folder);
                    folder.setUuid(uuid);
                    boolean result = FolderModel.UpdateFolder(oldFolder, folder, GetFolderIndex());
                    if (!result) return;
                    Upload(folder);
                }
            }


            Random rd = new Random();

            while (!canceled) {
                folder.reload();

                try {
                    PathSynchronizer ps = new PathSynchronizer(folder);
                    Files.walkFileTree(Paths.get(folder.getPath()), ps);
                    Instant lastSync = ps.getLastSync();
                    if (lastSync != null) {
                        Folder oldFolder = new Folder(folder);
                        folder.setLastSync(lastSync.toString());
                        FolderModel.UpdateFolder(oldFolder, folder, GetFolderIndex());
                    }
                    //sleeps 5~10 seconds
                    Thread.sleep(5000 + rd.nextInt(5000));

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }
}
