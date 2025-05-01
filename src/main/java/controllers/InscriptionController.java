package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.UUID;
import models.User;
import services.UserServiceImpl;
import services.UserService;
import java.time.LocalDate;

public class InscriptionController implements Initializable {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextField addressField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField postalCodeField;

    @FXML
    private ComboBox<String> userTypeComboBox;

    @FXML
    private ImageView profileImage;

    @FXML
    private Button changePhotoButton;

    @FXML
    private Text placeholderText;

    @FXML
    private CheckBox termsCheckBox;

    private File selectedImageFile;
    private String profileImagePath;

    private static final double AVATAR_SIZE = 120;
    private static final double RADIUS = AVATAR_SIZE / 2;

    private UserService userService;

    // Flag to indicate if we should use the placeholder text feature
    private boolean usePlaceholderText = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the ComboBox with user types
        ObservableList<String> userTypes = FXCollections.observableArrayList(
                "Acheteur", "Agriculteur", "Grossiste"
        );
        userTypeComboBox.setItems(userTypes);
        userTypeComboBox.getSelectionModel().selectFirst();

        // Initialize the user service
        try {
            userService = new UserServiceImpl();
            System.out.println("UserService initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données. Veuillez contacter le support.");
        }

        // Check if placeholderText is available in the FXML
        usePlaceholderText = (placeholderText != null);

        // Only add listeners if we're using the placeholder text feature
        if (usePlaceholderText) {
            firstNameField.textProperty().addListener((observable, oldValue, newValue) -> handleNameInput());
            lastNameField.textProperty().addListener((observable, oldValue, newValue) -> handleNameInput());
        }
    }

    private void updatePlaceholderWithInitials() {
        // Skip if placeholderText is not available
        if (!usePlaceholderText || placeholderText == null) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            String initials = "";
            if (!firstName.isEmpty()) {
                initials += firstName.substring(0, 1).toUpperCase();
            }
            if (!lastName.isEmpty()) {
                initials += lastName.substring(0, 1).toUpperCase();
            }

            placeholderText.setText(initials);
            placeholderText.setFont(Font.font(30));
        } else {
            placeholderText.setText("📷");
        }
    }

    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(changePhotoButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Store the selected file for later use
                selectedImageFile = selectedFile;

                Image image = new Image(selectedFile.toURI().toString(), AVATAR_SIZE, AVATAR_SIZE, false, true);
                profileImage.setImage(image);

                // Re-apply fixed circular clip
                Circle clip = new Circle(RADIUS, RADIUS, RADIUS);
                profileImage.setClip(clip);

                // Hide placeholder text when image is selected (if it exists)
                if (usePlaceholderText && placeholderText != null) {
                    placeholderText.setVisible(false);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur d'image", "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Save profile image if one was selected
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
                profileImagePath = "/uploads/profiles/" + uniqueFileName;

                System.out.println("Profile image saved: " + profileImagePath);
            } catch (Exception e) {
                System.err.println("Error saving profile image: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.WARNING, "Avertissement d'image",
                        "Impossible de sauvegarder l'image de profil, mais l'inscription continuera.");
            }
        }

        try {
            // Print debug information
            System.out.println("Creating user with the following information:");
            System.out.println("First Name: " + firstNameField.getText());
            System.out.println("Last Name: " + lastNameField.getText());
            System.out.println("Email: " + emailField.getText());
            System.out.println("User Type: " + userTypeComboBox.getValue());

            // Create a new User object with the form data
            User newUser = new User(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    passwordField.getText(), // Include password
                    "utilisateur", // default role
                    "Acheteur", // default type
                    "Actif", // default status
                    LocalDate.now()
            );

            // Set additional user properties
            newUser.setAddress(addressField.getText());
            newUser.setCity(cityField.getText());
            newUser.setPostalCode(postalCodeField.getText());
            newUser.setProfileImagePath(profileImagePath);

            System.out.println("User object created successfully, attempting to save to database...");

            // Save the user to database
            userService.Create(newUser);

            System.out.println("User saved to database successfully!");

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Inscription réussie",
                    "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");

            // Navigate back to login after registration
            handleNavigateToConnexion(event);

        } catch (Exception e) {
            System.err.println("Error saving user to database: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Échec de l'inscription",
                    "Impossible de créer votre compte: " + e.getMessage());
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
    private void handleNavigateToConnexion(ActionEvent event) {
        try {
            // Navigate to connexion view through MainController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.loadView("Connexion");

            // Replace scene content
            firstNameField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenTermsAndConditions(ActionEvent actionEvent) {
        System.out.println("you have accepted our terms and conditions ");
        // You could open a dialog with the terms and conditions here
        showAlert(Alert.AlertType.INFORMATION, "Conditions générales",
                "En vous inscrivant, vous acceptez nos conditions générales d'utilisation.");
    }

    // Add listeners to update the placeholder when user types their name
    @FXML
    private void handleNameInput() {
        updatePlaceholderWithInitials();
    }


// …

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Name‐validation regex: letters plus optional internal spaces, hyphens or apostrophes
        Pattern namePattern = Pattern.compile("^[A-Za-z]+(?:[-' ][A-Za-z]+)*$");
        // Email regex: basic RFC‐style check
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,24}$");

        // 1) First Name
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            errors.append("First name is required\n");
        } else if (!namePattern.matcher(firstName).matches()) {
            errors.append("First name must be 2–50 letters and may include hyphens/apostrophes\n");
        }

        // 2) Last Name
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            errors.append("Last name is required\n");
        } else if (!namePattern.matcher(lastName).matches()) {
            errors.append("Last name must be 2–50 letters and may include hyphens/apostrophes\n");
        }

        // 3) Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errors.append("Email is required\n");
        } else if (!emailPattern.matcher(email).matches()) {
            errors.append("Invalid email format\n");
        }

        // 4) Password + confirmation
        String pwd = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        if (pwd.isEmpty()) {
            errors.append("Password is required\n");
        } else {
            // length check
            if (pwd.length() < 8) {
                errors.append("Password must be at least 8 characters\n");
            }
            // complexity: count character classes
            int classes = 0;
            if (pwd.matches(".*[A-Z].*")) classes++;
            if (pwd.matches(".*[a-z].*")) classes++;
            if (pwd.matches(".*\\d.*"))    classes++;
            if (pwd.matches(".*[^A-Za-z0-9].*")) classes++;
            if (classes < 3) {
                errors.append("Password must include at least three of: uppercase, lowercase, digit, special character\n");
            }
            // match check
            if (!pwd.equals(confirm)) {
                errors.append("Passwords do not match\n");
            }
        }

        // 5) Terms & Conditions
        if (termsCheckBox != null && !termsCheckBox.isSelected()) {
            errors.append("You must accept the terms and conditions\n");
        }

        // Finalize
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errors.toString());
            return false;
        }
        return true;
    }


    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
