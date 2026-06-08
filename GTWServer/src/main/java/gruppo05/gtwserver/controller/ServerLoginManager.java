package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.LoginManager;
import java.util.Optional;
import javafx.scene.control.Alert;

/**
 *
 * @author francesco-vecchione
 */
public class ServerLoginManager implements LoginManager {

    @Override
    public void validateInfo(String username, String password) {
        AdminDAO dao = new ConcreteAdminDAO();
        
        Optional<Admin> o = dao.selectById(Optional.of(username));
        
        if(!o.isPresent()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "L'utente non è registrato");
            alert.showAndWait();
            return;
        } 
        
        if(!o.get().getPassword().equals(password)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Password non corretta");
            alert.showAndWait();
            return;
        }
    }
    
}
