package com.SeeAndYouGo.SeeAndYouGo.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MenuRepository {

    private final EntityManager em;

    public void save(Menu menu) {
        em.persist(menu);
    }

    public void saveAll(List<Menu> menuList) {
        for (Menu menu : menuList) {
            em.persist(menu);
        }
    }

    public Menu findOne(Long id) {
        return em.find(Menu.class, id);
    }

    public List<Menu> findAll() {
        return em.createQuery("SELECT m FROM Menu m", Menu.class)
                .getResultList();
    }

    public List<Menu> findMenusByNameAndDate(Long restaurantId, String date) {
        TypedQuery<Menu> query = em.createQuery(
                "SELECT m FROM Menu m " +
                        "WHERE m.restaurant_id = :restaurant_id " +
                        "AND m.date = :date " +
                        "AND m.type = :type",
                Menu.class
        );

        query.setParameter("restaurant_id", restaurantId);
        query.setParameter("date", date);
        query.setParameter("type", MenuType.LUNCH);

        return query.getResultList();
    }

    public List<Menu> findMenusByDateRange(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        TypedQuery<Menu> query = em.createQuery(
                "SELECT m FROM Menu m " +
                        "WHERE m.restaurant_id = :restaurant_id " +
                        "AND m.date BETWEEN :startDate " +
                        "AND :endDate AND m.type = :type",
                Menu.class
        );

        query.setParameter("restaurant_id", restaurantId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("type", MenuType.LUNCH);

        return query.getResultList();
    }
}