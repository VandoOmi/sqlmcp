-- =====================================================
-- SQL Server: Testdaten fuer MCP DB-Explorer
-- =====================================================
-- Szenario: IT-Ticket-System mit Tickets, Mitarbeitern,
--           Kategorien und Kommentaren.
-- =====================================================

USE mcp_testdb;
GO

-- Prioritaeten
CREATE TABLE priorities (
    id   INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(20) NOT NULL,
    sla_hours INT NOT NULL
);

-- Kategorien
CREATE TABLE categories (
    id   INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL
);

-- Mitarbeiter (Support-Team)
CREATE TABLE agents (
    id       INT IDENTITY(1,1) PRIMARY KEY,
    name     NVARCHAR(200) NOT NULL,
    email    NVARCHAR(200) NOT NULL,
    team     NVARCHAR(50) NOT NULL,
    active   BIT DEFAULT 1
);

-- Tickets
CREATE TABLE tickets (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    title        NVARCHAR(300) NOT NULL,
    description  NVARCHAR(MAX),
    category_id  INT NOT NULL FOREIGN KEY REFERENCES categories(id),
    priority_id  INT NOT NULL FOREIGN KEY REFERENCES priorities(id),
    assigned_to  INT FOREIGN KEY REFERENCES agents(id),
    status       NVARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reporter     NVARCHAR(200) NOT NULL,
    created_at   DATETIME2 DEFAULT GETDATE(),
    resolved_at  DATETIME2,
    resolution   NVARCHAR(MAX)
);

-- Kommentare
CREATE TABLE ticket_comments (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ticket_id  INT NOT NULL FOREIGN KEY REFERENCES tickets(id),
    author     NVARCHAR(200) NOT NULL,
    comment    NVARCHAR(MAX) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE()
);

-- ── Testdaten ───────────────────────────────────────────────────────────

INSERT INTO priorities (name, sla_hours) VALUES
    ('Kritisch', 4),
    ('Hoch',     8),
    ('Mittel',  24),
    ('Niedrig', 72);

INSERT INTO categories (name) VALUES
    ('Netzwerk'),
    ('Software'),
    ('Hardware'),
    ('Zugang & Berechtigungen'),
    ('E-Mail'),
    ('Drucker'),
    ('Sonstiges');

INSERT INTO agents (name, email, team) VALUES
    ('Anna Schmidt',    'a.schmidt@support.de',  'Level-1'),
    ('Boris Meyer',     'b.meyer@support.de',    'Level-1'),
    ('Claudia Wolf',    'c.wolf@support.de',     'Level-2'),
    ('Dennis Braun',    'd.braun@support.de',     'Level-2'),
    ('Elena Fischer',   'e.fischer@support.de',   'Level-3'),
    ('Frank Huber',     'f.huber@support.de',     'Level-3');

