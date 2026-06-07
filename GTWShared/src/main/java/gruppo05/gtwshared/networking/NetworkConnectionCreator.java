package gruppo05.gtwshared.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author francesco-vecchione
 */
public abstract class NetworkConnectionCreator {
    private final static String HEADER_STRING = ".properties";
    private final static String IP_STRING = "server.ip=";
    private final static String PORT_STRING = "server.port=";
    
    public abstract NetworkConnection createConnection();
    
    public NetworkConfiguration readConfiguration(String fileName) {
        /* Nel file *.properties
            .properties             -> HEADER_STRING
            server.ip=127.0.0.1     -> IP_STRING
            server.port=5000        -> PORT_STRING
        */ 
        NetworkConfiguration config = null;
        
        if(!(new File(fileName).exists())) return createDefaultConfigFile(fileName);
        
        try (FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr);
                Scanner sc = new Scanner(br)) {
            
            // Inizializzazione
            String ip = null;
            int port = 0;
            
            String line = sc.nextLine().trim();
            if(!line.equalsIgnoreCase(HEADER_STRING)) throw new IOException(
                    "Formato file non valido: la prima riga deve essere \"" + HEADER_STRING + "\"" );
            
            while(sc.hasNextLine()) {
                line = sc.nextLine().trim();
                
                if(line.startsWith(IP_STRING)) 
                    ip = line.substring(IP_STRING.length());
                else if(line.startsWith(PORT_STRING)) 
                    port = Integer.parseInt(line.substring(PORT_STRING.length()));
            }
            
            config = new NetworkConfiguration(ip, port);    
        } catch (IOException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }
        
        return config;
    }
    
    private NetworkConfiguration createDefaultConfigFile(String fileName) {
        NetworkConfiguration config = new NetworkConfiguration("127.0.0.1", 5000);
        
        try (FileWriter fw = new FileWriter(new File(fileName));
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw = new PrintWriter(bw)) {
            
            pw.format(HEADER_STRING + "%n" + IP_STRING + "%s%n" + PORT_STRING + "%d", 
                    config.getIp(),
                    config.getPort());
        } catch (IOException ex) {
            // Debug: da cambiare
            ex.printStackTrace();            
        }
        
        return config;
    }
}
