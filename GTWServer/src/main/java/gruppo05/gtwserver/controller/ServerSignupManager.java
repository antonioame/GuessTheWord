package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwserver.model.AdminId;
import gruppo05.gtwshared.controller.SignupManager;

/**
 *
 * @author francesco-vecchione
 */
public class ServerSignupManager implements SignupManager {

    @Override
    public void validateInfo(String username, String password) {
        DAO<Admin, AdminId> dao = new AdminDAO();
        
        dao.insert(new Admin(username, password));
    }
    
}
