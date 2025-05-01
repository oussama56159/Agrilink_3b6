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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import services.EmailService;
import services.UserService;
import services.UserServiceImpl;
import services.GoogleAuthService;

public class PasswordResetController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private Button submitButton;

    @FXML
    private Button backButton;

    private UserService userService;
    private EmailService emailService;
    private GoogleAuthService googleAuthService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserServiceImpl();
        emailService = new EmailService();
        googleAuthService = new GoogleAuthService();
    }

    @FXML
    private void handleSendResetLink(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur", "Le champ email ne peut pas être vide");
            return;
        }

        // Check if email exists in the database
        if (userService.findByEmail(email) == null) {
            showAlert(AlertType.ERROR, "Erreur", "Aucun compte n'est associé à cet email");
            return;
        }

        // Generate a new random password
        String newPassword = googleAuthService.generateRandomPassword();

        try {
            // Update the user's password in the database
            userService.updatePassword(email, newPassword);
            
            // Send the new password via email
            emailService.sendPasswordResetEmail(email, newPassword);

            // Navigate to the email sent confirmation page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EmailSent.fxml"));
            Parent root = loader.load();

            // Pass the email to the EmailSentController
            EmailSentController controller = loader.getController();
            controller.setUserEmail(email);

            // Replace scene content
            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la réinitialisation du mot de passe");
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
            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur de navigation", "Impossible de naviguer vers la connexion");
            e.printStackTrace();
        }
    }
}