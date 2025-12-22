package org.lab1.json;

public class GoogleAuthResponse {
    private String authUrl;
    private String state;
    private String message;

    public GoogleAuthResponse() {  }

    public GoogleAuthResponse(String authUrl, String state, String message) {
        this.authUrl = authUrl;
        this.state = state;
        this.message = message;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
