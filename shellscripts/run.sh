#!/bin/bash

# Risolve il percorso assoluto della cartella principale del progetto (un livello sopra a shellscripts)
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Verifica presenza di JDK 1.8
if ! /usr/libexec/java_home -v 1.8 >/dev/null 2>&1; then
    echo "Errore: JDK 1.8 (Java 8) non trovato nel sistema tramite /usr/libexec/java_home."
    echo "Verifica l'installazione di Java 8 prima di procedere."
    exit 1
fi

# Verifica presenza di Maven
if ! command -v mvn >/dev/null 2>&1; then
    echo "Errore: Maven (mvn) non trovato nel sistema."
    echo "Installa Maven (es. brew install maven) prima di procedere."
    exit 1
fi

# Forza l'uso di Java 8 per Maven
# (variabile di ambiente impostata e valida solo nel contesto di questo script shell)
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)

build_project() {
    echo "Compilazione e installazione globale dei moduli Maven..."
    cd "$PROJECT_ROOT"
    mvn clean install
}

test_project() {
    echo "Esecuzione dei test Maven..."
    cd "$PROJECT_ROOT"
    mvn test
}

clean_project() {
    echo "Pulizia del progetto (Maven clean)..."
    cd "$PROJECT_ROOT"
    mvn clean
}

run_server() {
    echo "Avvio del Server..."
    cd "$PROJECT_ROOT/GTWServer"
    mvn exec:java
}

run_client() {
    echo "Avvio del Client..."
    cd "$PROJECT_ROOT/GTWClient"
    mvn exec:java
}

run_both() {
    echo "Avvio completo dell'applicazione (1 Server e 2 Client)..."
    
    # Avvia il server in background convogliando i log in server.log
    echo "-> Avvio del Server in background (output in server.log)..."
    cd "$PROJECT_ROOT/GTWServer"
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
    mvn exec:java > "$PROJECT_ROOT/server.log" 2>&1 &
    SERVER_PID=$!
    
    # Assicura l'arresto dei processi in background alla terminazione dello script
    cleanup() {
        echo "Arresto dei processi in background..."
        [ -n "$SERVER_PID" ] && kill $SERVER_PID 2>/dev/null
        [ -n "$CLIENT1_PID" ] && kill $CLIENT1_PID 2>/dev/null
    }
    trap cleanup EXIT
    
    # Attesa per dare il tempo al server di avviarsi ed effettuare il bind sulla porta
    echo "-> Attesa di 3 secondi per l'inizializzazione del server..."
    sleep 3
    
    # Avvia il primo client in background convogliando i log in client1.log
    echo "-> Avvio del Primo Client in background (output in client1.log)..."
    cd "$PROJECT_ROOT/GTWClient"
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
    mvn exec:java > "$PROJECT_ROOT/client1.log" 2>&1 &
    CLIENT1_PID=$!
    
    # Attesa di 2 secondi
    echo "-> Attesa di 2 secondi..."
    sleep 2
    
    # Avvia il secondo client nella finestra corrente del terminale
    run_client
}

show_help() {
    echo "Uso: $0 [opzione]"
    echo ""
    echo "Opzioni disponibili:"
    echo "  build   - Esegue 'mvn clean install' dalla root per compilare e installare tutti i moduli."
    echo "  test    - Esegue 'mvn test' dalla root per eseguire tutti i test del progetto."
    echo "  clean   - Esegue 'mvn clean' dalla root per pulire il progetto."
    echo "  server  - Avvia solo il Server in questa finestra."
    echo "  client  - Avvia solo il Client in questa finestra."
    echo "  start   - Avvia il Server e un Client in background, e un secondo Client in questa finestra (Scelta consigliata)."
    echo "  help    - Mostra questo messaggio."
    echo ""
    echo "Se non viene passata alcuna opzione, lo script eseguirà 'start'."
}

# Selettore comando
COMMAND=${1:-start}

case "$COMMAND" in
    build)
        build_project
        ;;
    test)
        test_project
        ;;
    clean)
        clean_project
        ;;
    server)
        run_server
        ;;
    client)
        run_client
        ;;
    start)
        run_both
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "Comando non valido: $COMMAND"
        show_help
        exit 1
        ;;
esac
