package activefolders;

import activefolders.model.FolderModel;
import activefolders.views.FolderCell;
import activefolders.views.FolderDetails;
import activefolders.views.FolderForm;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public static final String FOLDERS_INDEX = "other.json";
    protected static String afHome;
    private static String AF_HOME = "AF_HOME";

    @FXML
    private Parent root;

    @FXML
    private ListView<Folder> folderList;

    private ObservableList<Folder> observableList;
    private HashMap<String, AFTool.Monitor> threads;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        threads = new HashMap<>();
        checkConfig();
        checkFolders();
    }

    private void checkFolders() {
        observableList = FXCollections.observableArrayList();
        Path othersPath = GetFolderIndex();
        ArrayList<Folder> folders = FolderModel.LoadFolders(othersPath);
        observableList.setAll(folders);

        folders.forEach(folder -> {
            if (!threads.containsKey(folder.getPath())) {
                AFTool.Monitor monitor = new AFTool.Monitor(folder);
                (new Thread(monitor)).start();
                threads.put(folder.getPath(), monitor);
            }
        });

        folderList.setItems(observableList);
        folderList.setCellFactory(param -> new FolderCell(this::openFolderDetails));
    }

    protected static Path GetFolderIndex() {
        return Paths.get(afHome + File.separator + FOLDERS_INDEX);
    }

    public void addFolder(Folder folder) {
        Path othersPath = GetFolderIndex();

        if (FolderModel.AddFolder(folder, othersPath)) {
            observableList.add(folder);
            AFTool.Monitor monitor = new AFTool.Monitor(folder);
            (new Thread(monitor)).start();
            threads.put(folder.getPath(), monitor);
        } else {
            // todo: alert adding folder failed
        }
    }

    public void updateFolder(Folder oldFolder, Folder newFolder) {
        Path othersPath = GetFolderIndex();

        if (FolderModel.UpdateFolder(oldFolder, newFolder, othersPath)) {
            if (!oldFolder.getPath().equals(newFolder.getPath())) {
                AFTool.Monitor monitor = threads.remove(oldFolder.getPath());
                if (monitor != null)
                    monitor.setCanceled(true);
            }

            observableList.clear();
            checkFolders();
        } else {
            // todo: alert updating folder failed
        }
    }

    public void checkConfig() {
        /**
         * Check if the AF_HOME environment variable is set.
         * if $AF_HOME is set and $AF_HOME is a valid path, set afHome to it
         * if $AF_HOME is not set, set afHome to the default ~/ActiveFolders
         */
        String envHomePathString = System.getenv(AF_HOME);

        try {
            Path path;
            if (envHomePathString != null) {
                path = Paths.get(System.getenv(envHomePathString));
            } else {
                path = Paths.get(System.getProperty("user.home") + File.separator + "ActiveFolders");
            }
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            this.afHome = path.toString();
        } catch (IOException | InvalidPathException e) {
            e.printStackTrace();
        }
    }

    public void createTrayIcon(final Stage stage) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = null;

            try {
                InputStream file = getClass().getClassLoader().getResourceAsStream("images/aficon.png");
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setOnCloseRequest(action -> hide(stage));

            final ActionListener closeListener = action -> exit();

            final ActionListener openAdvancedViewListener = action -> show(stage);

            final ActionListener addFolderListener = action -> openFolderForm();

            PopupMenu popupMenu = new PopupMenu();

            MenuItem closeItem = new MenuItem("Exit");
            closeItem.addActionListener(closeListener);
            popupMenu.add(closeItem);

            MenuItem addFolderItem = new MenuItem("Add Folder");
            addFolderItem.addActionListener(addFolderListener);
            popupMenu.add(addFolderItem);

            MenuItem advancedViewItem = new MenuItem("Open Advanced View");
            advancedViewItem.addActionListener(openAdvancedViewListener);
            popupMenu.add(advancedViewItem);

            TrayIcon trayIcon = new TrayIcon(image, "ActiveFolders", popupMenu);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    /* Left Button clicked */
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        //TODO: open history
                    }
                    super.mouseClicked(e);
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
            } else {
                exit();
            }
        });
    }

    private void exit() {
        threads.forEach((s, monitor) -> monitor.setCanceled(true));
        threads.clear();
        System.exit(0);
    }

    private void show(final Stage stage) {
        Platform.runLater(stage::show);
    }

    private void deleteFolder(Folder folder) {
        observableList.removeIf(f -> f.getPath().equals(folder.getPath()));
        FolderModel.RemoveFolder(folder, GetFolderIndex());
    }

    private void openFolderForm() {
        Platform.runLater(() -> {
            Stage stage1 = new Stage(StageStyle.UTILITY);
            stage1.initModality(Modality.APPLICATION_MODAL);

            FolderForm folderForm = new FolderForm();
            folderForm.setStage(stage1);
            folderForm.setUrlChecker(AFTool::GetDestinations);

            stage1.setScene(new Scene(folderForm));
            stage1.showAndWait();

            Folder newFolder = folderForm.getFolder();
            if (newFolder != null)
                addFolder(folderForm.getFolder());
        });

    }

    private Void openFolderDetails(Folder folder) {
        Platform.runLater(() -> {
            Stage stage = new Stage(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);

            FolderDetails folderDetails = new FolderDetails();
            folderDetails.setStage(stage);
            folderDetails.setFolder(folder);
            folderDetails.setUrlChecker(AFTool::GetDestinations);
            folderDetails.setUpdateFolder(this::updateFolder);
            folderDetails.setSyncFolder(AFTool::Upload);
            folderDetails.setDeleteFolder(this::deleteFolder);
            stage.setScene(new Scene(folderDetails));
            stage.showAndWait();
        });

        return null;
    }


}
