package net.candorservices.lisacek.skyportals.cons;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.candorservices.lisacek.skyportals.ConditionType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RecipeItem {

    private final ConditionType type;
    private final Material material;
    private final int amount;
    private final String key;
    private final String value;

    public RecipeItem(ConditionType type, Material material, int amount, String key, String value) {
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.key = key;
        this.value = value;
    }

    public boolean isValid(ItemStack stack, ConditionType type) {
        if (type == ConditionType.MATERIAL) {
            return stack.getType().name().equals(material.name()) && stack.getAmount() == amount;
        } else if (stack.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(stack);
            return nbtItem.hasKey(key) && nbtItem.getString(key).equals(value) && stack.getAmount() == amount;
        }
        return false;
    }

    public int getAmount() {
        return amount;
    }

    public ConditionType getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }


}
