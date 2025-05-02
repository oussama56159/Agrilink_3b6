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
import javafx.scene.text.Text;
import javafx.util.Callback;
import models.User;
import services.UserService;
import services.UserServiceImpl;
import utils.SessionManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.time.format.DateTimeFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Desktop;

public class UserManagementController implements Initializable {
    @FXML private Label adminEmailLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button forumBtn;

    @FXML
    private Button usersBtn;

    @FXML
    private Button productsBtn;

    @FXML
    private Button statisticsBtn;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button adminBtn;

    @FXML
    private Button listTab;

    @FXML
    private Button detailsTab;

    @FXML
    private Button modifyTab;

    @FXML
    private Button rolesTab;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> roleFilter;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> dateFilter;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> userColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, String> dateColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private ComboBox<Integer> pageSize;

    @FXML
    private Button prevPage;

    @FXML
    private Button nextPage;

    @FXML
    private Label paginationText;

    private UserService userService;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private ObservableList<User> filteredUsers = FXCollections.observableArrayList();
    private int currentPage = 1;
    private ObservableList<User> currentPageUsers = FXCollections.observableArrayList();
    // Display current admin name and email

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the user service
        try {
            userService = new UserServiceImpl();
            System.out.println("UserService initialized successfully in UserManagementController");
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données. Veuillez contacter le support.");
        }
         displayCurrentAdminInfo();
        // Initialize filter comboboxes
        if (roleFilter != null) {
            roleFilter.setItems(FXCollections.observableArrayList("Tous les rôles", "Administrateur","utilisateur"));
            roleFilter.setValue("Tous les rôles");
            roleFilter.setOnAction(event -> applyFilters());
        }

        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "Actif", "En attente", "Inactif"));
            statusFilter.setValue("Tous les statuts");
            statusFilter.setOnAction(event -> applyFilters());
        }

        if (typeFilter != null) {
            typeFilter.setItems(FXCollections.observableArrayList("Tous les types", "Acheteur", "Agriculteur", "Grossiste"));
            typeFilter.setValue("Tous les types");
            typeFilter.setOnAction(event -> applyFilters());
        }

        if (dateFilter != null) {
            dateFilter.setItems(FXCollections.observableArrayList("Toutes les dates", "Aujourd'hui", "Cette semaine", "Ce mois", "Cette année"));
            dateFilter.setValue("Toutes les dates");
            dateFilter.setOnAction(event -> applyFilters());
        }

        // Initialize page size combobox
        if (pageSize != null) {
            pageSize.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
            pageSize.setValue(10);
            pageSize.setOnAction(event -> applyFilters());
        }

        // Set up search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }

        // Set up table columns
        if (usersTable != null) {
            if (userColumn != null) {userColumn.setCellValueFactory(cellData -> { User user = cellData.getValue();
                String fullName = user.getFirstName() + " " + user.getLastName();
                return new SimpleStringProperty(fullName);
            });
            }            if (roleColumn != null) roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
            if (typeColumn != null) typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

            // Custom cell factory for status column to apply appropriate styles
            if (statusColumn != null) {
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
                statusColumn.setCellFactory(column -> {
                    return new TableCell<User, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setText(null);
                                setStyle("");
                                getStyleClass().removeAll("status-active", "status-pending", "status-inactive");
                            } else {
                                setText(item);

                                // Apply appropriate style class based on status
                                getStyleClass().removeAll("status-active", "status-pending", "status-inactive");
                                if (item.equalsIgnoreCase("Actif")) {
                                    getStyleClass().add("status-active");
                                } else if (item.equalsIgnoreCase("En attente")) {
                                    getStyleClass().add("status-pending");
                                } else if (item.equalsIgnoreCase("Bloqué")) {
                                    getStyleClass().add("status-inactive");
                                }
                            }
                        }
                    };
                });
            }

            // Custom cell factory for type column to apply appropriate styles
            if (typeColumn != null) {
                typeColumn.setCellFactory(column -> {
                    return new TableCell<User, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setText(null);
                            } else {
                                setText(item);

                                // Apply different styles based on type
                                if (item.equals("Agriculteur")) {
                                    setStyle("-fx-text-fill: #2e6e4a; -fx-font-weight: bold;");
                                } else if (item.equals("Acheteur")) {
                                    setStyle("-fx-text-fill: #b38600; -fx-font-weight: bold;");
                                } else if (item.equals("Grossiste")) {
                                    setStyle("-fx-text-fill: #1a73e8; -fx-font-weight: bold;");
                                } else {
                                    setStyle("-fx-text-fill: #666666;");
                                }
                            }
                        }
                    };
                });
            }

            if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDateString"));

            // Set up actions column with menu button
            setupActionsColumn();

            // Load real data from database
            loadUsersFromDatabase();
        }
    }

    private void displayCurrentAdminInfo() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            adminBtn.setText(current.getFirstName() + " " + current.getLastName());
            adminEmailLabel.setText(current.getEmail());
        }
    }

    private void loadUsersFromDatabase() {
        try {
            allUsers.clear();
            allUsers.addAll(userService.DisplayAll());
            applyFilters();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de données", "Impossible de charger les utilisateurs.");
        }
    }

    private void applyFilters() {
        filteredUsers.clear();
        currentPage = 1; // Reset to first page when filters change

        String roleVal = roleFilter.getValue();
        String statusVal = statusFilter.getValue();
        String typeVal = typeFilter.getValue();
        String dateVal = dateFilter.getValue();
        String search = searchField.getText().trim().toLowerCase();

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate yearEnd = yearStart.plusYears(1).minusDays(1);

        for (User u : allUsers) {
            boolean matchesRole = "Tous les rôles".equals(roleVal) || u.getRole().equals(roleVal);
            boolean matchesStatus = "Tous les statuts".equals(statusVal) || u.getStatus().equals(statusVal);
            boolean matchesType = "Tous les types".equals(typeVal) || u.getType().equals(typeVal);

            LocalDate reg = u.getRegistrationDate();
            boolean matchesDate = true;
            switch (dateVal) {
                case "Aujourd'hui": matchesDate = reg.isEqual(today); break;
                case "Cette semaine": matchesDate = !reg.isBefore(weekStart) && !reg.isAfter(weekEnd); break;
                case "Ce mois": matchesDate = !reg.isBefore(monthStart) && !reg.isAfter(monthEnd); break;
                case "Cette année": matchesDate = !reg.isBefore(yearStart) && !reg.isAfter(yearEnd); break;
                default: break;
            }

            boolean matchesSearch = search.isEmpty()
                    || u.getFirstName().toLowerCase().contains(search)
                    || u.getLastName().toLowerCase().contains(search)
                    || u.getEmail().toLowerCase().contains(search);

            if (matchesRole && matchesStatus && matchesType && matchesDate && matchesSearch) {
                filteredUsers.add(u);
            }
        }

        // Apply pagination
        applyPagination();
    }

    private void applyPagination() {
        int pageSize = this.pageSize.getValue();
        int totalItems = filteredUsers.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        // Ensure current page is within bounds
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages && totalPages > 0) currentPage = totalPages;
        
        // Calculate start and end indices for current page
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        
        // Update the table with current page items
        currentPageUsers.clear();
        if (totalItems > 0) {
            currentPageUsers.addAll(filteredUsers.subList(fromIndex, toIndex));
        }
        usersTable.setItems(currentPageUsers);
        
        // Update pagination controls
        prevPage.setDisable(currentPage == 1);
        nextPage.setDisable(currentPage >= totalPages || totalPages == 0);
        
        // Update pagination text
        String paginationInfo;
        if (totalItems == 0) {
            paginationInfo = "Aucun utilisateur trouvé";
        } else {
            paginationInfo = String.format("Affichage de %d à %d sur %d utilisateurs", 
                fromIndex + 1, toIndex, totalItems);
        }
        paginationText.setText(paginationInfo);
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
                } else if (clickedButton == statisticsBtn) {
                    viewName = "Statistics";
                } else if (clickedButton == settingsBtn) {
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

    private void setupActionsColumn() {
        if (actionsColumn == null) return;

        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button menuButton = new Button("⋮");

                    {
                        menuButton.getStyleClass().add("action-button");
                        menuButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            showActionsMenu(user, menuButton);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(menuButton);
                        }
                    }
                };
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void showActionsMenu(User user, Button button) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifier");
        MenuItem deleteItem = new MenuItem("Supprimer");
        MenuItem profileItem = new MenuItem("profile");
        MenuItem bloquerItem = new MenuItem("Bloquer");
        editItem.setOnAction(event -> {
            try {
                // Get the scene from the button
                Scene currentScene = button.getScene();
                if (currentScene != null) {
                    // Navigate to user edit form
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/UserModifyView.fxml"));
                    Parent root = loader.load();
                    UserModifyController controller = loader.getController();
                    controller.setUserForEditing(user);

                    // Replace scene content
                    currentScene.setRoot(root);
                    System.out.println("Editing user: " + user.getName());
                } else {
                    System.out.println("Error: Current scene is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error navigating to user edit form: " + e.getMessage());
            }

        });

        deleteItem.setOnAction(event -> {
            try {
                // Get the currently logged-in user information
                User currentLoggedInUser = SessionManager.getInstance().getCurrentUser();

                // Debug statements
                System.out.println("Current logged-in user: " +
                        (currentLoggedInUser != null ? currentLoggedInUser.getEmail() : "null"));
                System.out.println("Attempting to delete user: " + user.getEmail());

                if (currentLoggedInUser != null) {
                    System.out.println("Current user role: " + currentLoggedInUser.getRole());

                    // Compare emails instead of IDs for more reliable comparison
                    boolean isSameUser = currentLoggedInUser.getEmail().equalsIgnoreCase(user.getEmail());
                    boolean isAdmin = "Administrateur".equals(currentLoggedInUser.getRole());

                    System.out.println("Is same user: " + isSameUser);
                    System.out.println("Is admin: " + isAdmin);

                    // Check if the user is trying to delete their own account and is an administrator
                    if (isSameUser && isAdmin) {
                        showAlert(Alert.AlertType.ERROR, "Action refusée",
                                "Les administrateurs ne peuvent pas supprimer leur propre compte. C'est une mesure de sécurité.");
                        return;
                    }
                }

                // Confirm deletion
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation de suppression");
                alert.setHeaderText("Supprimer l'utilisateur");
                alert.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + user.getName() + "?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            // Delete user from database
                            userService.delete(user);

                            // Reload users from database
                            loadUsersFromDatabase();

                            System.out.println("User deleted: " + user.getName());
                        } catch (Exception e) {
                            System.err.println("Error deleting user: " + e.getMessage());
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                                    "Impossible de supprimer l'utilisateur: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error in delete action: " + e.getMessage());
                e.printStackTrace();
            }
        });

        profileItem.setOnAction(event -> { try {
            // Get the scene from the button
            Scene currentScene = button.getScene();
            if (currentScene != null) {
                // Navigate to user edit form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/UserProfileView.fxml"));
                Parent root = loader.load();
                UserProfileController controller = loader.getController();
                controller.setUser(user);

                // Replace scene content
                currentScene.setRoot(root);
                System.out.println("Editing user: " + user.getName());
            } else {
                System.out.println("Error: Current scene is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error navigating to user edit form: " + e.getMessage());
        }

        });
        bloquerItem.setOnAction(e -> {
            // Check if user is an Administrator
            if ("Administrateur".equals(user.getRole())) {
                showAlert(Alert.AlertType.WARNING, "Action non autorisée", "Les utilisateurs avec le rôle Administrateur ne peuvent pas être bloqués.");
                return;
            }
            // Check if user is already blocked
            if ("Bloqué".equals(user.getStatus())) {
                // Unblock the user
                user.setStatus("Actif");
                try {
                    userService.update(user);
                    showAlert(Alert.AlertType.INFORMATION, "Utilisateur débloqué", "L'utilisateur a été débloqué avec succès.");
                } catch (Exception ex) {
                    System.err.println("Error unblocking user: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de débloquer l'utilisateur: " + ex.getMessage());
                }
            } else {
                // Block the user
                user.setStatus("Bloqué");
                try {
                    userService.update(user);
                    showAlert(Alert.AlertType.INFORMATION, "Utilisateur bloqué", "L'utilisateur a été bloqué avec succès.");
                } catch (Exception ex) {
                    System.err.println("Error blocking user: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de bloquer l'utilisateur: " + ex.getMessage());
                }
            }
            refreshUserList();
        });

        contextMenu.getItems().addAll(editItem, deleteItem,profileItem, bloquerItem);
        contextMenu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        try {
            // Create dialog to choose export type
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Type d'export");
            dialog.setHeaderText("Choisissez le type d'export");
            dialog.setContentText("Quel type de liste souhaitez-vous exporter ?");

            ButtonType detailedButton = new ButtonType("Liste détaillée");
            ButtonType simplifiedButton = new ButtonType("Liste simplifiée");
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialog.getButtonTypes().setAll(detailedButton, simplifiedButton, cancelButton);

            dialog.showAndWait().ifPresent(response -> {
                if (response != cancelButton) {
                    boolean isDetailed = (response == detailedButton);
                    exportUserList(event, isDetailed);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Une erreur est survenue lors de l'export:\n" + e.getMessage());
        }
    }

    private void exportUserList(ActionEvent event, boolean isDetailed) {
        try {
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte (*.txt)", "*.txt")
            );
            
            // Show save file dialog
            File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
            
            if (file != null) {
                try (FileOutputStream output = new FileOutputStream(file)) {
                    StringBuilder content = new StringBuilder();
                    
                    // Add title
                    content.append("Liste des Utilisateurs - AgriLink\n");
                    content.append("Date d'export: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
                    
                    if (isDetailed) {
                        // Detailed export
                        for (User user : filteredUsers) {
                            content.append("=".repeat(80)).append("\n");
                            content.append("Informations détaillées de l'utilisateur\n");
                            content.append("-".repeat(40)).append("\n");
                            content.append(String.format("ID: %d\n", user.getId()));
                            content.append(String.format("Nom complet: %s %s\n", user.getFirstName(), user.getLastName()));
                            content.append(String.format("Email: %s\n", user.getEmail()));
                            content.append(String.format("Rôle: %s\n", user.getRole()));
                            content.append(String.format("Type: %s\n", user.getType()));
                            content.append(String.format("Statut: %s\n", user.getStatus()));
                            content.append(String.format("Date d'inscription: %s\n", user.getRegistrationDateString()));
                            content.append(String.format("Date de naissance: %s\n", user.getBirthDateString()));
                            content.append(String.format("Téléphone: %s\n", user.getPhone() != null ? user.getPhone() : "N/A"));
                            content.append(String.format("Adresse: %s\n", user.getAddress() != null ? user.getAddress() : "N/A"));
                            content.append(String.format("Ville: %s\n", user.getCity() != null ? user.getCity() : "N/A"));
                            content.append(String.format("Code postal: %s\n", user.getPostalCode() != null ? user.getPostalCode() : "N/A"));
                            content.append(String.format("Biographie: %s\n", user.getBiography() != null ? user.getBiography() : "N/A"));
                            content.append("\n");
                        }
                    } else {
                        // Simplified export (current format)
                        content.append(String.format("%-30s %-40s %-15s %-15s %-15s %-20s\n",
                            "Nom", "Email", "Rôle", "Type", "Statut", "Date d'inscription"));
                        content.append("-".repeat(135)).append("\n");
                        
                        for (User user : filteredUsers) {
                            String name = user.getFirstName() + " " + user.getLastName();
                            
                            content.append(String.format("%-30s %-40s %-15s %-15s %-15s %-20s\n",
                                name,
                                user.getEmail(),
                                user.getRole(),
                                user.getType(),
                                user.getStatus(),
                                user.getRegistrationDateString()));
                        }
                    }
                    
                    // Write content to file
                    output.write(content.toString().getBytes());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Export réussi", 
                             "Le fichier a été créé avec succès à l'emplacement:\n" + file.getAbsolutePath());
                    
                    // Open the file
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", 
                     "Une erreur est survenue lors de l'export du fichier:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        try {
            // Get the current scene from the event source
            Scene currentScene = ((Node) event.getSource()).getScene();
            if (currentScene != null) {
                // Navigate to user add form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/UserAdd.fxml"));
                Parent root = loader.load();

                // Replace scene content
                currentScene.setRoot(root);
                System.out.println("Navigated to user add form");
            } else {
                System.out.println("Error: Current scene is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error navigating to user add form: " + e.getMessage());
        }
    }

    @FXML
    private void handlePagination(ActionEvent event) {
        if (event.getSource() == prevPage && currentPage > 1) {
            currentPage--;
        } else if (event.getSource() == nextPage) {
            currentPage++;
        } else if (event.getSource() == pageSize) {
            currentPage = 1; // Reset to first page when page size changes
        }
        applyPagination();
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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to refresh the user list (can be called from other controllers)
    public void refreshUserList() {
        try {
            loadUsersFromDatabase();
            applyFilters();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rafraîchir la liste des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}