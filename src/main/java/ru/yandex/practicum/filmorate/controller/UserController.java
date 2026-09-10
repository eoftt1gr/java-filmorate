package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    public void validateUser(User user) {
        log.trace("Общая валидация пользователя.");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Валидация пользователя не пройдена: не указан имейл.");
            throw new ValidationException("Имейл должен быть указан.");
        } else if (!user.getEmail().contains("@")) {
            log.error("Валидация пользователя не пройдена: имейл не содержит '@'.");
            throw new ValidationException("Имейл должен содержать '@'.");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Валидация пользователя не пройдена: логин не указан.");
            throw new ValidationException("Логин должен быть указан");
        } else if (user.getLogin().contains(" ")) {
            log.error("Валидация пользователя не пройдена: логин содержит пробелы.");
            throw new ValidationException("Логин не должен содержать пробелов");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано: будет присвоен логин пользователя.");
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null) {
            log.error("Валидация пользователя не пройдена: не указана дата рождения.");
            throw new ValidationException("Дата рождения должна быть указана.");
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Валидация пользователя не пройдена: дата рождения указана в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        log.trace("Валидация пройдена.");
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);

        long id = getNextId();
        log.trace("Присвоение ID пользователю: {}", id);
        user.setId(id);

        log.trace("Добавление пользователя в базу данных.");
        users.put(user.getId(), user);

        log.info("Пользователь с id = {} успешно добавлен", id);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Валидация пользователя не пройдена: не указан ID пользователя.");
            throw new ValidationException("ID должен быть указан");
        }

        if (newUser.getEmail() != null) {
            boolean emailExists = users.entrySet().stream()
                    .anyMatch(entry -> entry.getValue().getEmail().equals(newUser.getEmail())
                            && !entry.getKey().equals(newUser.getId()));

            if (emailExists) {
                log.error("Валидация пользователя не пройдена: указанный имейл уже используется.");
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getEmail() != null) {
                log.debug("Обновление имейла пользователя.");
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getName() != null) {
                log.debug("Обновление имени пользователя.");
                oldUser.setName(newUser.getName());
            }

            if (newUser.getLogin() != null) {
                log.debug("Обновление логина пользователя.");
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getBirthday() != null) {
                log.debug("Обновление даты рождения пользователя.");
                oldUser.setBirthday(newUser.getBirthday());
            }

            validateUser(oldUser);

            log.info("Пользователь с id = {} успешно обновлён.", newUser.getId());
            return oldUser;
        }
        log.error("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
