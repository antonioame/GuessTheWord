package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.SignupManager;
import javafx.application.Platform;

public class ServerSignupManager implements SignupManager {

    private Runnable onSuccessCallback;

    public void setOnSuccessCallback(Runnable r) {
        this.onSuccessCallback = r;
    }
    
    @Override
    public void registerInfo(String username, String password) {
        AdminDAO dao = new ConcreteAdminDAO();
        
        dao.insert(new Admin(username, password));
        
        if (onSuccessCallback != null) {
            Platform.runLater(onSuccessCallback);
        }
    }
}
