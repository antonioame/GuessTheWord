package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Admin {
    private final AdminId id;
    private final String password;

    public Admin(String username, String password) {
        this.id = new AdminId(username);
        this.password = password;
    }

    public String getUsername() {
        return id.getUsername();
    }

    public String getPassword() {
        return password;
    }
}
