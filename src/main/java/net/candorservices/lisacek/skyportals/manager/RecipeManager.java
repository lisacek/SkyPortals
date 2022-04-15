package net.candorservices.lisacek.skyportals.manager;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import net.candorservices.lisacek.skyportals.ConditionType;
import net.candorservices.lisacek.skyportals.SkyPortals;
import net.candorservices.lisacek.skyportals.cons.Recipe;
import net.candorservices.lisacek.skyportals.cons.RecipeItem;
import net.candorservices.lisacek.skyportals.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RecipeManager {

    private static final RecipeManager INSTANCE = new RecipeManager();

    private final List<Recipe> recipes = new ArrayList<>();

    private RecipeManager() {
    }

    public void loadRecipes() {
        loadRecipesFromFile(SkyPortals.getInstance().getPortals(), "portals");
        loadRecipesFromFile(SkyPortals.getInstance().getItems(), "items");
    }

    private void loadRecipesFromFile(YamlConfiguration config, String section) {
        ConfigurationSection craftItems = config.getConfigurationSection(section);
        craftItems.getKeys(false).forEach(portal -> {
            ConfigurationSection portalSection = craftItems.getConfigurationSection(portal + ".item");
            if (portalSection != null) {
                ItemStack result = new ItemStack(Material.valueOf(portalSection.getString("material", "STONE")), portalSection.getInt("amount", 1));
                if (portalSection.getBoolean("nbt-settings.enabled", false)) {
                    if (portalSection.getBoolean("nbt-settings.base64.enabled", false) && portalSection.getString("material", "STONE").equalsIgnoreCase("PLAYER_HEAD")) {
                        String textureValue = portalSection.getString("nbt-settings.base64.value", "0");
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
                        NBTItem nbti = new NBTItem(head);
                        NBTCompound skull = nbti.addCompound("SkullOwner");
                        skull.setString("Name", portal);
                        NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
                        texture.setString("Value", textureValue);
                        result = nbti.getItem();
                    }

                    if (portalSection.getBoolean("nbt-settings.enabled", false)) {
                        NBTItem nbti = new NBTItem(result);
                        nbti.setString(portalSection.getString("nbt-settings.key", "portal"), portalSection.getString("nbt-settings.value", "portal"));
                        result = nbti.getItem();
                    }
                }

                ItemMeta meta = result.getItemMeta();
                meta.setDisplayName(Colors.translateColors(portalSection.getString("name", "&cPortal")));
                meta.setLore(Colors.translateColors(portalSection.getStringList("lore")));
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_DYE);
                meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                if (portalSection.getConfigurationSection("enchantments") != null) {
                    portalSection.getConfigurationSection("enchantments").getKeys(false).forEach(enchantment -> {
                        ConfigurationSection enchantmentSection = portalSection.getConfigurationSection("enchantments." + enchantment);
                        Enchantment ench = Enchantment.getByName(enchantmentSection.getString("name", ""));
                        if (ench != null) {
                            meta.addEnchant(ench, portalSection.getInt("enchantments." + enchantment, enchantmentSection.getInt("level")), true);
                        }
                    });
                }
                result.setItemMeta(meta);

                Recipe recipe = new Recipe(portal, result);

                Map<String, RecipeItem> recipeItems = new HashMap<>();
                ConfigurationSection items = portalSection.getConfigurationSection("recipe.items");
                items.getKeys(false).forEach(item -> {
                    ConfigurationSection itemSection = items.getConfigurationSection(item);
                    boolean isNBT = itemSection.getBoolean("nbt.enabled", false);
                    RecipeItem recipeItem = new RecipeItem(isNBT ? ConditionType.NBT : ConditionType.MATERIAL, Material.valueOf(itemSection.getString("material", "STONE")),
                            itemSection.getInt("amount", 1), isNBT ? itemSection.getString("nbt.key", null) : null, isNBT ? itemSection.getString("nbt.value", null) : null);
                    recipeItems.put(item, recipeItem);
                });

                List<String> pattern = portalSection.getStringList("recipe.pattern");
                AtomicInteger i = new AtomicInteger(0);
                pattern.forEach(line -> {
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        if (!part.equals("x")) {
                            recipe.setRecipeItem(i.get(), recipeItems.get(part));
                        }
                        i.getAndIncrement();
                    }
                });

                recipes.add(recipe);
            }
        });
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public static RecipeManager getInstance() {
        return INSTANCE;
    }

}
