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

public class UserManagementController implements Initializable {
    @FXML private Label adminEmailLabel;

    @FXML
    private Button dashboardBtn;

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

    private UserService userService;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private ObservableList<User> filteredUsers = FXCollections.observableArrayList();
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
                                } else if (item.equalsIgnoreCase("Inactif")) {
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

        usersTable.setItems(filteredUsers);


        // Apply pagination if needed
        applyPagination();
    }

    private void applyPagination() {
        // This would implement pagination logic based on the pageSize value
        // For simplicity, we're just showing all filtered results for now
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();

            try {
                String viewName = "";

                if (clickedButton == dashboardBtn) {
                    viewName = "Dashboard";
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

        contextMenu.getItems().addAll(editItem, deleteItem,profileItem);
        contextMenu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        System.out.println("Exporting user data");

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
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            if (button.getText().equals("<")) {
                System.out.println("Previous page");
                // Implement previous page logic
            } else {
                System.out.println("Next page");
                // Implement next page logic
            }
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