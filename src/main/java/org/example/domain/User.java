package org.example.domain;

public class User extends JsonObject {
    private int id;
    private String username;
    private String password;
    private String salt;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        final User user = new User();

        public UserBuilder id(int id) {
            user.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            user.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            user.password = password;
            return this;
        }

        public UserBuilder salt(String salt) {
            user.salt = salt;
            return this;
        }

        public User build() {
            return user;
        }
    }
}
