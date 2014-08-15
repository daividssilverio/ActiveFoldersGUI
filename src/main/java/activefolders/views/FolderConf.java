package activefolders.views;

import activefolders.Folder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by daividsilverio on 2014-08-11.
 */
public class FolderConf extends GridPane {
    @FXML
    private TextField url_text;

    @FXML
    private Button url_check;

    @FXML
    private TextField path_text;

    @FXML
    private Button path_search;

    @FXML
    private TextField keyfile_text;

    @FXML
    private Button keyfile_search;

    @FXML
    private ComboBox destination_text;

    @FXML
    private TextField username_text;

    private Stage stage;

    ArrayList<Node> nodes;

    private Function<String, List> urlChecker;
    private Consumer<Boolean> urlChecked;

    public FolderConf() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/folder_conf.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.setClassLoader(getClass().getClassLoader());

        try {
            loader.load();
            nodes = new ArrayList<Node>(6);

            nodes.add(path_text);
            nodes.add(path_search);
            nodes.add(keyfile_search);
            nodes.add(keyfile_text);
            nodes.add(destination_text);
            nodes.add(username_text);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUrlChecker(Function<String, List> check, Consumer<Boolean> checked) {
        urlChecker = check;
        urlChecked = checked;
    }

    @FXML
    private void urlCheck() {
        List list = urlChecker.apply(url_text.getText());
        ObservableList<String> items = FXCollections.observableArrayList();
        if (list.size() > 0) {
            items.addAll(list);
            destination_text.setItems(items);
            nodes.forEach(n -> n.setDisable(false));
            urlChecked.accept(true);
        } else {
            urlChecked.accept(false);
            System.out.println("invalid url or no available destinations");
        }
    }

    protected boolean checkFields() {
        if (url_text.getText().isEmpty())
            return false;

        if (path_text.getText().isEmpty() ||
                !Files.exists(Paths.get(path_text.getText())))
            return false;

        if (destination_text.getValue() == null ||
                destination_text.toString().isEmpty())
            return false;

        if (keyfile_text.getText().isEmpty() ||
                !Files.exists(Paths.get(keyfile_text.getText())))
            return false;

        if (username_text.getText().isEmpty())
            return false;

        // else
        return true;
    }

    @FXML
    private void pathSearch() {
        File file = getDirectory();

        if (file != null) {
            path_text.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void keyFileSearch() {
        File file = getFile();

        if (file != null) {
            keyfile_text.setText(file.getAbsolutePath());
        }
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    private File getFile() {
        FileChooser fileChooser = new FileChooser();
        return fileChooser.showOpenDialog(stage);
    }

    private File getDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        return directoryChooser.showDialog(stage);
    }

    public Folder getFolder() {
        Folder newFolder = new Folder();

        try {
            newFolder.setUrl(url_text.getText());
            newFolder.setPath(path_text.getText());
            newFolder.setDestination(destination_text.getValue().toString());
            newFolder.setKeyFile(keyfile_text.getText());
            newFolder.setUsername(username_text.getText());
        } catch (NullPointerException e) {
            return null;
        }
        return newFolder;
    }

    public void setFolder(Folder folder) {
        url_text.setText(folder.getUrl());
        path_text.setText(folder.getPath());
        ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.add(folder.getDestination());
        destination_text.setItems(observableList);
        keyfile_text.setText(folder.getKeyFile());
        username_text.setText(folder.getUsername());
    }
}
