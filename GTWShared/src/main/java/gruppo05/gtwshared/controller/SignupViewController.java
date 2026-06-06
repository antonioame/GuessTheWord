package gruppo05.gtwshared.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
 * FXML Controller class
 *
 * @author francesco-vecchione
 */
public class SignupViewController implements Initializable {

    @FXML
    private Pane outerContainer;
    @FXML
    private TextField txfUsername;
    @FXML
    private TextField txfPswd;
    @FXML
    private TextField txfPswdConfirm;
    @FXML
    private Hyperlink linkToLogin;
    @FXML
    private Button btnExit;
    @FXML
    private Button confirmBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void switchToLogin(ActionEvent event) throws IOException {
        Stage stage = (Stage) outerContainer.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("LoginView.fxml"));
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void exitApp(ActionEvent event) {
    }

    @FXML
    private void confirmAction(ActionEvent event) {
    }
    
}
