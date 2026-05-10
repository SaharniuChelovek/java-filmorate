-- 1. Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE NOT NULL
);

-- 2. Таблица рейтингов (MPA)
CREATE TABLE IF NOT EXISTS ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

-- 3. Таблица фильмов (связь с ratings)
CREATE TABLE IF NOT EXISTS films (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    rating_id INT,
    CONSTRAINT fk_film_rating FOREIGN KEY (rating_id) REFERENCES ratings(id)
);

-- 4. Таблица жанров
CREATE TABLE IF NOT EXISTS genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- 5. Таблица лайков фильмов (составной первичный ключ и внешние ключи)
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_likes_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

-- 6. Таблица дружбы (составной первичный ключ, статус и внешние ключи)
CREATE TABLE IF NOT EXISTS friendships (
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    -- Добавили ON DELETE CASCADE к обоим внешним ключам
    CONSTRAINT fk_friend_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

-- 7. Таблица связи фильмов и жанров (составной первичный ключ и внешние ключи)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT NOT NULL,
    genre_id INT NOT NULL,
    CONSTRAINT fk_genre_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_genre_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);