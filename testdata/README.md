# Testdatenbanken fuer MCP DB-Explorer

Dieses Verzeichnis enthaelt Docker-Container und Init-Skripte fuer drei verschiedene Datenbank-Typen, die du ueber die Admin-Oberflaeche des MCP DB-Explorers verbinden kannst.

## Schnellstart

```bash
cd testdata
docker compose up -d
```

Alle drei Datenbanken starten parallel. Nach ca. 30 Sekunden sind sie bereit.

## Verbindungsdaten

### PostgreSQL (Buchhandlung)

| Feld | Wert |
|------|------|
| **JDBC URL** | `jdbc:postgresql://localhost:5432/mcp_testdb` |
| **Driver** | `org.postgresql.Driver` |
| **User** | `mcp_user` |
| **Password** | `mcp_pass` |

**Tabellen:** `authors`, `genres`, `books`, `customers`, `orders`, `order_items`, `book_reviews`
**Szenario:** Online-Buchhandlung mit deutschsprachigen Autoren, Buechern, Bestellungen und Bewertungen.

---

### MySQL (Fitness-Studio)

| Feld | Wert |
|------|------|
| **JDBC URL** | `jdbc:mysql://localhost:3306/mcp_testdb` |
| **Driver** | `com.mysql.cj.jdbc.Driver` |
| **User** | `mcp_user` |
| **Password** | `mcp_pass` |

**Tabellen:** `trainers`, `course_types`, `courses`, `members`, `bookings`, `payments`
**Szenario:** Fitness-Studio Verwaltung mit Trainern, Kursen, Mitgliedern, Buchungen und Zahlungen.

---

### SQL Server (IT-Ticketsystem)

| Feld | Wert |
|------|------|
| **JDBC URL** | `jdbc:sqlserver://localhost:1433;databaseName=mcp_testdb;encrypt=false` |
| **Driver** | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| **User** | `sa` |
| **Password** | `McpTest#2026` |

**Tabellen:** `priorities`, `categories`, `agents`, `tickets`, `ticket_comments`
**Szenario:** IT-Support Ticket-System mit Prioritaeten, Agenten, Tickets und Kommentaren.

---

## Nutzung mit MCP DB-Explorer

1. App starten: `./mvnw spring-boot:run`
2. Admin-Panel oeffnen: `http://localhost:8080/admin.html`
3. Unter "Datenbankverbindung" die gewuenschte Verbindung eintragen
4. Verbindung testen & speichern
5. Neue Tabellen im Tab "Tabellen" registrieren
6. MCP-Tools erstellen, die auf die neuen Tabellen zugreifen

## Aufraeumen

```bash
# Container stoppen
docker compose down

# Container UND Daten loeschen
docker compose down -v
```

## Verzeichnisstruktur

```
testdata/
├── docker-compose.yml          # Startet alle 3 Datenbanken
├── README.md                   # Diese Datei
├── init/
│   ├── postgres/
│   │   └── 01_init.sql         # PostgreSQL: Buchhandlung
│   ├── mysql/
│   │   └── 01_init.sql         # MySQL: Fitness-Studio
│   └── mssql/
│       ├── setup.sh            # SQL Server Startup-Script
│       └── 01_init.sql         # SQL Server: IT-Tickets
├── 01_onlineshop/              # Standalone H2-Skripte (Online-Shop)
│   ├── schema.sql
│   └── data.sql
└── 02_hr/                      # Standalone H2-Skripte (HR)
    ├── schema.sql
    └── data.sql
```
