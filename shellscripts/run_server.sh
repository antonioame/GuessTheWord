#!/bin/bash

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$PROJECT_ROOT/eseguibili" || { echo "Errore: Cartella 'eseguibili' non trovata."; exit 1; }

# Assicura l'uso di Java 8
# (variabile valida solo nel contesto di questo script)
if /usr/libexec/java_home -v 1.8 >/dev/null 2>&1; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    echo "Errore: JDK 1.8 (Java 8) non trovato nel sistema."
    exit 1
fi

echo "=== Avvio Server ==="
$JAVA_CMD -jar server.jar
