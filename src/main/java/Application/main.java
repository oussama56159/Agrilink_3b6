package Application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main view
        Parent root = FXMLLoader.load(getClass().getResource("/Views/Main.fxml"));

        // Create the scene
        Scene scene = new Scene(root, 1000, 600);


        // Configure the stage
        primaryStage.setTitle("AgriLink");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/imgs/logo.png")));

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}