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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import services.EmailService;
import services.UserService;
import services.UserServiceImpl;
import services.GoogleAuthService;

public class EmailSentController implements Initializable {

    @FXML
    private Label confirmationMessage;

    @FXML
    private Button resendButton;

    @FXML
    private Button backButton;

    private String userEmail;
    private EmailService emailService;
    private UserService userService;
    private GoogleAuthService googleAuthService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize services
        emailService = new EmailService();
        userService = new UserServiceImpl();
        googleAuthService = new GoogleAuthService();
        
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
        confirmationMessage.setText("Nous avons envoyé un nouveau mot de passe à " + userEmail);
    }

    @FXML
    private void handleResendLink(ActionEvent event) {
        try {
            // Generate a new random password
            String newPassword = googleAuthService.generateRandomPassword();
            
            // Update the user's password in the database
            userService.updatePassword(userEmail, newPassword);
            
            // Send the new password via email
            emailService.sendPasswordResetEmail(userEmail, newPassword);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Un nouveau mot de passe a été envoyé à " + userEmail);
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'envoi du nouveau mot de passe");
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
        // Implement contact support logic here
        System.out.println("Contact support clicked");
    }
}