CREATE TABLE editions (
    id serial PRIMARY KEY,
    title varchar(200) NOT NULL,
    author_first_name varchar(50) NOT NULL,
    author_last_name varchar(50) NOT NULL,
    author_middle_name varchar(50),
    description text,
    cover_image varchar(255)
);

CREATE TABLE genres (
    id serial PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE
);

CREATE TABLE edition_genres (
    edition_id int NOT NULL REFERENCES editions(id) ON DELETE CASCADE,
    genre_id int NOT NULL REFERENCES genres(id),
    PRIMARY KEY (edition_id, genre_id)
);

CREATE TABLE edition_images (
    id serial PRIMARY KEY,
    edition_id int NOT NULL REFERENCES editions(id) ON DELETE CASCADE,
    image_name varchar(255) NOT NULL,
    display_order int DEFAULT 0
);

INSERT INTO genres (name) VALUES
    ('Фантастика'),
    ('Классика'),
    ('История'),
    ('Детектив'),
    ('Роман'),
    ('Фэнтези'),
    ('Научная литература'),
    ('Поэзия'),
    ('Биография'),
    ('Учебная литература');