package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.SignupManager;
import gruppo05.gtwshared.utility.SecurityUtils;

public class ServerSignupManager implements SignupManager {

    @Override
    public void validateInfo(String username, String password) {
        AdminDAO dao = new ConcreteAdminDAO();
        
        String hashedPassword = SecurityUtils.hashPassword(password);
        dao.insert(new Admin(username, hashedPassword));
    }
}
