package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public class AdminDAO implements DAO<Admin>{

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getString("username"), 
                rs.getString("password"));
    }
    
    @Override
    public Optional<Admin> selectById(Admin modelWithId) {
        Optional<Admin> result = Optional.empty();
        try(Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT * FROM admin WHERE username = ?")) {
            cmd.setString(1, modelWithId.getUsername());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapAdmin(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    
        return result;
    }
    
    @Override
    public List<Admin> selectAll() {
        List<Admin> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(
                        "SELECT * FROM admin")) {
            
            while(rs.next()) {
                result.add(mapAdmin(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void insert(Admin model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "INSERT INTO admin (username, password) VALUES (?,?)")) {
            cmd.setString(1, model.getUsername());
            cmd.setString(2, model.getPassword());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    }

    @Override
    public void update(Admin model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "UPDATE admin SET password = ? WHERE username = ?")) {
            cmd.setString(1, model.getPassword());
            cmd.setString(2, model.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(Admin modelWithId) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM admin WHERE username = ?")) {
            cmd.setString(1, modelWithId.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    }
}
