package com.fittrack.app;

public class RegistrationContext {
    private static RegistrationContext instance;
    private String email;
    private String password;

    private RegistrationContext() {}

    public static RegistrationContext getInstance() {
        if (instance == null) {
            instance = new RegistrationContext();
        }
        return instance;
    }

    public void setCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void clear() {
        email = null;
        password = null;
    }
}
