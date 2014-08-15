package activefolders;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/main_view.fxml"));
        Parent root = loader.load();
        Platform.setImplicitExit(false);
        controller = loader.getController();
        primaryStage.setTitle("ActiveFolders");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        controller.createTrayIcon(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
