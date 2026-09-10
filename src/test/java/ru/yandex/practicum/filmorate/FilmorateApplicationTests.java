package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
    static final FilmController filmController = new FilmController();
    static final UserController userController = new UserController();

    @Test
    void whenFilmNameIsEmpty_throwsException() {
        Film film = new Film(1L, "", "description", LocalDate.of(2020, 12, 25), Duration.ofHours(2));
        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });

        String expectedMessage = "Название не может быть пустым.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenFilmDescriptionLengthIs201_throwsException() {
        String description = "a".repeat(201);
        Film film = new Film(1L, "name", description, LocalDate.of(2020, 12, 25), Duration.ofHours(2));
        film.setDescription(description);

        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });

        String expectedMessage = "Описание не должно превышать 200 символов.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenFilmDescriptionLengthIs200_shouldBeCreated() {
        String description = "a".repeat(200);
        Film film = new Film(1L, "name", description, LocalDate.of(2020, 12, 25), Duration.ofHours(2));
        film.setDescription(description);

        Film createdFilm = filmController.create(film);
        assertEquals(200, createdFilm.getDescription().length());
    }

    @Test
    void whenFilmDescriptionIsNull_descriptionMustBeEmpty() {
        Film film = new Film(
                1L,
                "name",
                null,
                LocalDate.of(2020, 12, 25),
                Duration.ofHours(2)
        );

        Film createdFilm = filmController.create(film);

        assertEquals("", createdFilm.getDescription());
    }

    @Test
    void whenFilmReleaseDateIsNull_throwsException() {
        Film film = new Film(1L, "name", "description", null, Duration.ofHours(2));

        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });

        String expectedMessage = "Дата релиза должна быть указана.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenFilmReleaseDateIsBeforeRequiredTime_throwsException() {
        Film film = new Film(1L, "name", "description", LocalDate.of(1895, 12, 27), Duration.ofHours(2));

        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });

        String expectedMessage = "Дата релиза — не раньше 28 декабря 1895 года.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenFilmDurationIsNegative_throwsException() {
        Film film = new Film(1L, "name", "description", LocalDate.of(1895, 12, 28), Duration.ofHours(-2));

        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });

        String expectedMessage = "Продолжительность фильма должна быть положительной.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenFilmDurationIsZero_throwsException() {
        Film film = new Film(1L, "name", "description", LocalDate.of(1895, 12, 28), Duration.ofHours(0));

        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });

        String expectedMessage = "Продолжительность фильма должна быть положительной.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenUserEmailIsEmpty_throwsException() {
        User user = new User(
                1L,
                "",
                "login",
                "name",
                LocalDate.of(2001, 12, 25)
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });

        String expectedMessage = "Имейл должен быть указан.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenUserEmailIsNotContainsAtSign_throwsException() {
        User user = new User(
                1L,
                "privet.com",
                "login",
                "name",
                LocalDate.of(2001, 12, 25)
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });

        String expectedMessage = "Имейл должен содержать '@'.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenUserLoginIsEmpty_throwsException() {
        User user = new User(
                1L,
                "privet@gmail.com",
                "",
                "name",
                LocalDate.of(2001, 12, 25)
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });

        String expectedMessage = "Логин должен быть указан";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenUserLoginIsContainsSpace_throwsException() {
        User user = new User(
                1L,
                "privet@gmail.com",
                "login log",
                "name",
                LocalDate.of(2001, 12, 25)
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });

        String expectedMessage = "Логин не должен содержать пробелов";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenUserNameIsEmpty_replaceWithLogin() {
        User user = new User(
                1L,
                "privet@gmail.com",
                "login",
                "",
                LocalDate.of(2001, 12, 25)
        );

        User createUser = userController.create(user);

        assertEquals(user.getLogin(), createUser.getName());
    }

    @Test
    void whenUserBirthdayIsEmpty_throwsException() {
        User user = new User(
                1L,
                "privet@gmail.com",
                "login",
                "name",
                null
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });

        String expectedMessage = "Дата рождения должна быть указана.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenUserBirthdayIsInTheFuture_throwsException() {
        User user = new User(
                1L,
                "privet@gmail.com",
                "login",
                "name",
                LocalDate.of(2222, 12, 25)
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });

        String expectedMessage = "Дата рождения не может быть в будущем.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void update_whenUserIdIsEmpty_throwsException() {
        User user = new User(
                null,
                "privet@gmail.com",
                "login",
                "name",
                LocalDate.of(2222, 12, 25)
        );

        Exception exception = assertThrows(ValidationException.class, () -> {
            userController.update(user);
        });

        String expectedMessage = "ID должен быть указан";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void update_whenUserAnEmailIsUsed_throwsException() {
        User user = new User(
                0L,
                "privet@gmail.com",
                "login",
                "name",
                LocalDate.of(2000, 12, 25)
        );

        userController.create(user);

        User user1 = new User(
                1L,
                "privet1@gmail.com",
                "login",
                "name",
                LocalDate.of(2000, 12, 25)
        );

        userController.create(user1);

        User user2 = new User(
                1L,
                "privet1@gmail.com",
                "login",
                "name",
                LocalDate.of(2000, 12, 25)
        );

        Exception exception = assertThrows(DuplicatedDataException.class, () -> {
            userController.update(user2);
        });

        String expectedMessage = "Этот имейл уже используется";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void update_whenFilmIdIsEmpty_throwsException() {
        Film film = new Film(null, "name", "description", LocalDate.of(1895, 12, 28), Duration.ofHours(2));

        Exception exception = assertThrows(ValidationException.class, () -> {
            filmController.update(film);
        });

        String expectedMessage = "ID должен быть указан";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
