-- =====================================================
-- MySQL: Testdaten fuer MCP DB-Explorer
-- =====================================================
-- Szenario: Fitness-Studio Verwaltung mit Mitgliedern,
--           Kursen, Trainern und Buchungen.
-- =====================================================

-- Trainer
CREATE TABLE trainers (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    specialty   VARCHAR(100),
    email       VARCHAR(200),
    active      BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Kursarten
CREATE TABLE course_types (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    duration_min INT NOT NULL DEFAULT 60
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Kurse (konkrete Termine)
CREATE TABLE courses (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    course_type_id INT NOT NULL,
    trainer_id     INT NOT NULL,
    weekday        VARCHAR(10) NOT NULL,
    start_time     TIME NOT NULL,
    max_capacity   INT NOT NULL DEFAULT 20,
    FOREIGN KEY (course_type_id) REFERENCES course_types(id),
    FOREIGN KEY (trainer_id)     REFERENCES trainers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Mitglieder
CREATE TABLE members (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    phone           VARCHAR(30),
    membership_type VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    join_date       DATE NOT NULL,
    active          BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Buchungen
CREATE TABLE bookings (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    member_id   INT NOT NULL,
    course_id   INT NOT NULL,
    booking_date DATE NOT NULL,
    attended    BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Zahlungen
CREATE TABLE payments (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    member_id    INT NOT NULL,
    amount       DECIMAL(8,2) NOT NULL,
    payment_date DATE NOT NULL,
    period_month VARCHAR(7) NOT NULL,
    method       VARCHAR(20) NOT NULL DEFAULT 'Lastschrift',
    FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Testdaten ───────────────────────────────────────────────────────────

INSERT INTO trainers (name, specialty, email) VALUES
    ('Marco Rossi',    'Krafttraining',    'marco@fitgym.de'),
    ('Anja Berger',    'Yoga & Pilates',   'anja@fitgym.de'),
    ('Kai Zimmermann', 'Cardio & HIIT',    'kai@fitgym.de'),
    ('Lena Schmidt',   'Spinning',         'lena@fitgym.de'),
    ('Tim Wagner',     'Functional Training','tim@fitgym.de');

INSERT INTO course_types (name, description, duration_min) VALUES
    ('Yoga',              'Hatha Yoga fuer alle Level',                 60),
    ('Spinning',          'Indoor Cycling mit Musik',                   45),
    ('HIIT',              'High Intensity Interval Training',           30),
    ('Pilates',           'Core-Training und Flexibilitaet',            60),
    ('Kraftzirkel',       'Ganzkörper-Krafttraining im Zirkel',        45),
    ('Body Pump',         'Langhantel-Workout fuer alle Muskelgruppen', 60),
    ('Stretching',        'Dehnung und Mobilitaet',                     30),
    ('Functional Fit',    'Funktionelles Training mit Eigengewicht',    45);

INSERT INTO courses (course_type_id, trainer_id, weekday, start_time, max_capacity) VALUES
    (1, 2, 'Montag',     '08:00', 25),
    (1, 2, 'Mittwoch',   '08:00', 25),
    (1, 2, 'Freitag',    '08:00', 25),
    (2, 4, 'Montag',     '18:00', 30),
    (2, 4, 'Donnerstag', '18:00', 30),
    (3, 3, 'Dienstag',   '07:00', 20),
    (3, 3, 'Donnerstag', '07:00', 20),
    (4, 2, 'Dienstag',   '10:00', 20),
    (5, 1, 'Mittwoch',   '17:00', 15),
    (5, 1, 'Freitag',    '17:00', 15),
    (6, 1, 'Montag',     '19:00', 25),
    (6, 5, 'Mittwoch',   '19:00', 25),
    (7, 2, 'Freitag',    '09:30', 30),
    (8, 5, 'Dienstag',   '18:00', 20),
    (8, 5, 'Samstag',    '10:00', 20);

INSERT INTO members (name, email, phone, membership_type, join_date) VALUES
    ('Sarah Lehmann',    'sarah@example.com',    '0170-1234567', 'PREMIUM',  '2024-06-01'),
    ('Niklas Huber',     'niklas@example.com',   '0171-2345678', 'BASIC',    '2024-09-15'),
    ('Julia Becker',     'julia@example.com',    '0172-3456789', 'PREMIUM',  '2024-11-01'),
    ('Markus Klein',     'markus@example.com',   '0173-4567890', 'BASIC',    '2025-01-10'),
    ('Eva Richter',      'eva@example.com',      '0174-5678901', 'PREMIUM',  '2025-02-20'),
    ('Tobias Wolf',      'tobias@example.com',   '0175-6789012', 'BASIC',    '2025-04-01'),
    ('Laura Fischer',    'laura.f@example.com',  '0176-7890123', 'PREMIUM',  '2025-06-15'),
    ('Moritz Braun',     'moritz@example.com',   '0177-8901234', 'BASIC',    '2025-08-01'),
    ('Hannah Meyer',     'hannah@example.com',   '0178-9012345', 'PREMIUM',  '2025-10-10'),
    ('Felix Schulz',     'felix@example.com',    '0179-0123456', 'BASIC',    '2025-12-01'),
    ('Amelie Vogel',     'amelie@example.com',   '0160-1234567', 'PREMIUM',  '2026-01-15'),
    ('David Koch',       'david@example.com',    '0161-2345678', 'BASIC',    '2026-02-20'),
    ('Carla Wagner',     'carla@example.com',    '0162-3456789', 'BASIC',    '2026-03-01'),
    ('Leon Bauer',       'leon@example.com',     '0163-4567890', 'PREMIUM',  '2026-03-15'),
    ('Mia Schuster',     'mia@example.com',      NULL,           'BASIC',    '2026-04-01');

-- Buchungen (April 2026)
INSERT INTO bookings (member_id, course_id, booking_date, attended) VALUES
    (1,  1, '2026-04-06', TRUE),
    (1,  8, '2026-04-07', TRUE),
    (1,  2, '2026-04-08', TRUE),
    (2,  6, '2026-04-07', TRUE),
    (2, 14, '2026-04-07', TRUE),
    (3,  1, '2026-04-06', TRUE),
    (3,  4, '2026-04-06', TRUE),
    (3,  3, '2026-04-10', FALSE),
    (4,  9, '2026-04-08', TRUE),
    (5,  1, '2026-04-06', TRUE),
    (5,  2, '2026-04-08', TRUE),
    (5, 13, '2026-04-10', TRUE),
    (6,  6, '2026-04-07', TRUE),
    (6,  7, '2026-04-09', FALSE),
    (7, 11, '2026-04-06', TRUE),
    (7, 12, '2026-04-08', TRUE),
    (8, 15, '2026-04-11', FALSE),
    (9,  4, '2026-04-06', TRUE),
    (9,  5, '2026-04-09', TRUE),
    (10, 6, '2026-04-07', TRUE),
    (10,14, '2026-04-07', TRUE),
    (11, 1, '2026-04-06', TRUE),
    (11, 8, '2026-04-07', TRUE),
    (12, 9, '2026-04-08', FALSE),
    (13,14, '2026-04-07', TRUE),
    (14, 4, '2026-04-06', TRUE),
    (14,11, '2026-04-06', TRUE),
    (15, 3, '2026-04-10', FALSE);

-- Zahlungen (letztes Quartal)
INSERT INTO payments (member_id, amount, payment_date, period_month, method) VALUES
    (1,  49.99, '2026-02-01', '2026-02', 'Lastschrift'),
    (1,  49.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (1,  49.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (2,  29.99, '2026-02-01', '2026-02', 'Lastschrift'),
    (2,  29.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (2,  29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (3,  49.99, '2026-02-01', '2026-02', 'Kreditkarte'),
    (3,  49.99, '2026-03-01', '2026-03', 'Kreditkarte'),
    (3,  49.99, '2026-04-01', '2026-04', 'Kreditkarte'),
    (4,  29.99, '2026-02-01', '2026-02', 'Lastschrift'),
    (4,  29.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (4,  29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (5,  49.99, '2026-02-01', '2026-02', 'PayPal'),
    (5,  49.99, '2026-03-01', '2026-03', 'PayPal'),
    (5,  49.99, '2026-04-01', '2026-04', 'PayPal'),
    (6,  29.99, '2026-02-01', '2026-02', 'Lastschrift'),
    (6,  29.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (6,  29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (7,  49.99, '2026-02-01', '2026-02', 'Lastschrift'),
    (7,  49.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (7,  49.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (8,  29.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (8,  29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (9,  49.99, '2026-02-01', '2026-02', 'Kreditkarte'),
    (9,  49.99, '2026-03-01', '2026-03', 'Kreditkarte'),
    (9,  49.99, '2026-04-01', '2026-04', 'Kreditkarte'),
    (10, 29.99, '2026-02-01', '2026-02', 'Lastschrift'),
    (10, 29.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (10, 29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (11, 49.99, '2026-02-01', '2026-02', 'PayPal'),
    (11, 49.99, '2026-03-01', '2026-03', 'PayPal'),
    (11, 49.99, '2026-04-01', '2026-04', 'PayPal'),
    (12, 29.99, '2026-03-01', '2026-03', 'Lastschrift'),
    (12, 29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (13, 29.99, '2026-04-01', '2026-04', 'Lastschrift'),
    (14, 49.99, '2026-04-01', '2026-04', 'Kreditkarte'),
    (15, 29.99, '2026-04-01', '2026-04', 'Lastschrift');
