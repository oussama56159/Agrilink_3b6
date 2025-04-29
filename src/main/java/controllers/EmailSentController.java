package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

public class EmailSentController implements Initializable {

    @FXML
    private Label confirmationMessage;

    @FXML
    private Button resendButton;

    @FXML
    private Button backButton;

    private String userEmail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize with default email (this would normally be passed from the previous screen)
        userEmail = "example@gmail.com";
        updateConfirmationMessage();
    }

    /**
     * Sets the email address and updates the confirmation message
     * @param email The email address to display
     */
    public void setUserEmail(String email) {
        this.userEmail = email;
        updateConfirmationMessage();
    }

    /**
     * Updates the confirmation message with the current email address
     */
    private void updateConfirmationMessage() {
        confirmationMessage.setText("Nous avons envoyé un lien de réinitialisation à " + userEmail);
    }

    @FXML
    private void handleResendLink(ActionEvent event) {
        // Implement resend logic here
        System.out.println("Resending password reset link to: " + userEmail);

        // In a real application, you would resend the email
        // You could also show a confirmation message or disable the button temporarily
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
            backButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleContactSupport(ActionEvent event) {
        // Implement contact support logic
        System.out.println("Opening support contact page");

        // In a real application, you would navigate to a support page
        // or open a support dialog
    }
}