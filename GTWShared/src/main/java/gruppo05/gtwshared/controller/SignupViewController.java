package gruppo05.gtwshared.controller;

import gruppo05.gtwshared.networking.NetworkConnection;
import java.io.IOException;
import java.net.URL;
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

    private String onConfirmRoute;  // deve essere un path del tipo "/gruppo05/gtwserver/controller/file.fxml"
                                    // qui non ci arriveremo mai perchè nel caso l'operazione di registrazione
                                    // va a buon fine si viene riportati nella pagina di login
    
    private NetworkConnection connection;
    
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
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
        Parent root = loader.load();
        
        LoginViewController ctrl = (LoginViewController) loader.getController();
        ctrl.setOnConfirmRoute(onConfirmRoute);
        ctrl.setConnection(connection);
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void exitApp(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onConfirm(ActionEvent event) throws IOException {
        
        // Validare i campi (Password e Password Confermata devono essere uguali)
        
        // Se i campi non sono validi mostra errore
        
        // Se i campi sono validi inviare il messaggio con i campi
        
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
    
    public void setConnection(NetworkConnection connection) {
        this.connection = connection;
    }
}
