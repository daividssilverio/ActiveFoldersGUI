package activefolders.views;

import activefolders.Folder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Created by daividsilverio on 2014-08-06.
 */
public class FolderForm extends AnchorPane {
    @FXML
    private Button add;

    @FXML
    private Button cancel;

    @FXML
    FolderConf folderConf;

    private Stage stage;

    private Function<String, List> urlChecker;

    public FolderForm() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/folder_form.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.setClassLoader(getClass().getClassLoader());

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUrlChecker(Function<String, List> f) {
        urlChecker = f;
        folderConf.setUrlChecker(f, this::urlChecked);
    }

    private void urlChecked(Boolean urlIsokay) {
        add.setDisable(!urlIsokay);
    }

    private boolean checkFields() {
        return folderConf.checkFields();
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
        folderConf.setStage(stage);
    }

    public Folder getFolder() {
        return folderConf.getFolder();
    }

    @FXML
    private void returnFolder() {
        if (checkFields())
            stage.close();
        else
            System.out.println("something is wrong");
    }

    protected void setFolder(Folder folder) {
        folderConf.setFolder(folder);
    }

    @FXML
    private void cancel() {
        stage.close();
    }

}
