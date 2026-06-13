#!/bin/bash

# Risolve il percorso assoluto della cartella principale del progetto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Il revisore si posiziona nella cartella eseguibili (dopo aver scompattato archivio zip consegnato)
cd "$PROJECT_ROOT/eseguibili" || { echo "Errore: Cartella 'eseguibili' non trovata."; exit 1; }

echo "=== Avvio Simulazione del Comportamento del Revisore ==="

# Assicura l'uso di Java 8 se presente nel sistema
if /usr/libexec/java_home -v 1.8 >/dev/null 2>&1; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    echo "Errore: JDK 1.8 (Java 8) non trovato nel sistema tramite /usr/libexec/java_home."
    echo "Verifica l'installazione di Java 8 prima di procedere."
    exit 1
fi

# Funzione per arrestare tutti i processi in background quando lo script termina
cleanup() {
    echo ""
    echo "=== Fine Simulazione ==="
    echo "Arresto dei processi avviati in background..."
    [ -n "$CLIENT1_PID" ] && kill $CLIENT1_PID 2>/dev/null
    [ -n "$SERVER_PID" ] && kill $SERVER_PID 2>/dev/null
}
trap cleanup EXIT

# Crea cartella dedicata per i log
mkdir -p run_logs

# FASE 1) Avvia il server
echo "FASE 1) Avvio del Server (in background)..."
$JAVA_CMD -jar server.jar > run_logs/server_simulazione.log 2>&1 &
SERVER_PID=$!

echo "Attesa di 3 secondi..."
sleep 3

# FASE 2) Avvia il primo client
echo "FASE 2) Avvio del Primo Client (in background)..."
$JAVA_CMD -jar client.jar > run_logs/client1_simulazione.log 2>&1 &
CLIENT1_PID=$!

echo "Attesa di 3 secondi..."
sleep 3

# FASE 3) Avvia il secondo client
echo "FASE 3) Avvio del Secondo Client (in foreground)..."
echo "-> I log del server e dei client vengono salvati in eseguibili/run_logs/"
echo "-> Premi CTRL+C nel terminale oppure chiudi la finestra di questo client per arrestare tutto."
$JAVA_CMD -jar client.jar > run_logs/client2_simulazione.log 2>&1

echo "Il secondo client è stato chiuso."
