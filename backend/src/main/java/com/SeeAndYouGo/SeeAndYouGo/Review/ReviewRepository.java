package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {
    @PersistenceContext
    private final EntityManager em;

    public void save(Review review) {
        em.persist(review);
    }

    public Review findOne(Long id) {
        return em.find(Review.class, id);
    }

    public Optional<Review> findById(Long id) {
        Review review = em.find(Review.class, id);
        return Optional.ofNullable(review);
    }

    public List<Review> findAll() {
        return em.createQuery("select  r from Review r", Review.class)
                .getResultList();
    }

    public List<Review> findByWriter(String writer) {
        return em.createQuery("select r from Review r where r.writer = :writer", Review.class)
                .setParameter("writer", writer)
                .getResultList();
    }

    public List<Review> findByMenu(Menu menu) {
        return em.createQuery("select r from Review r where r.menu = :menu", Review.class)
                .setParameter("menu", menu)
                .getResultList();
    }

    /**
     * 특정 평점 이상의 리뷰 검색
     */
    public List<Review> findByReviewRateGreaterThan(Double reviewRate) {
        return em.createQuery("select r from Review r where r.reviewRate >= :reviewRate", Review.class)
                .setParameter("reviewRate", reviewRate)
                .getResultList();
    }

    /**
     * 좋아요 개수 순으로 리뷰 정렬해서 검색
     */
    public List<Review> findAllOrderByLikeCountDesc() {
        return em.createQuery("select r from Review r order by r.likeCount desc", Review.class)
                .getResultList();
    }

    /**
     * 특정 시간 이후에 작성된 리뷰 검색
     */
    public List<Review> findByMadeTimeAfter(LocalDateTime time) {
        return em.createQuery("select r from Review r where r.madeTime > :time", Review.class)
                .setParameter("time", time)
                .getResultList();
    }

    /**
     * 이미지 링크가 있는 리뷰만 검색
     */
    public List<Review> findByImgLinkIsNotNull() {
        return em.createQuery("select r from Review r where r.imgLink is not null", Review.class)
                .getResultList();
    }

    public List<Review> findTopReviewsByRestaurantAndDate(String restaurantName, String date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
        LocalDateTime startDate = parsedDate.atTime(0,0,0); //.atStartOfDay();

        TypedQuery<Review> reviewTypedQuery = em.createQuery(
                "select r from Review r " +
                        "where r.restaurant.name = :restaurantName " +
                        "and FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
                        "order by r.madeTime desc", Review.class)
                .setParameter("restaurantName", restaurantName)
                .setParameter("date", startDate)
                .setMaxResults(5);
        return reviewTypedQuery.getResultList();
    }

    public List<Review> findAllByMadeTime(String date) {
        TypedQuery<Review> reviewTypedQuery = em.createQuery(
                        "select r from Review r " +
                                "where FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
                                "order by r.madeTime desc", Review.class)
                .setParameter("date", date);
        return reviewTypedQuery.getResultList();
    }

    public List<Review> findReviewsByRestaurantAndDate(String restaurantName, String date) {
        TypedQuery<Review> reviewTypedQuery = em.createQuery(
                        "select r from Review r " +
                                "where FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
                                "and r.restaurant.name = :restaurantName " +
                                "order by r.madeTime desc", Review.class)
                .setParameter("restaurantName", restaurantName)
                .setParameter("date", date);
        return reviewTypedQuery.getResultList();
    }
}
