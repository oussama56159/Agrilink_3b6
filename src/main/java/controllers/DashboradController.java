package controllers;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import models.User;
import services.UserService;
import services.UserServiceImpl;
import utils.SessionManager;

public class DashboradController implements Initializable {
    @FXML
    private Button viewPf;
    @FXML
    private Label MailLabel;
    @FXML
    private Label acheteurCount;
    @FXML
    private Label agriculteurCount;
    @FXML
    private Label grossisteCount;
    
    private UserService userService;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private ObservableList<User> filteredUsers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the user service
        try {
            userService = new UserServiceImpl();
            System.out.println("UserService initialized successfully in UserManagementController");
            updateUserTypeStatistics();
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to connect to the database. Please contact support.");
        }

        displayCurrentuserInfo();
    }

    private void updateUserTypeStatistics() {
        try {
            int[] stats = userService.getUserTypeStatistics();
            acheteurCount.setText(String.valueOf(stats[0]));
            agriculteurCount.setText(String.valueOf(stats[1]));
            grossisteCount.setText(String.valueOf(stats[2]));
        } catch (Exception e) {
            System.err.println("Error updating user type statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Get the current scene from the event source
            Scene currentScene = ((Node) event.getSource()).getScene();
            if (currentScene != null) {
                // Navigate back to login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Connexion.fxml"));
                Parent root = loader.load();

                // Replace scene content
                currentScene.setRoot(root);
                System.out.println("User logged out");
            } else {
                System.out.println("Error: Current scene is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error navigating to login screen: " + e.getMessage());
        }
    }
    public void navigateToUserProfile(ActionEvent actionEvent) {
        try {
            // Get the current scene from the event source
            Scene currentScene = ((Node) actionEvent.getSource()).getScene();
            if (currentScene != null) {
                // Navigate to user profile view through MainController
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
                Parent root = loader.load();
                MainController mainController = loader.getController();

                // Use "UserProfile" instead of "UserProfileView" to match your file name
                mainController.loadView("UserProfileView");

                // Replace scene content
                currentScene.setRoot(root);
                System.out.println("Navigated to User Profile");
            } else {
                System.out.println("Error: Current scene is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error navigating to User Profile: " + e.getMessage());
        }
    }
    private void displayCurrentuserInfo() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            viewPf.setText(current.getFirstName() + " " + current.getLastName());
            MailLabel.setText(current.getEmail());
        }
    }
}
