#!/bin/bash
# =====================================================
# SQL Server Init Script
# Wartet bis SQL Server bereit ist und fuehrt dann
# die Init-Skripte aus.
# =====================================================

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
SA_PASSWORD="McpTest#2026"

echo "Warte auf SQL Server..."
for i in {1..30}; do
    $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -C -Q "SELECT 1" -b > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "SQL Server ist bereit."
        break
    fi
    echo "  Versuch $i/30..."
    sleep 2
done

# Datenbank anlegen
echo "Erstelle Datenbank mcp_testdb..."
$SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -C -Q "
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'mcp_testdb')
    CREATE DATABASE mcp_testdb;
"

# Init-Skripte ausfuehren
echo "Fuehre Init-Skripte aus..."
for f in /docker-entrypoint-initdb.d/*.sql; do
    echo "  -> $f"
    $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -C -d mcp_testdb -i "$f"
done

echo "SQL Server Initialisierung abgeschlossen."
