package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantRepository {
    private final EntityManager em;

    public void save(Restaurant restaurant) {
        em.persist(restaurant);
    }

    public void saveAll(List<Restaurant> restaurants) {
        for (Restaurant restaurant : restaurants) {
            em.persist(restaurant);
        }
    }

    public Restaurant findOne(Long id) {
        return em.find(Restaurant.class, id);
    }

    public List<Restaurant> findAll() {
        return em.createQuery("SELECT r FROM Restaurant r", Restaurant.class)
                .getResultList();
    }
    public Long countNumberOfDataInDate(String name, String today){
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(*) FROM Restaurant r "+
                "WHERE r.name = :name " +
                "AND r.date = :today",
                Long.class
        );
        query.setParameter("name", name);
        query.setParameter("today", today);
        return query.getSingleResult();
    }

    public Long countNumberOfDataInDate(String today){
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(*) FROM Restaurant r "+
                        "WHERE r.date = :today",
                Long.class
        );
        query.setParameter("today", today);
        return query.getSingleResult();
    }

    public Restaurant findByName(String name) {
        try {
            return em.createQuery("select r from Restaurant r where r.name = :name", Restaurant.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void deleteRestaurantsMatchedDate(String date) {
    }

    public List<Restaurant> findTodayAllRestaurant(String name, String date) {

        TypedQuery<Restaurant> query = em.createQuery(
                "SELECT r FROM Restaurant r " +
                        "WHERE r.name = :name AND r.date = :date",
                Restaurant.class
        );
        query.setParameter("name", name);
        query.setParameter("date", date);
        return query.getResultList();
    }

    public Restaurant findTodayRestaurant(String name, String date) {

        TypedQuery<Restaurant> query = em.createQuery(
                "SELECT r FROM Restaurant r " +
                        "WHERE r.name = :name " +
                        "AND r.date = :date",
                Restaurant.class
        );
        query.setParameter("name", name);
        query.setParameter("date", date);
        return query.setMaxResults(1).getSingleResult();
    }

}
