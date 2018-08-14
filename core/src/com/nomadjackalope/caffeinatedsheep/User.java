package com.nomadjackalope.caffeinatedsheep;

/**
 * Created by Benjamin on 8/17/2015.
 *
 */
public class User {

    String name;
    int money;
    int dogs;
    int food;
    int wolvesLevel;
    int foodLevel;
    static int idCount = 0;
    int id = idCount++;
    boolean isActive;

    public User() {

    }

    /*public User(String name) {
        this.name = name;
        money = 0;
        wolves = 0;
        food = 0;
        wolvesLevel = 0;
        foodLevel = 0;
    }*/

    public User(String name, int money, int dogs, int food, int wolvesLevel, int foodLevel) {
        this.name = name;
        this.money= money;
        this.dogs = dogs;
        this.food = food;
        this.wolvesLevel = wolvesLevel;
        this.foodLevel = foodLevel;
    }

    @Override
    public String toString() {
        return name;
    }
}
