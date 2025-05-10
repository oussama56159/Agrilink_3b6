package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import models.User;
import services.UserService;
import services.UserServiceImpl;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;

public class UserModifyController implements Initializable {

    @FXML
    private ImageView profileImage;

    @FXML
    private Button changePhotoButton;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField postalCodeField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private DatePicker registrationDatePicker;

    @FXML
    private TextArea biographyArea;

    @FXML
    private ComboBox<String> roleFilter;

    @FXML private TextField password;

    @FXML private TextField confirmPassword;
    @FXML
    private Button dashboardBtn;

    @FXML
    private Button forumBtn;

    @FXML
    private Button usersBtn;

    @FXML
    private Button productsBtn;

    @FXML
    private Button fournisseur;

    @FXML
    private Button commande;
    @FXML
    private Button adminBtn;
    @FXML private Label adminEmailLabel;


    private static final double AVATAR_SIZE = 120;
    private static final double RADIUS = AVATAR_SIZE / 2;

    private UserService userService;
    private User currentUser;
    private File selectedImageFile;
    private String originalEmail; // Store original email to check if it changed

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the user service
        try {
            userService = new UserServiceImpl();
            System.out.println("UserService initialized successfully in UserModifyController");
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données. Veuillez contacter le support.");
        }

        // Set ImageView to fixed square for circle
        if (profileImage != null) {
            profileImage.setFitWidth(AVATAR_SIZE);
            profileImage.setFitHeight(AVATAR_SIZE);
            profileImage.setPreserveRatio(false);

            // Apply initial circular clip
            Circle clip = new Circle(RADIUS, RADIUS, RADIUS);
            profileImage.setClip(clip);
        }

        // Initialize roleFilter safely
        if (roleFilter != null) {
            roleFilter.setItems(FXCollections.observableArrayList("Tous","Administrateur","utilisateur"));
            roleFilter.setValue("Tous");
        }

