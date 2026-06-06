package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Admin {
    final String username;
    final String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
