package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import services.UserService;
import services.UserServiceImpl;
import utils.SessionManager;

public class ConnexionController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserServiceImpl();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de connexion", "Veuillez entrer l'email et le mot de passe.");
            return;
        }

        try {
            User user = userService.login(email, password);

            if (user != null) {
                // Store user in session
                SessionManager.getInstance().setCurrentUser(user);
                System.out.println("Login successful for: " + user.getFirstName() + " " + user.getLastName());

                // Role-based navigation
                String role = user.getRole().toLowerCase();
                if ("administrateur".equals(role)) {
                    navigateToView("UserManagement");
                } else if ("utilisateur".equals(role)) {
                    navigateToView("Dashboard");
                } else {
                    showAlert(AlertType.ERROR, "Accès refusé", "Votre rôle (" + user.getRole() + ") n'est pas autorisé.");
                }

            } else {
                showAlert(AlertType.ERROR, "Échec de connexion", "Email ou mot de passe invalide.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur de connexion", "Une erreur s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();

            // Load the desired view dynamically
            mainController.loadView(viewName);

            // Replace the current scene content
            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur de navigation", "Impossible de naviguer vers la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.loadView("PasswordReset");

            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur de navigation", "Impossible de naviguer vers la réinitialisation du mot de passe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavigateToInscription(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.loadView("Inscription");

            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur de navigation", "Impossible de naviguer vers l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
