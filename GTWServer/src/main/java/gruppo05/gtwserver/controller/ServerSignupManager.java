package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.SignupViewController;
import gruppo05.gtwshared.controller.SignupManager;
import gruppo05.gtwshared.utility.SecurityUtils;
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
        
        // Controllo specifico per username esistente
        if(dao.selectById(Optional.of(username)).isPresent()) {
            System.err.println("[Signup Error] Tentativo di registrazione fallito: " + username + " già esistente");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Errore: Username già in uso");
                alert.showAndWait();
                
                // Sblocco manuale del pulsante nell'interfaccia (se l'istanza esiste)
                if (SignupViewController.instance != null) {
                    SignupViewController.instance.resetSignupButton();
                }
            });
            return;
        } 
        
        String hashedPassword = SecurityUtils.hashPassword(password);
        dao.insert(new Admin(username, hashedPassword));
        System.out.println("Admin aggiunto con successo!");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Il nuovo admin è stato registrato correttamente");
            alert.showAndWait();
        });
        
        if (onSuccessCallback != null) {
            Platform.runLater(onSuccessCallback);
        }
    }
}