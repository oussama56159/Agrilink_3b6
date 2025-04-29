package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainController implements Initializable {

    @FXML
    private StackPane mainContainer;

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Make sure the content area fills the available space
        contentArea.prefWidthProperty().bind(mainContainer.widthProperty());
        contentArea.prefHeightProperty().bind(mainContainer.heightProperty());

        // Load the default view (Connexion)
        loadView("Connexion");
    }

    public void loadView(String viewName) {
        try {
            // Load the requested view
            Parent view = FXMLLoader.load(getClass().getResource("/Views/" + viewName + ".fxml"));

            // Clear the current content and add the new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}