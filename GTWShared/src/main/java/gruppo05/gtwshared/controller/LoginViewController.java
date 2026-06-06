package gruppo05.gtwshared.controller;

import gruppo05.gtwshared.networking.NetworkConnection;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
    private Button confirmBtn;

    private String onConfirmRoute;  // deve essere un path del tipo "/gruppo05/gtwserver/controller/file.fxml"
        
    private NetworkConnection conn;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void switchToSignup(ActionEvent event) throws IOException {
        Stage stage = (Stage) outerContainer.getScene().getWindow();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SignupView.fxml"));
        Parent root = loader.load();
        
        SignupViewController ctrl = (SignupViewController) loader.getController();
        ctrl.setOnConfirmRoute(onConfirmRoute);
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void exitApp(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onConfirm(ActionEvent event) throws IOException {
        
        // Inviare il messaggio con i campi
        
        // Attendere la risposta
        
        // Ricevere la risposta
        
        // Se la risposta è negativa i campi sono invalidi allora non fare nulla
        
        // Se la risposta è positiva allora portati alla confirm route
        
        Stage stage = (Stage) outerContainer.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(onConfirmRoute));
        
        stage.setScene(new Scene(root));
        stage.show();        
    }
    
    public void setOnConfirmRoute(String onConfirmRoute) {
        this.onConfirmRoute = onConfirmRoute;
    }
    
    public void setConn(NetworkConnection conn) {
        this.conn = conn;
    }
}
