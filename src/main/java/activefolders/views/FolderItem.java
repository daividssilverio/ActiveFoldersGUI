package activefolders.views;

import activefolders.Folder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by daividsilverio on 2014-08-06.
 */
public class FolderItem extends SplitPane {
    @FXML
    private Text folder_text;

    @FXML
    private Text url_text;

    @FXML
    private Button conf;

    private Folder folder;

    private Function<Folder, Void> folderDetailOpener;

    public FolderItem() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/folder_item.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFolder(Folder folder) {
        this.folder = folder;

        url_text.setText(folder.getUrl());
        folder_text.setText(folder.getPath());
    }

    public void setFolderDetailOpener(Function<Folder, Void> folderDetailOpener) {
        this.folderDetailOpener = folderDetailOpener;
    }

    @FXML
    private void openDetails() {
        folderDetailOpener.apply(this.folder);
    }

}
