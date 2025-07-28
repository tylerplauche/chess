package ui;

import com.google.gson.Gson;
import model.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ServerFacade {
    private final String baseUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RegisterResult register(String username, String password, String email) throws IOException {
        RegisterRequest request = new RegisterRequest(username, password, email);
        return makePostRequest("/user", request, RegisterResult.class);
    }

    public LoginResult login(String username, String password) throws IOException {
        LoginRequest request = new LoginRequest(username, password);
        return makePostRequest("/session", request, LoginResult.class);
    }

    private <T> T makePostRequest(String path, Object request, Class<T> responseClass) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        // Write JSON body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = gson.toJson(request).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int status = connection.getResponseCode();

        InputStream responseStream = (status >= 200 && status < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (InputStreamReader isr = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            // If error stream, you might want to throw exception with message
            if (status >= 400) {
                String errorMsg = new BufferedReader(isr).lines().reduce("", (acc, line) -> acc + line);
                throw new IOException("HTTP " + status + ": " + errorMsg);
            }
            return gson.fromJson(isr, responseClass);
        }
    }
}
