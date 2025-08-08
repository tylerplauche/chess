package model;

public record AuthData(String authToken, String username) {

    @Override
    public String authToken() {
        return authToken;
    }
}
