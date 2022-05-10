package vizanalyzer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author KH9132
 */
public class VizAnalyzer extends Application {

    static Stage mainWindow;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("VizAnalyzerUI.fxml"));
            Scene myScene = new Scene(root);
            mainWindow = primaryStage;
            primaryStage.setScene(myScene);
            primaryStage.resizableProperty().set(false);
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(VizAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }

}
