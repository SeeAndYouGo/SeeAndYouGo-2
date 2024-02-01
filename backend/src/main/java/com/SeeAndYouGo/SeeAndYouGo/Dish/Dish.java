package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.MenuDish.MenuDish;
import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Dish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private DishType dishType;

    @OneToMany(mappedBy = "dish")
    private List<MenuDish> menuDishes = new ArrayList<>();


    @Builder
    public Dish(String name, DishType dishType) {
        this.name = name;
        this.dishType = dishType;
    }

    public List<Menu> getMenus(){
        List<Menu> menus = new ArrayList<>();
        List<Long> menuIds = new ArrayList<>();
        for (MenuDish menuDish : menuDishes) {
            Menu menu = menuDish.getMenu();
            if(!menuIds.contains(menu.getId())){
                menus.add(menu);
                menuIds.add(menu.getId());
            }
        }

        return menus;
    }

    @Override
    public String toString(){
        return name;
    }
}