        // Initialize comboboxes
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList("Administrateur","utilisateur"));
        }
        if (typeComboBox != null) {
            typeComboBox.setItems(FXCollections.observableArrayList( "Acheteur", "Agriculteur", "Grossiste"));
        }
        if (statusComboBox != null) {
            statusComboBox.setItems(FXCollections.observableArrayList("Actif", "Bloqué", "Suspendu"));
        }

        // Set current date as default
        if (registrationDatePicker != null) {
            registrationDatePicker.setValue(LocalDate.now());
        }
        displayCurrentUserInfo();
    }
    @FXML
    private void handleChangePhoto(ActionEvent event) {
        if (profileImage == null || changePhotoButton == null) {
            System.err.println("Error: profileImage or changePhotoButton is null");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(changePhotoButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Store the selected file for later use
                this.selectedImageFile = selectedFile;

                // Load image with fixed size
                Image image = new Image(selectedFile.toURI().toString(), AVATAR_SIZE, AVATAR_SIZE, false, true);
                profileImage.setImage(image);

                // Re-apply fixed circular clip
                Circle clip = new Circle(RADIUS, RADIUS, RADIUS);
                profileImage.setClip(clip);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur d'image", "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateForm()) {
            try {
                // Check if currentUser is null
                if (currentUser == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur chargé pour l'édition.");
                    return;
                }

                // Save profile image if one was selected
                String profileImagePath = currentUser.getProfileImagePath(); // Keep existing path by default

                if (selectedImageFile != null) {
                    try {
                        // Create a unique filename for the image
                        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(selectedImageFile.getName());

                        // Define the directory to save profile images (create if it doesn't exist)
                        Path uploadDir = Paths.get("src/main/resources/uploads/profiles");
                        Files.createDirectories(uploadDir);

                        // Copy the file to the uploads directory
                        Path destination = uploadDir.resolve(uniqueFileName);
                        Files.copy(selectedImageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                        // Save the relative path to be stored in the user object
                        // Make sure this path starts with a slash and uses the correct format
                        profileImagePath = "/uploads/profiles/" + uniqueFileName;

                        System.out.println("Profile image saved to: " + destination.toAbsolutePath());
                        System.out.println("Profile image path set to: " + profileImagePath);
                    } catch (Exception e) {
                        System.err.println("Error saving profile image: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.WARNING, "Avertissement d'image",
                                "Impossible de sauvegarder l'image de profil, mais l'utilisateur sera mis à jour.");
                    }
                }

// Make sure to set the path on the user object
                currentUser.setProfileImagePath(profileImagePath);

                // Check if email has changed
                String newEmail = emailField != null && emailField.getText() != null ?
                        emailField.getText().trim() : "";
                boolean emailChanged = !newEmail.equals(originalEmail);

                // If email changed, check if the new email already exists
                if (emailChanged && !newEmail.isEmpty()) {
                    try {
                        if (emailExists(newEmail)) {
                            showAlert(Alert.AlertType.ERROR, "Erreur d'email",
                                    "Cet email est déjà utilisé. Veuillez choisir un autre email.");
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("Error checking email existence: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Update user object with form data - with null checks for all fields
                if (firstNameField != null && firstNameField.getText() != null) {
                    currentUser.setFirstName(firstNameField.getText().trim());
                }

                if (lastNameField != null && lastNameField.getText() != null) {
                    currentUser.setLastName(lastNameField.getText().trim());
                }

                if (emailField != null && emailField.getText() != null) {
                    currentUser.setEmail(emailField.getText().trim());
                }

                if (phoneField != null && phoneField.getText() != null) {
                    currentUser.setPhone(phoneField.getText().trim());
                } else {
                    currentUser.setPhone(""); // Set empty string if field is null
                }

                if (addressField != null && addressField.getText() != null) {
                    currentUser.setAddress(addressField.getText().trim());
                } else {
                    currentUser.setAddress(""); // Set empty string if field is null
                }

                if (cityField != null && cityField.getText() != null) {
                    currentUser.setCity(cityField.getText().trim());
                } else {
                    currentUser.setCity(""); // Set empty string if field is null
                }

                if (postalCodeField != null && postalCodeField.getText() != null) {
                    currentUser.setPostalCode(postalCodeField.getText().trim());
                } else {
                    currentUser.setPostalCode(""); // Set empty string if field is null
                }

                if (roleComboBox != null && roleComboBox.getValue() != null) {
                    currentUser.setRole(roleComboBox.getValue());
                }

                if (typeComboBox != null && typeComboBox.getValue() != null) {
                    currentUser.setType(typeComboBox.getValue());
                }

                if (statusComboBox != null && statusComboBox.getValue() != null) {
                    currentUser.setStatus(statusComboBox.getValue());
                }

                if (registrationDatePicker != null && registrationDatePicker.getValue() != null) {
                    currentUser.setRegistrationDate(registrationDatePicker.getValue());
                }

                if (biographyArea != null && biographyArea.getText() != null) {
                    currentUser.setBiography(biographyArea.getText().trim());
                } else {
                    currentUser.setBiography(""); // Set empty string if field is null
                }

                // Only update password if a new one is provided
                if (password != null && password.getText() != null && !password.getText().trim().isEmpty()) {
                    String newPassword = password.getText().trim();
                    String confirmPwd = confirmPassword != null ? confirmPassword.getText().trim() : "";
                    
                    // Verify that passwords match
                    if (newPassword.equals(confirmPwd)) {
                        // Password will be hashed in the UserServiceImpl.update method
                        // The update method checks if the password is already hashed
                        currentUser.setPassword(newPassword);
                    } else {
                        // Show error if passwords don't match
                        showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Les mots de passe ne correspondent pas.");
                        return; // Stop the save process if passwords don't match
                    }
                }
                // Note: We don't set an empty password if none is provided, to preserve the existing password

                currentUser.setProfileImagePath(profileImagePath);

                // Save user to database
                userService.update(currentUser);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur mis à jour avec succès.");

                // Navigate back to user management
                navigateToUsers();

            } catch (Exception e) {
                System.err.println("Error updating user: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur de mise à jour",
                        "Échec de la mise à jour de l'utilisateur: " + e.getMessage());
            }
        }
    }

    private boolean emailExists(String email) throws Exception {
        // This is a simple implementation - you might want to add a specific method to UserService
        try {
            // Get all users and check if any has the same email
            for (User user : userService.DisplayAll()) {
                if (user.getEmail().equalsIgnoreCase(email) && !user.getEmail().equalsIgnoreCase(originalEmail)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new Exception("Error checking email existence: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateToUsers();
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Veuillez corriger les erreurs suivantes:\n");

        // Check for null fields first
        if (firstNameField == null || lastNameField == null || emailField == null ||
                roleComboBox == null || typeComboBox == null || statusComboBox == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de formulaire",
                    "Certains champs du formulaire ne sont pas correctement initialisés. Veuillez contacter le support.");
            return false;
        }

        // Now check field values
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            firstNameField.setStyle("-fx-border-color: red;");
            errorMessage.append("- Le prénom est requis\n");
            isValid = false;
        } else {
            firstNameField.setStyle("");
        }

        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            lastNameField.setStyle("-fx-border-color: red;");
            errorMessage.append("- Le nom est requis\n");
            isValid = false;
        } else {
            lastNameField.setStyle("");
        }

        if (emailField.getText() == null || emailField.getText().trim().isEmpty() ||
                !emailField.getText().contains("@")) {
            emailField.setStyle("-fx-border-color: red;");
            errorMessage.append("- Un email valide est requis\n");
            isValid = false;
        } else {
            emailField.setStyle("");
        }

        if (roleComboBox.getValue() == null) {
            roleComboBox.setStyle("-fx-border-color: red;");
            errorMessage.append("- Le rôle est requis\n");
            isValid = false;
        } else {
            roleComboBox.setStyle("");
        }

        if (typeComboBox.getValue() == null) {
            typeComboBox.setStyle("-fx-border-color: red;");
            errorMessage.append("- Le type est requis\n");
            isValid = false;
        } else {
            typeComboBox.setStyle("");
        }

        if (statusComboBox.getValue() == null) {
            statusComboBox.setStyle("-fx-border-color: red;");
            errorMessage.append("- Le statut est requis\n");
            isValid = false;
        } else {
            statusComboBox.setStyle("");
        }

        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
        }

        return isValid;
    }

    @FXML private void navigateToDashboard() { loadView("Dashboard"); }

    @FXML private void navigateToUsers()     { loadView("UserManagement"); }
    @FXML private void navigateToProducts()  { loadView("Products"); }
    @FXML private void navigateToStatistics(){ loadView("Statistics"); }
    @FXML private void navigateToSettings()  { loadView("Settings"); }
    @FXML private void handleLogout()        { loadView("Connexion"); }

    private void loadView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.loadView(viewName);

            // If navigating back to user management, refresh the user list
            if (viewName.equals("UserManagement")) {
                try {
                    // Get the UserManagementController and refresh the user list
                    FXMLLoader userManagementLoader = new FXMLLoader(getClass().getResource("/Views/UserManagement.fxml"));
                    userManagementLoader.load();
                    UserManagementController userManagementController = userManagementLoader.getController();
                    userManagementController.refreshUserList();
                } catch (IOException e) {
                    System.err.println("Error refreshing user list: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Check if firstNameField is not null before accessing its scene
            if (firstNameField != null && firstNameField.getScene() != null) {
                firstNameField.getScene().setRoot(root);
            } else {
                System.err.println("Error: firstNameField or its scene is null");
                // Try to find another control that might have a valid scene
                if (lastNameField != null && lastNameField.getScene() != null) {
                    lastNameField.getScene().setRoot(root);
                } else if (emailField != null && emailField.getScene() != null) {
                    emailField.getScene().setRoot(root);
                } else if (changePhotoButton != null && changePhotoButton.getScene() != null) {
                    changePhotoButton.getScene().setRoot(root);
                } else {
                    System.err.println("Error: Could not find a valid scene to set the root");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + e.getMessage());
        }
    }

    public void setUserForEditing(User user) {
        this.currentUser = user;
        this.originalEmail = user.getEmail(); // Store original email

        // Add null checks for all fields
        if (firstNameField != null) {
            firstNameField.setText(user.getFirstName());
        }

        if (lastNameField != null) {
            lastNameField.setText(user.getLastName());
        }

        if (emailField != null) {
            emailField.setText(user.getEmail());
        }

        if (phoneField != null) {
            phoneField.setText(user.getPhone());
        }

        if (addressField != null) {
            addressField.setText(user.getAddress());
        }

        if (cityField != null) {
            cityField.setText(user.getCity());
        }

        if (postalCodeField != null) {
            postalCodeField.setText(user.getPostalCode());
        }

        if (roleComboBox != null) {
            roleComboBox.setValue(user.getRole());
        }

        if (typeComboBox != null) {
            typeComboBox.setValue(user.getType());
        }

        if (statusComboBox != null) {
            statusComboBox.setValue(user.getStatus());
        }

        if (registrationDatePicker != null && user.getRegistrationDate() != null) {
            registrationDatePicker.setValue(user.getRegistrationDate());
        }

        if (biographyArea != null) {
            biographyArea.setText(user.getBiography());
        }
        if (password != null) {
            password.setText("");
        }
        if (confirmPassword != null) {
            confirmPassword.setText("");
        }

        if (profileImage != null && user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
            try {
                // Try to load from absolute path first
                File imageFile = new File(user.getProfileImagePath());

                // If file doesn't exist, try to load from resources
                if (!imageFile.exists()) {
                    // Adjust path to look in resources
                    String resourcePath = "src/main/resources" + user.getProfileImagePath();
                    imageFile = new File(resourcePath);
                }

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), AVATAR_SIZE, AVATAR_SIZE, false, true);
                    profileImage.setImage(image);
                    // Re-apply circular clip
                    profileImage.setClip(new Circle(RADIUS, RADIUS, RADIUS));
                } else {
                    System.out.println("Profile image not found: " + user.getProfileImagePath());
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void handleNavigation(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();

            try {
                String viewName = "";

                if (clickedButton == dashboardBtn) {
                    viewName = "Dashboard";
                } else if (clickedButton == forumBtn) {
                    viewName = "Forum";
                } else if (clickedButton == usersBtn) {
                    viewName = "UserManagement";
                } else if (clickedButton == productsBtn) {
                    viewName = "Products";
                } else if (clickedButton ==fournisseur ) {
                    viewName = "Statistics";
                } else if (clickedButton == commande) {
                    viewName = "Settings";
                }

                if (!viewName.isEmpty()) {
                    // Get the current scene from the event source
                    Scene currentScene = ((Node) event.getSource()).getScene();
                    if (currentScene != null) {
                        // Load the view through MainController
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
                        Parent root = loader.load();
                        MainController mainController = loader.getController();
                        mainController.loadView(viewName);

                        // Replace scene content
                        currentScene.setRoot(root);
                    } else {
                        System.out.println("Error: Current scene is null");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error navigating to view: " + e.getMessage());
            }
        }
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
    private void  displayCurrentUserInfo() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            adminBtn.setText(current.getFirstName() + " " + current.getLastName());
            adminEmailLabel.setText(current.getEmail());
        }
    }
}