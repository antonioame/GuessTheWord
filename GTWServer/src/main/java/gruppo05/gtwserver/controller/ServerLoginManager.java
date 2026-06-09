package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.controller.LoginManager;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Gestore dell'autenticazione lato Server (Amministratore).
 * Implementa l'interfaccia LoginManager per validare le credenziali dell'amministratore
 * tramite accesso al database locale (AdminDAO) ed eseguire un callback in caso di successo.
 */
public class ServerLoginManager implements LoginManager {

    private Runnable onSuccessCallback;

    /**
     * @brief Imposta il callback da eseguire in caso di autenticazione riuscita.
     * @param[in] r Callback (Runnable) di successo.
     */
    public void setOnSuccessCallback(Runnable r) {
        this.onSuccessCallback = r;
    }

    /**
     * @brief Valida le credenziali dell'utente amministratore interrogando il database locale.
     * Mostra un messaggio di errore in caso di fallimento o esegue il callback in caso di successo.
     * @param[in] username Nome utente inserito.
     * @param[in] password Password inserita.
     */
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

        if (onSuccessCallback != null) {
            Platform.runLater(onSuccessCallback);
        }
    }
    
}
