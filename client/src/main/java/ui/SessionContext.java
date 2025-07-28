package ui;

public class SessionContext {
    private String username;
    private String authToken;

    public void login(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }

    public void logout() {
        this.username = null;
        this.authToken = null;
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthToken() {
        return authToken;
    }
}
