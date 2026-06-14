package gruppo05.gtwshared.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * FXML Controller class per la gestione del Login (condiviso tra Client e Server).
 */
public class LoginViewController implements Initializable {

    @FXML private Pane outerContainer;
    @FXML private TextField txfUsername;
    @FXML private TextField txfPswd;
    @FXML private Hyperlink linkToLogin;
    @FXML private Button btnExit;
    @FXML private Button btnConfirm;

    private LoginManager loginManager;  
    private SignupManager signupManager;
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Binding: il pulsante si disabilita se i campi sono vuoti O se l'operazione è in corso
        btnConfirm.disableProperty().bind(
            isProcessing
            .or(txfUsername.textProperty().isEmpty())
            .or(txfPswd.textProperty().isEmpty())
        );
    }    

    @FXML
    private void onConfirm(ActionEvent event) throws IOException {
        isProcessing.set(true); // Congela l'interfaccia utente durante la verifica
        loginManager.validateInfo(txfUsername.getText(), txfPswd.getText());
    }
    
    /**
     * Sblocca l'interfaccia grafica riabilitando i controlli 
     * (in caso di errore o problemi di rete).
     */
    public void resetLoginButton() {
        Platform.runLater(() -> {
            isProcessing.set(false); // Sblocca l'interfaccia eliminando il congelamento
        });
    }

    @FXML
    private void switchToSignup(ActionEvent event) throws IOException {
        Stage stage = (Stage) outerContainer.getScene().getWindow();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SignupView.fxml"));
        Parent root = loader.load();
        
        SignupViewController ctrl = (SignupViewController) loader.getController();
        ctrl.setSignupManager(signupManager);
        ctrl.setLoginManager(loginManager);
        
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    @FXML
    private void exitApp(ActionEvent event) {
        Platform.exit();
    }
    
    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
        
        if (this.loginManager != null) {
            this.loginManager.setOnFailureCallback(this::resetLoginButton);
        }
    }
    
    public void setSignupManager(SignupManager signupManager) {
        this.signupManager = signupManager;
    }
}