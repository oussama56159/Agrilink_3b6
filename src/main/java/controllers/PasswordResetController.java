package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class PasswordResetController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private Button submitButton;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize any components if needed
    }

    @FXML
    private void handleSendResetLink(ActionEvent event) {
        // Implement password reset logic here
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            System.out.println("Email field cannot be empty");
            return;
        }

        // In a real application, you would send a reset link to the email
        System.out.println("Password reset link sent to: " + email);

        // Navigate to the email sent confirmation page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EmailSent.fxml"));
            Parent root = loader.load();

            // Pass the email to the EmailSentController
            EmailSentController controller = loader.getController();
            controller.setUserEmail(email);

            // Replace scene content
            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavigateToConnexion(ActionEvent event) {
        try {
            // Navigate back to the login view through MainController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.loadView("Connexion");

            // Replace scene content
            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}