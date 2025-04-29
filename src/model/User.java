package src.model;

import java.util.Objects;

public class User {
    private String username;
    private String passwordHash;
    private String displayName;
    private String contactInfo;
    private String permissions;

    public User() {
    }

    public User(String username, String passwordHash, String displayName, String contactInfo, String permissions) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.contactInfo = contactInfo;
        this.permissions = permissions;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(passwordHash, user.passwordHash) &&
                Objects.equals(displayName, user.displayName) &&
                Objects.equals(contactInfo, user.contactInfo) &&
                Objects.equals(permissions, user.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, passwordHash, displayName, contactInfo, permissions);
    }
}
