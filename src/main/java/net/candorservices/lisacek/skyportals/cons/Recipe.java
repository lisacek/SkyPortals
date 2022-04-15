package net.candorservices.lisacek.skyportals.cons;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class Recipe {

    private final String name;

    private final ItemStack result;

    private final RecipeItem[] recipeItems = new RecipeItem[9];

    public Recipe(String name, ItemStack result) {
        this.name = name;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public int getRecipeItemsAmount() {
      return Arrays.stream(recipeItems).filter(Objects::nonNull).toArray().length;
    }

    public ItemStack getResult() {
        return result;
    }

    public RecipeItem[] getRecipeItems() {
        return recipeItems;
    }

    public void setRecipeItem(int index, RecipeItem recipeItem) {
        recipeItems[index] = recipeItem;
    }

    public RecipeItem getRecipeItem(int index) {
        return recipeItems[index];
    }

}
