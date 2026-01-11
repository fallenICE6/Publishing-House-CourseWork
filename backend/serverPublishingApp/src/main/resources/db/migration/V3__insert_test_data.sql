
INSERT INTO editions (title, author_first_name, author_last_name, author_middle_name, description, cover_image) VALUES
('Мастер и Маргарита', 'Михаил', 'Булгаков', 'Афанасьевич', 'Роман о любви, добре и зле, сатире на бюрократию и мистике Москвы 1930-х годов.', 'book1'),
('Преступление и наказание', 'Фёдор', 'Достоевский', 'Михайлович', 'Роман о студенте Раскольникове, совершившем убийство.', 'book2'),
('451° по Фаренгейту', 'Рэй', 'Брэдбери', 'Дуглас', 'Антиутопия о мире, где книги запрещены, а пожарные сжигают их.', 'book3');

-- Привязка жанров к изданиям
INSERT INTO edition_genres (edition_id, genre_id)
SELECT e.id, g.id FROM editions e, genres g
WHERE e.title = 'Мастер и Маргарита' AND g.name IN ('Фантастика', 'Классика');

INSERT INTO edition_genres (edition_id, genre_id)
SELECT e.id, g.id FROM editions e, genres g
WHERE e.title = 'Преступление и наказание' AND g.name IN ('Классика', 'Роман');

INSERT INTO edition_genres (edition_id, genre_id)
SELECT e.id, g.id FROM editions e, genres g
WHERE e.title = '451° по Фаренгейту' AND g.name IN ('Фантастика');


INSERT INTO edition_images (edition_id, image_name, display_order)
SELECT id, 'book1_1', 0 FROM editions WHERE title = 'Мастер и Маргарита'
UNION ALL
SELECT id, 'book1_2', 1 FROM editions WHERE title = 'Мастер и Маргарита'
UNION ALL
SELECT id, 'book1_3', 2 FROM editions WHERE title = 'Мастер и Маргарита';

INSERT INTO edition_images (edition_id, image_name, display_order)
SELECT id, 'book2_1', 0 FROM editions WHERE title = 'Преступление и наказание'
UNION ALL
SELECT id, 'book2_2', 1 FROM editions WHERE title = 'Преступление и наказание'
UNION ALL
SELECT id, 'book2_3', 2 FROM editions WHERE title = 'Преступление и наказание';

INSERT INTO edition_images (edition_id, image_name, display_order)
SELECT id, 'book3_1', 0 FROM editions WHERE title = '451° по Фаренгейту'
UNION ALL
SELECT id, 'book3_2', 1 FROM editions WHERE title = '451° по Фаренгейту';
