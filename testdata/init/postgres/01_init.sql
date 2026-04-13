-- =====================================================
-- PostgreSQL: Testdaten fuer MCP DB-Explorer
-- =====================================================
-- Szenario: Online-Buchhandlung mit Autoren, Buechern,
--           Kunden und Bestellungen.
-- =====================================================

-- Autoren
CREATE TABLE authors (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    country     VARCHAR(100),
    born_year   INT
);

-- Genres
CREATE TABLE genres (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Buecher
CREATE TABLE books (
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(300) NOT NULL,
    author_id    INT NOT NULL REFERENCES authors(id),
    genre_id     INT NOT NULL REFERENCES genres(id),
    isbn         VARCHAR(20) UNIQUE,
    price        NUMERIC(8,2) NOT NULL,
    pages        INT,
    published    DATE,
    in_stock     INT DEFAULT 0
);

-- Kunden
CREATE TABLE customers (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    city       VARCHAR(100),
    registered DATE DEFAULT CURRENT_DATE
);

-- Bestellungen
CREATE TABLE orders (
    id           SERIAL PRIMARY KEY,
    customer_id  INT NOT NULL REFERENCES customers(id),
    order_date   DATE DEFAULT CURRENT_DATE,
    total_amount NUMERIC(10,2) NOT NULL
);

-- Bestellpositionen
CREATE TABLE order_items (
    id        SERIAL PRIMARY KEY,
    order_id  INT NOT NULL REFERENCES orders(id),
    book_id   INT NOT NULL REFERENCES books(id),
    quantity  INT NOT NULL DEFAULT 1,
    unit_price NUMERIC(8,2) NOT NULL
);

-- Bewertungen
CREATE TABLE book_reviews (
    id          SERIAL PRIMARY KEY,
    book_id     INT NOT NULL REFERENCES books(id),
    customer_id INT NOT NULL REFERENCES customers(id),
    rating      INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    review_date DATE DEFAULT CURRENT_DATE
);

-- ── Testdaten ───────────────────────────────────────────────────────────

INSERT INTO authors (name, country, born_year) VALUES
    ('Thomas Mann',         'Deutschland', 1875),
    ('Franz Kafka',         'Oesterreich', 1883),
    ('Hermann Hesse',       'Deutschland', 1877),
    ('Friedrich Duerrenmatt','Schweiz',     1921),
    ('Cornelia Funke',      'Deutschland', 1958),
    ('Daniel Kehlmann',     'Oesterreich', 1975),
    ('Patrick Sueskind',    'Deutschland', 1949),
    ('Michael Ende',        'Deutschland', 1929);

INSERT INTO genres (name) VALUES
    ('Roman'),
    ('Krimi'),
    ('Fantasy'),
    ('Sachbuch'),
    ('Klassiker'),
    ('Kinderbuch'),
    ('Science-Fiction');

INSERT INTO books (title, author_id, genre_id, isbn, price, pages, published, in_stock) VALUES
    ('Der Zauberberg',             1, 5, '978-3-596-29433-6', 14.99, 1008, '1924-11-01', 25),
    ('Buddenbrooks',               1, 5, '978-3-596-29431-2', 12.99,  757, '1901-01-01', 18),
    ('Die Verwandlung',            2, 5, '978-3-15-009900-1',  4.99,   96, '1915-01-01', 40),
    ('Der Prozess',                2, 5, '978-3-596-90025-7', 10.99,  304, '1925-01-01', 15),
    ('Siddhartha',                 3, 1, '978-3-518-36682-2',  9.99,  192, '1922-01-01', 30),
    ('Der Steppenwolf',            3, 1, '978-3-518-36675-4', 11.99,  288, '1927-01-01', 22),
    ('Der Besuch der alten Dame',  4, 2, '978-3-257-23045-1',  8.99,  160, '1956-01-01', 35),
    ('Die Physiker',               4, 2, '978-3-257-23047-5',  7.99,  96,  '1962-01-01', 28),
    ('Tintenherz',                 5, 3, '978-3-7915-0465-8', 16.99,  576, '2003-09-01', 20),
    ('Die Vermessung der Welt',    6, 1, '978-3-499-24100-0', 11.99,  304, '2005-09-01', 12),
    ('Das Parfum',                 7, 1, '978-3-257-22800-7', 12.99,  320, '1985-01-01', 19),
    ('Die unendliche Geschichte',  8, 3, '978-3-522-20260-2', 15.99,  428, '1979-01-01', 33),
    ('Momo',                       8, 6, '978-3-522-20250-3', 13.99,  304, '1973-01-01', 27),
    ('Tschick',                    6, 1, '978-3-499-25635-6', 10.99,  256, '2010-09-01',  8);

INSERT INTO customers (name, email, city, registered) VALUES
    ('Sabine Hartmann', 'sabine@example.com',  'Berlin',    '2025-03-10'),
    ('Klaus Fischer',   'klaus@example.com',   'Muenchen',  '2025-05-22'),
    ('Petra Neumann',   'petra@example.com',   'Hamburg',   '2025-07-15'),
    ('Wolfgang Braun',  'wolfgang@example.com', 'Koeln',    '2025-09-01'),
    ('Monika Schulze',  'monika@example.com',  'Stuttgart', '2025-11-20'),
    ('Herbert Klein',   'herbert@example.com', 'Dresden',   '2026-01-08'),
    ('Ulrike Vogel',    'ulrike@example.com',  'Frankfurt', '2026-02-14'),
    ('Rainer Zimmer',   'rainer@example.com',  'Leipzig',   '2026-03-05'),
    ('Gisela Stein',    'gisela@example.com',  'Nuernberg', '2026-03-28'),
    ('Manfred Roth',    'manfred@example.com', 'Hannover',  '2026-04-01');

INSERT INTO orders (customer_id, order_date, total_amount) VALUES
    (1, '2026-01-15', 27.98),
    (1, '2026-03-20', 12.99),
    (2, '2026-02-10', 33.97),
    (3, '2026-02-28', 16.99),
    (4, '2026-03-05', 22.98),
    (5, '2026-03-15', 49.96),
    (6, '2026-03-22', 11.99),
    (7, '2026-04-01', 28.98),
    (8, '2026-04-05', 15.99),
    (9, '2026-04-10', 41.97),
    (10,'2026-04-12', 10.99);

INSERT INTO order_items (order_id, book_id, quantity, unit_price) VALUES
    (1,  1, 1, 14.99),
    (1,  5, 1,  9.99),
    (1,  3, 1,  4.99),  -- Sabine: Zauberberg + Siddhartha + Verwandlung -> Korrektur total unten
    (2,  2, 1, 12.99),
    (3, 11, 1, 12.99),
    (3,  9, 1, 16.99),
    (3,  3, 1,  4.99),  -- Klaus: Parfum + Tintenherz + Verwandlung -> Korrektur
    (4,  9, 1, 16.99),
    (5,  5, 1,  9.99),
    (5, 11, 1, 12.99),
    (6, 12, 2, 15.99),
    (6,  9, 1, 16.99),
    (7,  6, 1, 11.99),
    (8, 13, 1, 13.99),
    (8, 12, 1, 15.99),  -- Ulrike: Momo + Unendliche Geschichte -> Korrektur
    (9, 12, 1, 15.99),
    (10, 1, 1, 14.99),
    (10,11, 1, 12.99),
    (10,14, 1, 10.99),  -- Gisela: Zauberberg + Parfum + Tschick -> Korrektur
    (11, 4, 1, 10.99);

INSERT INTO book_reviews (book_id, customer_id, rating, comment, review_date) VALUES
    (1,  1, 5, 'Ein Meisterwerk der deutschen Literatur. Zeitlos.',          '2026-01-20'),
    (1,  9, 4, 'Sehr komplex aber lohnenswert. Nichts fuer nebenbei.',      '2026-04-12'),
    (3,  1, 5, 'Kafkas beste Erzaehlung. Brilliant und verstörend.',        '2026-01-22'),
    (5,  1, 4, 'Wunderschoen geschrieben, sehr meditativ.',                 '2026-01-25'),
    (11, 2, 5, 'Faszinierende Geschichte, man riecht foermlich mit.',       '2026-02-15'),
    (9,  2, 4, 'Tolles Buch fuer Buch-Liebhaber!',                         '2026-02-18'),
    (9,  3, 5, 'Hat meine ganze Familie begeistert.',                       '2026-03-05'),
    (12, 5, 5, 'Die Fantasie kennt keine Grenzen. Magisch.',               '2026-03-20'),
    (12, 8, 5, 'Eines der besten Fantasy-Buecher aller Zeiten.',           '2026-04-08'),
    (5,  4, 5, 'Hesse in Bestform. Liest sich an einem Tag.',              '2026-03-10'),
    (11, 4, 4, 'Spannend und gleichzeitig poetisch.',                      '2026-03-12'),
    (6,  6, 4, 'Intensiv und herausfordernd. Toller Hesse.',               '2026-03-25'),
    (13, 7, 5, 'Zeitlos schoen. Perfekt fuer Jung und Alt.',               '2026-04-03'),
    (14, 9, 5, 'Genial, lustig, traurig - alles gleichzeitig.',            '2026-04-11'),
    (4, 10, 4, 'Kafka at his finest. Beklemmend gut.',                     '2026-04-13');