-- Tickets (Mix aus offenen, in Bearbeitung, geloesten)
INSERT INTO tickets (title, description, category_id, priority_id, assigned_to, status, reporter, created_at, resolved_at, resolution) VALUES
    ('VPN-Verbindung bricht staendig ab', 'Seit heute Morgen bricht die VPN-Verbindung alle 10 Minuten ab. Betrifft Homeoffice.', 1, 1, 3, 'IN_PROGRESS', 'Max Mustermann', '2026-04-13 08:15:00', NULL, NULL),
    ('Outlook stuerzt beim Start ab', 'Outlook 365 oeffnet sich kurz und schliesst sofort wieder. Neustart hilft nicht.', 5, 2, 1, 'IN_PROGRESS', 'Erika Muster', '2026-04-13 08:30:00', NULL, NULL),
    ('Neuer Mitarbeiter braucht AD-Konto', 'Bitte AD-Konto fuer Thomas Neumann anlegen. Abteilung: Marketing. Start: 15.04.', 4, 3, 2, 'OPEN', 'Hans Schmidt', '2026-04-13 09:00:00', NULL, NULL),
    ('Drucker 3. OG druckt nicht mehr', 'HP LaserJet im 3. OG zeigt Papierstau, aber kein Papier klemmt.', 6, 3, NULL, 'OPEN', 'Anna Weber', '2026-04-13 09:45:00', NULL, NULL),
    ('SAP-Berechtigung fuer Modul FI fehlt', 'Benoetige Zugriff auf SAP FI-Modul fuer Monatsabschluss.', 4, 2, 4, 'IN_PROGRESS', 'Peter Fischer', '2026-04-12 14:00:00', NULL, NULL),
    ('Laptop-Bildschirm flackert', 'Dell Latitude 5540 - Bildschirm flackert unregelmaessig. Auch am externen Monitor.', 3, 3, 1, 'IN_PROGRESS', 'Laura Hoffmann', '2026-04-12 10:30:00', NULL, NULL),
    ('Teams-Anrufe ohne Ton', 'Bei Teams-Anrufen hoere ich nichts, Mikro funktioniert laut Gegenseite.', 2, 2, 3, 'RESOLVED', 'Felix Wagner', '2026-04-11 16:00:00', '2026-04-12 09:00:00', 'Audio-Treiber wurde neu installiert. Headset war als Standardgeraet entfernt.'),
    ('WLAN im Meetingraum A schwach', 'Signal im Meetingraum A ist sehr schwach, Videocalls brechen ab.', 1, 2, 5, 'RESOLVED', 'Sophie Klein', '2026-04-10 11:00:00', '2026-04-11 15:00:00', 'Neuer Access Point installiert und konfiguriert.'),
    ('Software-Installation: Visual Studio', 'Bitte Visual Studio 2025 Enterprise auf meinem Rechner installieren.', 2, 4, 2, 'RESOLVED', 'Jan Becker', '2026-04-10 09:00:00', '2026-04-10 14:30:00', 'VS 2025 Enterprise installiert und lizenziert.'),
    ('Passwort abgelaufen', 'Windows meldet dass mein Passwort abgelaufen ist, kann mich nicht anmelden.', 4, 1, 1, 'RESOLVED', 'Lena Schulz', '2026-04-11 07:30:00', '2026-04-11 07:45:00', 'Passwort zurueckgesetzt, Userin informiert.'),
    ('File-Server langsam', 'Zugriff auf \\\\fileserver\\shared dauert ewig seit dem Wochenende.', 1, 2, 5, 'IN_PROGRESS', 'Thomas Mueller', '2026-04-12 08:00:00', NULL, NULL),
    ('Monitor zeigt kein Bild', 'Neuer Monitor Dell U2723QE zeigt kein Bild ueber USB-C.', 3, 3, NULL, 'OPEN', 'Markus Braun', '2026-04-13 10:15:00', NULL, NULL),
    ('Spam-Flut im Postfach', 'Seit 3 Tagen bekomme ich 50+ Spam-Mails pro Tag trotz Filter.', 5, 2, 4, 'IN_PROGRESS', 'Elena Richter', '2026-04-11 09:00:00', NULL, NULL),
    ('Zugang zu Confluence gesperrt', 'Kann mich nicht mehr in Confluence einloggen, Account scheint gesperrt.', 4, 3, 2, 'RESOLVED', 'Hannah Meyer', '2026-04-09 13:00:00', '2026-04-09 14:00:00', 'Account war nach 5 Fehlversuchen gesperrt. Entsperrt und neues Passwort vergeben.'),
    ('Excel Makros funktionieren nicht', 'Nach Office-Update laufen meine VBA-Makros nicht mehr.', 2, 3, 3, 'OPEN', 'Markus Klein', '2026-04-13 10:30:00', NULL, NULL);

-- Kommentare
INSERT INTO ticket_comments (ticket_id, author, comment, created_at) VALUES
    (1, 'Claudia Wolf',    'Pruefe VPN-Gateway Logs. Eventuell MTU-Problem.',                   '2026-04-13 08:30:00'),
    (1, 'Max Mustermann',  'Danke, bin weiterhin betroffen. Kollegen auch.',                     '2026-04-13 09:15:00'),
    (1, 'Claudia Wolf',    'MTU auf 1400 reduziert, bitte testen.',                              '2026-04-13 10:00:00'),
    (2, 'Anna Schmidt',    'Office Repair gestartet. Warte auf Ergebnis.',                       '2026-04-13 09:00:00'),
    (5, 'Dennis Braun',    'Genehmigung vom Abteilungsleiter angefragt.',                        '2026-04-12 14:30:00'),
    (5, 'Dennis Braun',    'Genehmigung erhalten, Rolle wird heute Abend zugewiesen.',           '2026-04-12 16:00:00'),
    (6, 'Anna Schmidt',    'Treiber-Update durchgefuehrt. Bitte Feedback nach einem Tag.',       '2026-04-12 11:30:00'),
    (6, 'Laura Hoffmann',  'Flackert leider immer noch, vor allem bei hellem Hintergrund.',      '2026-04-13 08:00:00'),
    (7, 'Claudia Wolf',    'Audio-Treiber Realtek HD neu installiert.',                          '2026-04-11 17:00:00'),
    (7, 'Felix Wagner',    'Funktioniert wieder einwandfrei, vielen Dank!',                      '2026-04-12 09:00:00'),
    (8, 'Elena Fischer',   'Access Point bestellt, Lieferung morgen.',                           '2026-04-10 14:00:00'),
    (8, 'Elena Fischer',   'AP installiert und konfiguriert. Signalstaerke jetzt -45dBm.',       '2026-04-11 15:00:00'),
    (11,'Elena Fischer',   'Disk-I/O auf dem Fileserver bei 95%. Festplattencheck laeuft.',       '2026-04-12 09:00:00'),
    (11,'Elena Fischer',   'RAID-Rebuild nach stiller Disk-Fehler. ETA: 6 Stunden.',              '2026-04-12 12:00:00'),
    (13,'Dennis Braun',    'Spam-Regeln verschaerft und Absender-Domaenen blockiert.',           '2026-04-11 10:00:00'),
    (13,'Elena Richter',   'Etwas besser, aber immer noch ca. 20 Spam-Mails pro Tag.',           '2026-04-12 08:00:00');
GO
