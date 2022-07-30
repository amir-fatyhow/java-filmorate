DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS film_userID_liked;
DROP TABLE IF EXISTS film_genre;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS film_mpa;
DROP TABLE IF EXISTS mpa;

CREATE TABLE users (
    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email varchar,
    login varchar,
    birthday date,
    user_name varchar,
    CONSTRAINT data_users UNIQUE (email,login)
);

CREATE TABLE friends (
    user_id int,
    friend_id int,
    friend_status boolean
);

CREATE TABLE films (
    film_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_name varchar,
    description varchar,
    release_date date,
    duration int,
    rate int
);

CREATE TABLE film_userID_liked (
    film_id int,
    user_id int
);

CREATE TABLE film_genre (
    film_id int,
    genre_id int
);

CREATE TABLE genres (
    genre_id int,
    genre varchar
);

CREATE TABLE film_mpa (
    film_id int,
    mpa_id int
);

CREATE TABLE mpa (
    mpa_id int,
    mpa varchar
);
