package gruppo05.gtwshared.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author francesco-vecchione
 */
public class SignupViewController implements Initializable {

    @FXML private Pane outerContainer;
    @FXML private TextField txfUsername;
    @FXML private TextField txfPswd;
    @FXML private TextField txfPswdConfirm;
    @FXML private Hyperlink linkToLogin;
    @FXML private Button btnExit;
    @FXML private Button btnConfirm;
    
    private SignupManager signupManager;
    private LoginManager loginManager;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupButtonBinding();
    }    

    /**
     * Metodo per incapsulare il binding del tasto di conferma.
     */
    private void setupButtonBinding() {
        btnConfirm.disableProperty().bind(
                txfUsername.textProperty().isEmpty().or(
                txfPswd.textProperty().isEmpty().or(
                txfPswdConfirm.textProperty().isEmpty())));
    }

    @FXML
    private void onConfirm(ActionEvent event) throws IOException {        
        // 1. Rimuove il binding e congela il pulsante
        btnConfirm.disableProperty().unbind();
        btnConfirm.setDisable(true);

        // 2. Controllo validità password locale
        if (!txfPswd.getText().equals(txfPswdConfirm.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Le password inserite non coincidono!");
            alert.showAndWait();
            
            // Sblocca subito il tasto se c'è un errore di validazione locale
            resetSignupButton();
            return;
        }
        
        // 3. Invia richiesta di registrazione
        signupManager.registerInfo(txfUsername.getText(), txfPswd.getText());
    }
    
    /**
     * Sblocca l'interfaccia. Da richiamare quando il server rifiuta la registrazione.
     */
    public void resetSignupButton() {
        Platform.runLater(() -> {
            btnConfirm.setDisable(false);
            setupButtonBinding();
        });
    }

    @FXML
    public void switchToLogin(ActionEvent event) throws IOException {
        Stage stage = (Stage) outerContainer.getScene().getWindow();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
        Parent root = loader.load();
        
        LoginViewController ctrl = (LoginViewController) loader.getController();
        ctrl.setLoginManager(loginManager);
        ctrl.setSignupManager(signupManager);
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void exitApp(ActionEvent event) {
        Platform.exit();
    }

    public void setSignupManager(SignupManager signupManager) {
        this.signupManager = signupManager;
    }
    
    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }
}