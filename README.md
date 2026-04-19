# java-filmorate
Template repository for Filmorate project.


[Скачать диаграмму](QuickDBD-filmoratecxema.png)

![ER-диаграмма](Снимокэкрана.png)


## Примеры запросов

### Топ фильмов

SELECT film_id, COUNT(user_id)
FROM film_likes
GROUP BY film_id
ORDER BY COUNT DESC
LIMIT 10;

### Список друзей

SELECT friend_id
FROM friendships
WHERE user_id = ?
  AND status = 'CONFIRMED';

### Список одинаковых друзей

SELECT f1.friend_id
FROM friendships AS f1
JOIN friendships AS f2 ON f1.friend_id = f2.friend_id
WHERE f1.user_id = ?
  AND f2.user_id = ?
  AND f1.status = 'CONFIRMED'
  AND f2.status = 'CONFIRMED';


  Пояснения:

  Так как friend_id и userid в сути своей являются id значениями конкретных людей, то они оба идут от таблицы users
  В базе данных списков не должно быть, поэтому лайки фильмов и списки конкретных друзей выведены в отдельные таблицы
  В методе получения списка друзей на месте полей friend_id и user_id стоят знаки вопроса. Это так называемые плейсхолдеры: на их места подставляются значения

  
