package activefolders;

import activefolders.model.FolderModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by daividsilverio on 2014-08-05.
 */
public class Folder {
    private String url;
    private String path;
    private String destination;
    private String username;
    private String keyFile;
    private String uuid;
    private String lastSync;

    public Folder(Folder folder) {
        load(folder);
    }

    @JsonIgnore
    private void load(Folder folder) {
        this.url = folder.getUrl();
        this.path = folder.getPath();
        this.destination = folder.getDestination();
        this.username = folder.getUsername();
        this.keyFile = folder.getKeyFile();
        this.uuid = folder.getUuid();
    }

    public Folder() {
    }

    @JsonIgnore
    public void reload() {
        Folder folder = FolderModel.ReloadFolder(this);
        this.load(folder);
    }

    @JsonIgnore
    public String getConfFileString() {
        return path + File.separator + ".activefolders";
    }

    @JsonIgnore
    public String getFolderUrlString() {
        return url + "/folders/" + uuid;
    }

    @JsonIgnore
    public String getFolderFilesUploadString() {
        return getFolderUrlString() + "/files";
    }

    @JsonIgnore
    public String getFolderPrefixName() {
        return Paths.get(path).getParent().toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }
}
