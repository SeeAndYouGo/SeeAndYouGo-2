package com.SeeAndYouGo.SeeAndYouGo.MenuDish;

import java.io.Serializable;
import java.util.Objects;

public class MenuDishId implements Serializable {
    private String menu;
    private String dish;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuDishId that = (MenuDishId) o;
        return menu.equals(that.menu) && dish.equals(that.dish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, dish);
    }
}