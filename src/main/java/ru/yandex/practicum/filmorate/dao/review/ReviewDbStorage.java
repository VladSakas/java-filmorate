package ru.yandex.practicum.filmorate.dao.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;

    private static final String ADD_QUERY =
            "INSERT INTO reviews (content, user_id, is_positive, film_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";
    private static final String GET_BY_ID_QUERY =
            "SELECT r.*, COALESCE(SUM(ru.useful), 0) AS useful_rating " +
                    "FROM reviews r " +
                    "LEFT JOIN review_useful ru ON r.id = ru.review_id " +
                    "WHERE r.id = ? " +
                    "GROUP BY r.id";
    private static final String ADD_LIKE_QUERY =
            "MERGE INTO review_useful (review_id, user_id, useful) KEY (review_id, user_id) VALUES (?, ?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM review_useful WHERE review_id = ? AND user_id = ?";

    @Override
    public Review addReview(Review review) {
        String sql = ADD_QUERY;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, review.getContent());
            ps.setLong(2, review.getUserId());
            ps.setBoolean(3, review.getIsPositive());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            review.setReviewId(keyHolder.getKey().longValue());
        }

        review.setUseful(0);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = UPDATE_QUERY;
        jdbc.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());

        return getReviewById(review.getReviewId()).orElse(review);
    }

    @Override
    public void deleteReview(Long id) {
        String sql = DELETE_QUERY;
        jdbc.update(sql, id);
    }

    @Override
    public List<Review> getReviews(Long filmId, int count) {
        String sql;
        List<Review> reviews;

        String baseSql = "SELECT r.*, COALESCE(SUM(ru.useful), 0) AS useful_rating " +
                "FROM reviews r " +
                "LEFT JOIN review_useful ru ON r.id = ru.review_id ";

        String endSql = "GROUP BY r.id ORDER BY useful_rating DESC LIMIT ?";

        if (filmId == null) {
            sql = baseSql + endSql;
            reviews = jdbc.query(sql, this::mapRowToReview, count);
        } else {
            sql = baseSql + "WHERE r.film_id = ? " + endSql;
            reviews = jdbc.query(sql, this::mapRowToReview, filmId, count);
        }

        return reviews;
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        String sql = GET_BY_ID_QUERY;
        try {
            Review review = jdbc.queryForObject(sql, this::mapRowToReview, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long id, Long userId, int like) {
        String sql = ADD_LIKE_QUERY;
        jdbc.update(sql, id, userId, like);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        String sql = REMOVE_LIKE_QUERY;
        jdbc.update(sql, id, userId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful_rating"));
        return review;
    }
}
