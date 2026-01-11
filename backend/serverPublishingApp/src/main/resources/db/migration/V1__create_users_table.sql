create table users (
    id serial primary key,
    username varchar(50) not null unique,
    email varchar(100) unique,
    phone varchar(13) not null unique,
    password varchar(255) not null,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    middle_name varchar(50),
    role varchar(20) not null default 'author'
);