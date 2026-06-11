package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.SignupManager;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ServerSignupManager implements SignupManager {

    private Runnable onSuccessCallback;

    public void setOnSuccessCallback(Runnable r) {
        this.onSuccessCallback = r;
    }
    
    @Override
    public void registerInfo(String username, String password) {
        AdminDAO dao = new ConcreteAdminDAO();
        
        if(dao.selectById(Optional.of(username)).isPresent()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "La registrazione non è andata a buon fine");
            alert.showAndWait();
            return;
        } 
        
        dao.insert(new Admin(username, password));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Il nuovo admin è stato registrato correttamente");
        alert.showAndWait();
        
        if (onSuccessCallback != null) {
            Platform.runLater(onSuccessCallback);
        }
    }
}
