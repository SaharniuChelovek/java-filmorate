MERGE INTO ratings (id, name) KEY(id) VALUES (1, 'G');
MERGE INTO ratings (id, name) KEY(id) VALUES (2, 'PG');
MERGE INTO ratings (id, name) KEY(id) VALUES (3, 'PG-13');
MERGE INTO ratings (id, name) KEY(id) VALUES (4, 'R');
MERGE INTO ratings (id, name) KEY(id) VALUES (5, 'NC-17');

MERGE INTO genres (id, name) KEY(id) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) KEY(id) VALUES (2, 'Драма');
MERGE INTO genres (id, name) KEY(id) VALUES (3, 'Триллер');
MERGE INTO genres (id, name) KEY(id) VALUES (4, 'Ужасы');
MERGE INTO genres (id, name) KEY(id) VALUES (5, 'Боевик');
MERGE INTO genres (id, name) KEY(id) VALUES (6, 'Мультфильм');