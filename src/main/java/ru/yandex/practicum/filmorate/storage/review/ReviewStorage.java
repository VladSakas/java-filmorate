package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Long id);

    List<Review> getReviews(Long filmId, int count);

    Optional<Review> getReviewById(Long id);

    void addLike(Long id, Long userId, int like);

    void removeLike(Long id, Long userId);
}
