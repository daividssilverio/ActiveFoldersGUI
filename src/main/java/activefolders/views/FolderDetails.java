package activefolders.views;

import activefolders.Folder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by daividsilverio on 2014-08-11.
 */
public class FolderDetails extends AnchorPane {
    @FXML
    private FolderConf folderConf;

    @FXML
    private Button saveFolder;

    private Folder folder;
    private BiConsumer<Folder, Folder> updateFolder;
    private Consumer<Folder> syncFolder;
    private Consumer<Folder> deleteFolder;
    private Stage stage;

    public FolderDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/folder_details.fxml"));
        loader.setClassLoader(getClass().getClassLoader());
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUrlChecker(Function<String, List> urlChecker) {
        this.folderConf.setUrlChecker(urlChecker, this::urlChecked);
    }

    public void urlChecked(Boolean urlIsOkay) {
        saveFolder.setDisable(!urlIsOkay);
    }

    public void setFolder(Folder folder) {
        this.folder = new Folder(folder);
        this.folderConf.setFolder(folder);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.folderConf.setStage(stage);
    }

    @FXML
    private void saveFolder() {
        if (folderConf.checkFields()) {
            updateFolder.accept(this.folder, folderConf.getFolder());
            stage.close();
        }
    }

    @FXML
    private void forceSync() {
        syncFolder.accept(this.folder);
    }

    @FXML
    private void deleteFolder() {
        deleteFolder.accept(this.folder);
        stage.close();
    }

    @FXML
    private void stopSync() {
        //stop sync
    }

    public void setUpdateFolder(BiConsumer<Folder, Folder> updateFolder) {
        this.updateFolder = updateFolder;
    }

    public void setSyncFolder(Consumer<Folder> syncFolder) {
        this.syncFolder = syncFolder;
    }

    public void setDeleteFolder(Consumer<Folder> deleteFolder) {
        this.deleteFolder = deleteFolder;
    }
}
