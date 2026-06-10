package gruppo05.gtwshared.networking;

/**
 * @class NetworkConfiguration
 * @brief Contenitore immutabile per i parametri di configurazione della rete.
 * @details Questa classe memorizza l'indirizzo IP e la porta necessari per instaurare
 * una connessione verso il server o per configurare un socket di ascolto.
 * 
 * @author chiara
 * @version 1.0
 */
public class NetworkConfiguration {

    /** Indirizzo IP del server (es. "127.0.0.1"). */
    private final String ip;
    
    /** Numero di porta utilizzata per la comunicazione di rete. */
    private final int port;

    /**
     * @brief Costruttore completo per inizializzare IP e porta.
     * @param ip L'indirizzo IP del destinatario.
     * @param port La porta di comunicazione.
     */
    public NetworkConfiguration(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
    /**
     * @brief Costruttore semplificato per la configurazione del solo ascolto locale.
     * @details Utile quando l'indirizzo IP non è necessario (es. nel lato Server).
     * @param port La porta di ascolto.
     */
    public NetworkConfiguration(int port) {
        this.ip = null;
        this.port = port;
    }

    /**
     * @brief Restituisce l'indirizzo IP configurato.
     * @return Stringa contenente l'IP, oppure null se non impostato.
     */
    public String getIp() {
        return ip;
    }

    /**
     * @brief Restituisce la porta configurata.
     * @return Intero rappresentante la porta di rete.
     */
    public int getPort() {
        return port;
    }
}