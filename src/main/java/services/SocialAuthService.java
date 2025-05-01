package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SocialAuthService {
    private static final String GOOGLE_CLIENT_SECRET_FILE = "src/main/resources/config/google_client_secret.json";
    private static final String REDIRECT_URI = "http://localhost:8888/Callback"; // Using localhost
    
    private GoogleAuthorizationCodeFlow googleFlow;
    
    public SocialAuthService() {
        initializeGoogleAuth();
    }
    
    private void initializeGoogleAuth() {
        try {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new FileReader(GOOGLE_CLIENT_SECRET_FILE)
            );
            
            googleFlow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                Arrays.asList("email", "profile")
            )
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
            .setAccessType("offline")
            .build();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getGoogleAuthUrl() {
        return googleFlow.newAuthorizationUrl()
            .setRedirectUri(REDIRECT_URI)
            .setAccessType("offline")
            .build();
    }
    
    public Map<String, String> handleGoogleCallback(String code) {
        try {
            GoogleTokenResponse response = googleFlow.newTokenRequest(code)
                .setRedirectUri(REDIRECT_URI)
                .execute();
                
            // Get user info from Google
            String userInfo = new String(Files.readAllBytes(Paths.get("user_info.json")));
            JsonObject userInfoJson = JsonParser.parseString(userInfo).getAsJsonObject();
            
            Map<String, String> userData = new HashMap<>();
            userData.put("email", userInfoJson.get("email").getAsString());
            userData.put("name", userInfoJson.get("name").getAsString());
            userData.put("picture", userInfoJson.get("picture").getAsString());
            
            return userData;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 