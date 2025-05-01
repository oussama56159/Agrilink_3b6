package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import models.User;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleAuthService {
    private static final Logger LOGGER = Logger.getLogger(GoogleAuthService.class.getName());

    private static final String APPLICATION_NAME = "AgriLink";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");
    private static final String CREDENTIALS_FILE_PATH = "/config/google_client_secret.json";
    private static final int[] POTENTIAL_PORTS = {8888, 8889, 8890, 8891, 8892};
    private static final int MAX_RETRIES = 3;

    private final UserService userService;

    public GoogleAuthService() {
        this.userService = new UserServiceImpl();
    }

    /**
     * Trouve un port disponible pour l'authentification
     * @return Un port disponible, ou -1 si aucun n'est trouvé
     */
    private int findAvailablePort() {
        for (int port : POTENTIAL_PORTS) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                LOGGER.info("Port " + port + " is already in use");
            }
        }
        return -1;
    }

    /**
     * Crée un objet Credential autorisé avec gestion dynamique des ports.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Charger le fichier de secrets client
        InputStream in = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Fichier de ressource introuvable : " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Trouver un port disponible
        int availablePort = findAvailablePort();
        if (availablePort == -1) {
            throw new IOException("Aucun port disponible pour l'authentification");
        }

        // Construction du flux d'autorisation
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        // Utiliser le port disponible
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(availablePort)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Authentifie l'utilisateur avec Google et récupère ses informations
     * @return L'utilisateur authentifié ou null si échec
     */
    public User authenticateWithGoogle() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                // Nettoyer les tokens existants
                cleanupTokens();

                // Initialisation du transport HTTP
                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

                // Obtention des credentials
                Credential credential = getCredentials(HTTP_TRANSPORT);

                // Création du service OAuth2
                Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();

                // Récupération des informations de l'utilisateur
                Userinfo userInfo = oauth2.userinfo().get().execute();

                // Vérifier si l'email existe déjà dans la base de données
                User existingUser = userService.findByEmail(userInfo.getEmail());

                if (existingUser != null) {
                    return existingUser;
                } else {
                    return createNewUser(userInfo);
                }

            } catch (BindException e) {
                LOGGER.log(Level.WARNING, "Port binding error on attempt " + (attempt + 1), e);
                try {
                    Thread.sleep(1000); // Attendre avant de réessayer
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (GeneralSecurityException | IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'authentification Google", e);
                break;
            }
        }
        return null;
    }

    /**
     * Nettoie les tokens d'authentification existants
     */
    private void cleanupTokens() {
        File tokensDir = new File(TOKENS_DIRECTORY_PATH);
        if (tokensDir.exists()) {
            File[] files = tokensDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        LOGGER.warning("Impossible de supprimer le fichier de token : " + file.getName());
                    }
                }
            }
        }
    }

    /**
     * Crée un nouvel utilisateur à partir des informations Google
     * @param userInfo Informations de l'utilisateur Google
     * @return L'utilisateur nouvellement créé
     */
    private User createNewUser(Userinfo userInfo) {
        User newUser = new User();

        // Diviser le nom complet en prénom et nom
        String fullName = userInfo.getName();
        String[] nameParts = fullName.split(" ", 2);

        newUser.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
        newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        newUser.setEmail(userInfo.getEmail());

        // Valeurs par défaut
        newUser.setRole("utilisateur"); // Rôle par défaut
        newUser.setType("Acheteur"); // Type d'authentification
        newUser.setStatus("Actif"); // Statut par défaut
        newUser.setRegistrationDate(java.time.LocalDate.now()); // Date d'inscription

        // Générer un mot de passe aléatoire
        String randomPassword = generateRandomPassword();
        newUser.setPassword(randomPassword);

        try {
            // Ajouter l'utilisateur à la base de données
            userService.Create(newUser);

            // Récupérer l'utilisateur complet avec son ID
            return userService.findByEmail(userInfo.getEmail());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new user from Google authentication", e);
            return null;
        }
    }

    /**
     * Génère un mot de passe aléatoire pour les utilisateurs Google
     * @return Un mot de passe aléatoire
     */
    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
} 