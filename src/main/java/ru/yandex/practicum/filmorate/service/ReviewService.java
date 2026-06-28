package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    public Review addReview(Review review) {
        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());

        Review createdReview = reviewStorage.addReview(review);

        eventService.createEvent(
                review.getUserId(),
                EventType.REVIEW,
                Operation.ADD,
                createdReview.getReviewId()
        );

        log.info("Добавлен новый отзыв с ID: {}", createdReview.getReviewId());
        return createdReview;
    }

    public Review updateReview(Review review) {
        getReviewById(review.getReviewId());

        Review updatedReview = reviewStorage.updateReview(review);

        eventService.createEvent(
                review.getUserId(),
                EventType.REVIEW,
                Operation.UPDATE,
                review.getReviewId()
        );

        log.info("Обновлен отзыв с ID: {}", updatedReview.getReviewId());
        return updatedReview;
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        reviewStorage.deleteReview(id);

        eventService.createEvent(
                review.getUserId(),
                EventType.REVIEW,
                Operation.REMOVE,
                id
        );

        log.info("Удален отзыв с ID: {}", id);
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> {
                    log.warn("Отзыв с ID {} не найден", id);
                    return new NotFoundException("Отзыв с ID " + id + " не найден");
                });
    }

    public List<Review> getReviews(Long filmId, int count) {

        if (filmId != null) {
            validateFilmExists(filmId);
        }

        log.info("Запрошено {} отзывов для фильма с ID: {}", count, filmId);
        return reviewStorage.getReviews(filmId, count);
    }

    public void addLike(Long id, Long userId) {
        getReviewById(id);
        validateUserExists(userId);

        reviewStorage.addLike(id, userId, 1);
        log.info("Пользователь {} поставил лайк отзыву {}", userId, id);
    }

    public void addDislike(Long id, Long userId) {
        getReviewById(id);
        validateUserExists(userId);

        reviewStorage.addLike(id, userId, -1);
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        getReviewById(id);
        validateUserExists(userId);

        reviewStorage.removeLike(id, userId);
        log.info("Пользователь {} удалил свою оценку у отзыва {}", userId, id);
    }

    private void validateUserExists(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void validateFilmExists(Long filmId) {
        if (filmStorage.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }
}
