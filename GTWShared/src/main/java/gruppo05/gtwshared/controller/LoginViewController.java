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
public class LoginViewController implements Initializable {

    @FXML
    private Pane outerContainer;
    @FXML
    private TextField txfUsername;
    @FXML
    private TextField txfPswd;
    @FXML
    private Hyperlink linkToLogin;
    @FXML
    private Button btnExit;
    @FXML
    private Button btnConfirm;

    private LoginManager loginManager;  
    private SignupManager signupManager;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConfirm.disableProperty().bind(Bindings.or(
                txfUsername.textProperty().isEmpty(), txfPswd.textProperty().isEmpty()));
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

    @FXML
    private void onConfirm(ActionEvent event) throws IOException {
        loginManager.validateInfo(txfUsername.getText(), txfPswd.getText());
    }
    
    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }
    
    public void setSignupManager(SignupManager signupManager) {
        this.signupManager = signupManager;
    }
}
