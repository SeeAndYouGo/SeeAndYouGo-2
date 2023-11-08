package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

    public Long countNumberOfData(){
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(*) FROM Dish",
                Long.class
        );
        return query.getSingleResult();
    }


    public Dish findByDishIdentifier(String restaurantName, String mainDishName, Dept dept, String date) {
        TypedQuery<Dish> query = em.createQuery(
                "SELECT d FROM Dish d " +
                        "WHERE d.restaurant.name = :restaurant_name " +
                        "AND d.name = :main_dish_name "+
//                        "AND d.dept = :dept " +
                        "AND d.date = :date ",
                Dish.class);

        query.setParameter("restaurant_name", restaurantName);
        query.setParameter("main_dish_name",  mainDishName);
//        query.setParameter("dept", dept);
        query.setParameter("date", date);
//        query.setParameter("menu_type", MenuType.LUNCH);

        return query.getSingleResult();
    }
}


