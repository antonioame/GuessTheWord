package gruppo05.gtwshared.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * @class NetworkConnectionCreator
 * @brief Classe astratta per la gestione della creazione delle connessioni di rete.
 * @details Fornisce i metodi base per leggere la configurazione da file locale e 
 * definisce l'interfaccia per la creazione di connessioni specifiche.
 * 
 * @author chiara
 * @version 1.0
 */
public abstract class NetworkConnectionCreator {

    /** Estensione attesa o header identificativo per il file di configurazione. */
    private final static String HEADER_STRING = ".properties";
    
    /** Prefisso per la lettura dell'indirizzo IP dal file. */
    private final static String IP_STRING = "server.ip=";
    
    /** Prefisso per la lettura della porta dal file. */
    private final static String PORT_STRING = "server.port=";
    
    /** Definizione del Logger. */
    private static final Logger LOGGER = Logger.getLogger(NetworkConnectionCreator.class.getName());
    
    /**
     * @brief Metodo astratto per creare una connessione specifica.
     * @return L'istanza di una classe che estende {@link NetworkConnection}.
     */
    public abstract NetworkConnection createConnection();
    
    /**
     * @brief Legge la configurazione di rete da un file esterno.
     * @details Se il file non esiste, invoca {@link #createDefaultConfigFile(String)} 
     * per generarne uno con valori di default (localhost:5050).
     * @param fileName Percorso del file di configurazione.
     * @return Un oggetto {@link NetworkConfiguration} popolato con IP e porta.
     */
    public NetworkConfiguration readConfiguration(String fileName) {
        NetworkConfiguration config = null;
        
        // Verifica esistenza file: se manca, crea quello di default
        if(!(new File(fileName).exists())) return createDefaultConfigFile(fileName);
        
        // Apertura in lettura del file con try-with-resources
        try (FileReader fr = new FileReader(fileName);
             BufferedReader br = new BufferedReader(fr);
             Scanner sc = new Scanner(br)) {
            
            // Inizializzazione
            String ip = null;
            int port = 0;
            
            // Lettura prima riga per validazione formato header
            String line = sc.nextLine().trim();
            if(!line.equalsIgnoreCase(HEADER_STRING)) throw new IOException(
                    "Formato file non valido: la prima riga deve essere \"" + HEADER_STRING + "\"" );
            
            // Ciclo sulle righe successive per estrarre IP e Porta
            while(sc.hasNextLine()) {
                line = sc.nextLine().trim();
                
                if(line.startsWith(IP_STRING)) 
                    ip = line.substring(IP_STRING.length());
                else if(line.startsWith(PORT_STRING)) 
                    port = Integer.parseInt(line.substring(PORT_STRING.length()));
            }
            
            // Creazione oggetto configurazione con dati letti
            config = new NetworkConfiguration(ip, port);    
        } catch (IOException ex) {
            // Gestione errori 
            LOGGER.log(Level.SEVERE, "Errore fatale nella lettura del file di configurazione", ex);
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore di Sistema");
                alert.setHeaderText("Errore di configurazione");
                alert.setContentText("Il file di configurazione è corrotto o illeggibile. L'applicazione verrà chiusa.");
                alert.showAndWait();
                System.exit(1);
            });
        }
        
        return config;
    }
    
    /**
     * @brief Genera un file di configurazione di default in caso di assenza.
     * @details Crea un file con parametri predefiniti (127.0.0.1:5050) per garantire
     * l'avvio dell'applicazione.
     * @param fileName Nome del file da creare.
     * @return L'oggetto configurazione creato.
     */
    private NetworkConfiguration createDefaultConfigFile(String fileName) {
        NetworkConfiguration config = new NetworkConfiguration("127.0.0.1", 5050);
        
        // Scrittura su file dei valori predefiniti
        try (FileWriter fw = new FileWriter(new File(fileName));
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            
            // Formattazione del contenuto del file
            pw.format(HEADER_STRING + "%n" + IP_STRING + "%s%n" + PORT_STRING + "%d", 
                      config.getIp(),
                      config.getPort());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "File configurazione non trovato", ex);
    
            // Mostra un alert bloccante
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore di Sistema");
                alert.setHeaderText("Configurazione mancante");
                alert.setContentText("Impossibile leggere il file di configurazione. L'applicazione verrà chiusa.");
                alert.showAndWait();
                System.exit(1); // Chiudi l'app
            });          
        }
        
        return config;
    }
}