package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import models.User;
import services.UserService;
import services.UserServiceImpl;
import utils.SessionManager;

public class UserProfileController implements Initializable {

    @FXML private Button dashboardBtn, usersBtn, productsBtn, statisticsBtn, settingsBtn, logoutBtn;
    @FXML private Circle profilePhotoCircle;
    @FXML private ImageView profileImage;

    @FXML private Label userNameLabel, locationLabel, emailLabel, phoneLabel, addressLabel, memberSinceLabel, biolabel;
    @FXML private Button editProfileBtn, productsTabBtn, reviewsTabBtn, eventsTabBtn, publicationsTabBtn, addProductBtn;
    @FXML private VBox productsTabContent, reviewsTabContent, eventsTabContent, publicationsTabContent;
    @FXML private Label sidebarUserName, sidebarUserEmail;
    @FXML
    private Button adminBtn;
    @FXML private Label adminEmailLabel;

    @FXML
    private Button forumBtn;

    @FXML
    private Button fournisseur;

    @FXML
    private Button commande;

    private static final double AVATAR_SIZE = 120;
    private static final double RADIUS = AVATAR_SIZE / 2;

    private User currentUser;
    private UserService userService;
    private String currentUserEmail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeUserService();
            currentUserEmail = SessionManager.getInstance().getCurrentUserEmail();
            loadCurrentUserData();
            if (productsTabBtn != null && productsTabContent != null) showProductsTab();
            loadProfileImage();
            System.out.println("UserProfileController initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'initialiser le profil: " + e.getMessage());
        }
    }

    private void initializeUserService() {
        try {
            userService = new UserServiceImpl();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Impossible de se connecter à la base de données.");
        }
        displayCurrentUserInfo();
    }

    public void loadCurrentUserData() {
        try {
            boolean userFound = false;
            for (User user : userService.DisplayAll()) {
                if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(currentUserEmail)) {
                    currentUser = user;
                    userFound = true;
                    break;
                }
            }
            if (!userFound) {
                System.out.println("User not found. Using default profile.");
                currentUser = new User("Admin", "User", currentUserEmail, "ADMIN", "Staff", "ACTIVE", java.time.LocalDate.now());
                currentUser.setProfileImagePath("/images/profileDefault.png");
            }
            // Sidebar
            if (sidebarUserName != null) sidebarUserName.setText(currentUser.getName());
            if (sidebarUserEmail != null) sidebarUserEmail.setText(currentUser.getEmail());
            // Main labels
            if (userNameLabel != null) userNameLabel.setText(currentUser.getName());
            if (biolabel != null) biolabel.setText(currentUser.getBiography());
            String location = (currentUser.getCity() != null ? currentUser.getCity() : "")
                    + (currentUser.getPostalCode() != null ? ", " + currentUser.getPostalCode() : "");
            if (locationLabel != null) locationLabel.setText(location.isBlank() ? "Location not specified" : location);
            if (emailLabel != null) emailLabel.setText(currentUser.getEmail());
            if (phoneLabel != null) phoneLabel.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "Not specified");
            String address = (currentUser.getAddress() != null ? currentUser.getAddress() : "");
            if (!location.isBlank()) address += ", " + location;
            if (addressLabel != null) addressLabel.setText(address.isBlank() ? "Not specified" : address);
            if (memberSinceLabel != null && currentUser.getRegistrationDate() != null) {
                memberSinceLabel.setText("Membre depuis " + currentUser.getRegistrationDate().getMonth() + " " + currentUser.getRegistrationDate().getYear());
            } else if (memberSinceLabel != null) {
                memberSinceLabel.setText("Membre depuis récemment");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de données", "Impossible de charger les données de l'utilisateur.");
        }
    }

    private void loadProfileImage() {
        if (profileImage == null) return;
        String path = currentUser != null ? currentUser.getProfileImagePath() : null;
        if (path != null && !path.isEmpty()) {
            // Try loading from classpath
            URL resourceUrl = getClass().getResource(path);
            if (resourceUrl != null) {
                setImage(new Image(resourceUrl.toExternalForm(), AVATAR_SIZE, AVATAR_SIZE, false, true));
                System.out.println("Loaded image from classpath: " + path);
                return;
            }
            // Try loading from filesystem
            File fsFile = new File("src/main/resources" + path);
            if (fsFile.exists()) {
                setImage(new Image(fsFile.toURI().toString(), AVATAR_SIZE, AVATAR_SIZE, false, true));
                System.out.println("Loaded image from filesystem: " + fsFile.getAbsolutePath());
                return;
            }
            System.err.println("Profile image not found: " + path);
        }
        setDefaultProfileImage();
    }

    private void setImage(Image img) {
        profileImage.setImage(img);
        profileImage.setClip(new Circle(RADIUS, RADIUS, RADIUS));
    }

    private void setDefaultProfileImage() {
        URL defaultUrl = getClass().getResource("/imgs/profileDefault.png");
        if (defaultUrl != null) {
            setImage(new Image(defaultUrl.toExternalForm(), AVATAR_SIZE, AVATAR_SIZE, false, true));
            System.out.println("Default profile image loaded.");
        } else {
            System.err.println("Default profile image not found!");
        }
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        if (userService != null) {
            loadCurrentUserData();
            loadProfileImage();
        }
    }

    @FXML private void handleEditProfile(ActionEvent event) {
        try {
            String role = currentUser.getRole();
            String fxmlPath;
            FXMLLoader loader;
            Parent root;

            if ("Administrateur".equalsIgnoreCase(role)) {
                // Admins get the full UserModifyView
                fxmlPath = "/Views/UserModifyView.fxml";
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                UserModifyController adminController = loader.getController();
                adminController.setUserForEditing(currentUser);

            } else {
                // Regular users get only personal‐details form
                fxmlPath = "/Views/ModifyPersonalDetails.fxml";
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                ModifyPersonalDetailsController userController = loader.getController();
                userController.setUserForEditing(currentUser);
            }

            // swap scenes
            ((Node) event.getSource()).getScene().setRoot(root);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de naviguer vers la vue: " + e.getMessage());
        }
    }


    @FXML private void handleTabChange(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        productsTabBtn.getStyleClass().remove("active-tab");
        reviewsTabBtn.getStyleClass().remove("active-tab");
        eventsTabBtn.getStyleClass().remove("active-tab");
        publicationsTabBtn.getStyleClass().remove("active-tab");
        productsTabContent.setVisible(false);
        reviewsTabContent.setVisible(false);
        eventsTabContent.setVisible(false);
        publicationsTabContent.setVisible(false);
        if (clicked == productsTabBtn) showProductsTab();
        else if (clicked == reviewsTabBtn) showReviewsTab();
        else if (clicked == eventsTabBtn) showEventsTab();
        else if (clicked == publicationsTabBtn) showPublicationsTab();
    }

    private void showProductsTab() {
        productsTabBtn.getStyleClass().add("active-tab");
        productsTabContent.setVisible(true);
    }

    private void showReviewsTab() {
        reviewsTabBtn.getStyleClass().add("active-tab");
        reviewsTabContent.setVisible(true);
    }

    private void showEventsTab() {
        eventsTabBtn.getStyleClass().add("active-tab");
        eventsTabContent.setVisible(true);
    }

    private void showPublicationsTab() {
        publicationsTabBtn.getStyleClass().add("active-tab");
        publicationsTabContent.setVisible(true);
    }

    @FXML private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ProductAdd.fxml"));
            Parent root = loader.load();
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de naviguer vers le formulaire d'ajout de produit.");
        }
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


    @FXML private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Connexion.fxml"));
            Parent root = loader.load();
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de déconnexion", "Impossible de se déconnecter.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refreshProfile() {
        loadCurrentUserData();
        loadProfileImage();
    }

    public void setUser(User user) {
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur fourni pour afficher le profil.");
            return;
        }
        this.currentUser = user;
        this.currentUserEmail = user.getEmail();

        loadProfileImage();
    }
    private void  displayCurrentUserInfo() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            adminBtn.setText(current.getFirstName() + " " + current.getLastName());
            adminEmailLabel.setText(current.getEmail());
        }
    }
}