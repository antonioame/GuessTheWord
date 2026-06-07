package gruppo05.gtwshared.networking;

/**
 *
 * @author francesco-vecchione
 */
public class NetworkConfiguration {
    private final String ip;
    private final int port;

    public NetworkConfiguration(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
    public NetworkConfiguration(int port) {
        this.ip = null;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
