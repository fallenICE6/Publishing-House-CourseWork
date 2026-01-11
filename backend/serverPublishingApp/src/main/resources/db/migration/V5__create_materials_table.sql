CREATE TABLE materials (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL CHECK (category IN ('paper', 'cover', 'binding')),
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10,2) NOT NULL
);

INSERT INTO materials (category, name, price) VALUES
('paper', 'Офсетная бумага 80 г/м²', 25.50),
('paper', 'Мелованная бумага 90 г/м²', 45.00),
('paper', 'Дизайнерская бумага 120 г/м²', 85.00),
('paper', 'Книжная бумага 70 г/м²', 20.00),

('cover', 'Мягкая обложка', 150.00),
('cover', 'Твёрдый переплёт 7БЦ', 350.00),
('cover', 'Твёрдый переплёт с тканью', 450.00),

('binding', 'Клеевое скрепление (КБС)', 50.00),
('binding', 'Сшивание скобой', 25.00),
('binding', 'Пружина металлическая', 80.00);
