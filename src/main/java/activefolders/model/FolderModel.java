package activefolders.model;

import activefolders.Folder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by daividsilverio on 2014-08-12.
 */
public class FolderModel {
    /**
     * @param path try to load folders from the paths indicated in the
     *             path provided, if the file is non existent, will create the
     *             file and write an empty json array on it.
     * @return a list of folders found in the path provided
     */
    public static ArrayList<Folder> LoadFolders(Path path) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writerWithDefaultPrettyPrinter();
        ArrayList<Folder> folders = new ArrayList<>();

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                ArrayList<String> a = new ArrayList<>(0);
                mapper.writeValue(path.toFile(), a);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ArrayList<String> files = getStringPaths(path, mapper);
            for (String file : files) {
                Path json = Paths.get(file + File.separator + ".activefolders");
                if (Files.exists(json)) {
                    folders.add(mapper.readValue(json.toFile(), Folder.class));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return folders;
    }

    /**
     * @param folder       folder to be added
     * @param foldersIndex path to the index of folders, usually others.json in $AF_HOME
     * @return a boolean indicating if the insertion of the folder was successful
     * <p>
     * Will try to add a folder in the folderIndex (other.json in $AF_HOME) based on
     * the path contained in the Folder provided
     */
    public static boolean AddFolder(Folder folder, Path foldersIndex) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writerWithDefaultPrettyPrinter();

        try {
            ArrayList<String> folders = getStringPaths(foldersIndex, mapper);

            if (!folders.contains(folder.getPath())) {
                folders.add(folder.getPath());
                folder.setLastSync(Instant.MIN.toString());
                System.out.println(folder.getLastSync());
                mapper.writeValue(new File(folder.getConfFileString()), folder);
                mapper.writeValue(foldersIndex.toFile(), folders);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param oldFolder    folder before update
     * @param newFolder    folder after update
     * @param foldersIndex path to the index of folders, usually others.json in $AF_HOME
     * @return true if successfully updated the folder, false if something happened.
     * will return false if trying to update the folder path to someplace where there is
     * an folder configured (.activefolders file present in the new path)
     */
    public static boolean UpdateFolder(Folder oldFolder, Folder newFolder, Path foldersIndex) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writerWithDefaultPrettyPrinter();

        try {
            //todo: fix, update folder should not replace existing folder

            // the update didn't change the path
            if (oldFolder.getPath().equals(newFolder.getPath())) {
                mapper.writeValue(new File(newFolder.getConfFileString()), newFolder);
                return true;
            }

            // the update changed the path
            ArrayList<String> folders = getStringPaths(foldersIndex, mapper);

            // the new folder can't overwrite existing folder
            if (folders.contains(newFolder.getPath()))
                return false;

            folders.removeIf(string -> string.equals(oldFolder.getPath()));

            folders.add(newFolder.getPath());
            mapper.writeValue(foldersIndex.toFile(), folders);
            mapper.writeValue(new File(newFolder.getPath() + File.separator + ".activefolders"), newFolder);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static ArrayList<String> getStringPaths(Path foldersIndex, ObjectMapper mapper) throws IOException {
        return mapper.readValue(foldersIndex.toFile(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class));
    }

    public static Folder ReloadFolder(Folder folder) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            folder = mapper.readValue(Paths.get(folder.getConfFileString()).toFile(), Folder.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return folder;
    }

    public static boolean RemoveFolder(Folder folder, Path foldersIndex) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            ArrayList<String> folders = getStringPaths(foldersIndex, mapper);
            folders.removeIf(string -> string.equals(folder.getPath()));
            mapper.writeValue(foldersIndex.toFile(), folders);
            Files.delete(Paths.get(folder.getConfFileString()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
