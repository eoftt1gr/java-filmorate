package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateFilm(film);

        long id = getNextId();
        log.trace("Присвоение ID фильму: {}", id);
        film.setId(id);

        log.trace("Добавление фильма в базу данных.");
        films.put(film.getId(), film);

        log.info("Фильм с id = {} успешно добавлен.", id);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Валидация фильма не пройдена: не указан ID");
            throw new ValidationException("ID должен быть указан.");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null) {
                log.debug("Обновление названия фильма.");
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null) {
                log.debug("Обновление описания фильма.");
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getDuration() != null) {
                log.debug("Обновление продолжительности фильма.");
                oldFilm.setDuration(newFilm.getDuration());
            }

            if (newFilm.getReleaseDate() != null) {
                log.debug("Обновление даты релиза фильма.");
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            validateFilm(oldFilm);

            log.info("Фильм с id = {} успешно обновлён.", newFilm.getId());
            return oldFilm;
        }
        log.error("Фильм с id = " + newFilm.getId() + " не найден");
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void validateFilm(Film film) {
        log.trace("Общая валидация фильма.");
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Валидация фильма не пройдена: пустое название.");
            throw new ValidationException("Название не может быть пустым.");
        }

        if (film.getDescription() == null) {
            log.debug("Описание фильма не указано, устанавливаем пустое описание.");
            film.setDescription("");
        } else if (film.getDescription().length() > 200) {
            log.error("Валидация фильма не пройдена: описание превышает 200 символов.");
            throw new ValidationException("Описание не должно превышать 200 символов.");
        }

        if (film.getReleaseDate() == null) {
            log.error("Валидация фильма не пройдена: не указана дата релиза.");
            throw new ValidationException("Дата релиза должна быть указана.");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Валидация фильма не пройдена: дата релиза не может быть раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года.");
        }

        if (film.getDuration().isZero() || film.getDuration().isNegative()) {
            log.error("Валидация фильма не пройдена: продолжительность фильма {}.", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }
        log.trace("Валидация пройдена.");
    }
}
