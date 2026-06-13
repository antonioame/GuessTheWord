package gruppo05.gtwshared.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
 */
public class SignupViewController implements Initializable {

    public static SignupViewController instance;

    @FXML private Pane outerContainer;
    @FXML private TextField txfUsername;
    @FXML private TextField txfPswd;
    @FXML private TextField txfPswdConfirm;
    @FXML private Hyperlink linkToLogin;
    @FXML private Button btnExit;
    @FXML private Button btnConfirm;
    
    private SignupManager signupManager;
    private LoginManager loginManager;
    private BooleanProperty isProcessing = new SimpleBooleanProperty(false);
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this; 
        
        btnConfirm.disableProperty().bind(
            isProcessing
            .or(txfUsername.textProperty().isEmpty())
            .or(txfPswd.textProperty().isEmpty())
            .or(txfPswdConfirm.textProperty().isEmpty())
        );
    }    

    @FXML
    private void onConfirm(ActionEvent event) throws IOException {        
        isProcessing.set(true);

        if (!txfPswd.getText().equals(txfPswdConfirm.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Le password inserite non coincidono!");
            alert.showAndWait();
            isProcessing.set(false);
            return;
        }
        
        signupManager.registerInfo(txfUsername.getText(), txfPswd.getText());
    }
    
    public void resetSignupButton() {
        Platform.runLater(() -> {
            isProcessing.set(false);
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