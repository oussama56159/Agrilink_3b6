package controllers;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import services.UserServiceImpl;

public class UserAddController implements Initializable {

    @FXML private ImageView profileImage;
    @FXML private Button changePhotoButton;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField password;
    @FXML private TextField confirmPassword;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker registrationDatePicker;
    @FXML private TextArea biographyArea;

    private static final double AVATAR_SIZE = 120;
    private static final double RADIUS = AVATAR_SIZE / 2;

    private final UserService userService = new UserServiceImpl();
    private File selectedPhotoFile;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configure image view
        profileImage.setFitWidth(AVATAR_SIZE);
        profileImage.setFitHeight(AVATAR_SIZE);
        profileImage.setPreserveRatio(false);
        profileImage.setClip(new Circle(RADIUS, RADIUS, RADIUS));

        roleComboBox.setItems(FXCollections.observableArrayList("Administrateur","utilisateur"));
        typeComboBox.setItems(FXCollections.observableArrayList("Acheteur","Agriculteur","Grossiste"));
        statusComboBox.setItems(FXCollections.observableArrayList("Actif","Inactif","En attente","Suspendu"));

        registrationDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleChangePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedPhotoFile = fileChooser.showOpenDialog(changePhotoButton.getScene().getWindow());
        if (selectedPhotoFile != null) {
            Image image = new Image(selectedPhotoFile.toURI().toString(), AVATAR_SIZE, AVATAR_SIZE, false, true);
            profileImage.setImage(image);
            profileImage.setClip(new Circle(RADIUS, RADIUS, RADIUS));
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // Validate required fields
        if (!validateForm()) {
            return;
        }

        try {
            // Build user object
            User user = new User(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    password.getText().trim(),
                    roleComboBox.getValue(),
                    typeComboBox.getValue(),
                    statusComboBox.getValue(),
                    registrationDatePicker.getValue()
            );
            user.setPhone(phoneField.getText().trim());
            user.setAddress(addressField.getText().trim());
            user.setCity(cityField.getText().trim());
            user.setPostalCode(postalCodeField.getText().trim());
            user.setBiography(biographyArea.getText().trim());
            if (selectedPhotoFile != null) {
                user.setProfileImagePath(selectedPhotoFile.getAbsolutePath());
            }

            // Persist to DB
            userService.Create(user);

            // Confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Utilisateur créé");
            alert.setHeaderText(null);
            alert.setContentText("Le nouvel utilisateur a été créé avec succès.");
            alert.showAndWait();

            navigateToUsers();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de la création de l'utilisateur");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML private void handleCancel(ActionEvent event) {
        navigateToUsers();
    }

    private boolean validateForm() {
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

        String pwd = password.getText();
        String confirm = confirmPassword.getText();
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

        // Finalize
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    @FXML private void navigateToDashboard() { loadView("Dashboard"); }
    @FXML private void navigateToUsers()     { loadView("UserManagement"); }
    @FXML private void navigateToProducts()  { loadView("Products"); }
    @FXML private void navigateToStatistics(){ loadView("Statistics"); }
    @FXML private void navigateToSettings()  { loadView("Settings"); }
    @FXML private void handleLogout()        { loadView("Connexion"); }

    private void loadView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
            Parent root = loader.load();
            controllers.MainController mainController = loader.getController();
            mainController.loadView(viewName);
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
