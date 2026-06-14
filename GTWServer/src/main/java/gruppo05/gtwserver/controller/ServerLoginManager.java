package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.LoginManager;
import gruppo05.gtwshared.utility.SecurityUtils;

import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Gestore dell'autenticazione lato Server (Amministratore).
 * Implementa l'interfaccia LoginManager per validare le credenziali dell'amministratore
 * tramite accesso al database locale (AdminDAO) ed eseguire un callback in caso di successo o fallimento.
 */
public class ServerLoginManager implements LoginManager {

    private Runnable onSuccessCallback;
    private Runnable onFailureCallback;

    @Override
    public void setOnSuccessCallback(Runnable r) {
        this.onSuccessCallback = r;
    }

    @Override
    public void setOnFailureCallback(Runnable r) {
        this.onFailureCallback = r;
    }

    @Override
    public void validateInfo(String username, String password) {
        AdminDAO dao = new ConcreteAdminDAO();
        
        Optional<Admin> o = dao.selectById(Optional.of(username));
        
        if (!o.isPresent()) {
            System.err.println("[Login Error] Utente non trovato: " + username);
            
            if (onFailureCallback != null) {
                Platform.runLater(onFailureCallback);
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR, "L'utente non è registrato");
            alert.showAndWait();
            return;
        } 
        
        String hashedPassword = SecurityUtils.hashPassword(password);
        if (!o.get().getPassword().equals(hashedPassword)) {
            System.err.println("[Login Error] Password errata per utente: " + username);
            
            if (onFailureCallback != null) {
                Platform.runLater(onFailureCallback);
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR, "Password errata");
            alert.showAndWait();
            return;
        }

        if (onSuccessCallback != null) {
            Platform.runLater(onSuccessCallback);
        }
    }
}