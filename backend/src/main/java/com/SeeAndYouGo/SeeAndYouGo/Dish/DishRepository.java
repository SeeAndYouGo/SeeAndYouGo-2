package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DishRepository {
    private final EntityManager em;

    public void save(Dish dish) {
        em.persist(dish);
    }

    public void saveAll(List<Dish> dishes) {
        for (Dish dish : dishes) {
            em.persist(dish);
        }
    }

    public Dish findOne(Long id) {
        return em.find(Dish.class, id);
    }

    public List<Dish> findAll() {
        return em.createQuery("SELECT m FROM Dish m", Dish.class)
                .getResultList();
    }
}


