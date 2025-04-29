package models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents an application user with JavaFX properties for UI binding.
 */
public class User {
    private final IntegerProperty id;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty role;
    private final StringProperty type;
    private final StringProperty status;
    private final ObjectProperty<LocalDate> registrationDate;
    private final ObjectProperty<LocalDate> birthDate;
    private final StringProperty biography;
    private final StringProperty phone;
    private final StringProperty address;
    private final StringProperty city;
    private final StringProperty postalCode;
    private final StringProperty profileImagePath;
    private final StringProperty password;
    private final StringProperty permissions;

    /**
     * No-arg constructor initializes default values.
     */
    public User() {
        this.id = new SimpleIntegerProperty(0);
        this.firstName = new SimpleStringProperty("");
        this.lastName = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.role = new SimpleStringProperty("");
        this.type = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("");
        this.registrationDate = new SimpleObjectProperty<>(LocalDate.now());
        this.birthDate = new SimpleObjectProperty<>(null);
        this.biography = new SimpleStringProperty("");
        this.phone = new SimpleStringProperty("");
        this.address = new SimpleStringProperty("");
        this.city = new SimpleStringProperty("");
        this.postalCode = new SimpleStringProperty("");
        this.profileImagePath = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
        this.permissions = new SimpleStringProperty("");
    }

    /**
     * Constructor for existing users (with id).
     */
    public User(int id,
                String firstName,
                String lastName,
                String email,
                String role,
                String type,
                String status,
                LocalDate registrationDate) {
        this();
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setRole(role);
        setType(type);
        setStatus(status);
        setRegistrationDate(registrationDate);
    }

    /**
     * Constructor for existing users (with id and password).
     */
    public User(int id,
                String firstName,
                String lastName,
                String email,
                String password,
                String role,
                String type,
                String status,
                LocalDate registrationDate) {
        this(id, firstName, lastName, email, role, type, status, registrationDate);
        setPassword(password);
    }

    /**
     * Constructor for new users (without id).
     */
    public User(String firstName,
                String lastName,
                String email,
                String role,
                String type,
                String status,
                LocalDate registrationDate) {
        this();
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setRole(role);
        setType(type);
        setStatus(status);
        setRegistrationDate(registrationDate);
    }

    /**
     * Constructor for new users (without id, with password).
     */
    public User(String firstName,
                String lastName,
                String email,
                String password,
                String role,
                String type,
                String status,
                LocalDate registrationDate) {
        this(firstName, lastName, email, role, type, status, registrationDate);
        setPassword(password);
    }

    /**
     * Utility to parse date strings (dd/MM/yyyy).
     */
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, fmt);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return null;
        }
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // FirstName
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public StringProperty firstNameProperty() { return firstName; }

    // LastName
    public String getLastName() { return lastName.get(); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public StringProperty lastNameProperty() { return lastName; }

    // Combined name for backward compatibility
    public String getName() { return getFirstName() + " " + getLastName(); }
    public void setName(String name) {
        String[] parts = name.split(" ", 2);
        setFirstName(parts[0]);
        if (parts.length > 1) setLastName(parts[1]);
    }
    public StringProperty nameProperty() {
        return new SimpleStringProperty(getName());
    }

    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    // Role
    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
    public StringProperty roleProperty() { return role; }

    // Type
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }

    // Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    // RegistrationDate
    public LocalDate getRegistrationDate() { return registrationDate.get(); }
    public void setRegistrationDate(LocalDate date) { this.registrationDate.set(date); }
    public ObjectProperty<LocalDate> registrationDateProperty() { return registrationDate; }
    public String getRegistrationDateString() {
        LocalDate d = getRegistrationDate();
        return (d == null) ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    public void setRegistrationDate(String dateStr) { setRegistrationDate(parseDate(dateStr)); }

    // BirthDate
    public LocalDate getBirthDate() { return birthDate.get(); }
    public void setBirthDate(LocalDate date) { this.birthDate.set(date); }
    public ObjectProperty<LocalDate> birthDateProperty() { return birthDate; }
    public String getBirthDateString() {
        LocalDate d = getBirthDate();
        return (d == null) ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    public void setBirthDate(String dateStr) { setBirthDate(parseDate(dateStr)); }

    // Biography, Phone, Address, City, PostalCode, ProfileImagePath
    public String getBiography() { return biography.get(); }
    public void setBiography(String bio) { this.biography.set(bio); }
    public StringProperty biographyProperty() { return biography; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public StringProperty phoneProperty() { return phone; }

    public String getAddress() { return address.get(); }
    public void setAddress(String addr) { this.address.set(addr); }
    public StringProperty addressProperty() { return address; }

    public String getCity() { return city.get(); }
    public void setCity(String city) { this.city.set(city); }
    public StringProperty cityProperty() { return city; }

    public String getPostalCode() { return postalCode.get(); }
    public void setPostalCode(String code) { this.postalCode.set(code); }
    public StringProperty postalCodeProperty() { return postalCode; }

    public String getProfileImagePath() { return profileImagePath.get(); }
    public void setProfileImagePath(String path) { this.profileImagePath.set(path); }
    public StringProperty profileImagePathProperty() { return profileImagePath; }

    // Password
    public String getPassword() { return password.get(); }
    public void setPassword(String pwd) { this.password.set(pwd); }
    public StringProperty passwordProperty() { return password; }

    // Permissions
    public String getPermissions() { return permissions.get(); }
    public void setPermissions(String perms) { this.permissions.set(perms); }
    public StringProperty permissionsProperty() { return permissions; }

    /**
     * Convenience for TableView display.
     */
    public String getUserInfo() {
        return getName() + (getEmail().isEmpty() ? "" : "\n" + getEmail());
    }
}
