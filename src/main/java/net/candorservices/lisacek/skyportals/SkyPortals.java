package net.candorservices.lisacek.skyportals;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.minuskube.inv.InventoryManager;
import net.candorservices.lisacek.skyportals.cons.ConsoleOutput;
import net.candorservices.lisacek.skyportals.cons.Portal;
import net.candorservices.lisacek.skyportals.cons.RecipeItem;
import net.candorservices.lisacek.skyportals.listener.BlockPlaceListener;
import net.candorservices.lisacek.skyportals.manager.FileManager;
import net.candorservices.lisacek.skyportals.manager.PortalManager;
import net.candorservices.lisacek.skyportals.manager.RecipeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SkyPortals extends JavaPlugin implements Listener {

    private static SkyPortals instance;

    private final PortalManager portalManager = new PortalManager(this);

    private YamlConfiguration portals;

    private YamlConfiguration items;

    private YamlConfiguration messages;

    private InventoryManager invManag;

    private final HashMap<Player, Portal> portalCache = new HashMap<>();

    private final List<Location> locationCache = new ArrayList<>();

    private final ConsoleOutput console = new ConsoleOutput("&bSkyPortals &8| &7");

    @Override
    public void onEnable() {
        instance = this;
        invManag = new InventoryManager(this);
        invManag.init();
        FileManager.getInstance().loadPortals();
        FileManager.getInstance().loadItems();
        FileManager.getInstance().loadMessages();
        getCommand("skyportals").setExecutor(new AdminCommand());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        portalManager.reloadPortals();
        RecipeManager.getInstance().loadRecipes();
        console.info("Loaded &b" + RecipeManager.getInstance().getRecipes().size() + " &7recipes!");
        console.info("Plugin was enabled!");

    }

    @Override
    public void onDisable() {
        console.info("Plugin was disabled!");
    }

    public YamlConfiguration getMessages() {
        return messages;
    }

    public void setMessages(YamlConfiguration messages) {
        this.messages = messages;
    }


    public Portal getPortal(Player player) {
        return portalCache.get(player);
    }

    public void setPortal(Player player, Portal portal) {
        portalCache.put(player, portal);
    }

    public static SkyPortals getInstance() {
        return instance;
    }

    public InventoryManager getInvManag() {
        return invManag;
    }

    public void setInvManag(InventoryManager invManag) {
        this.invManag = invManag;
    }

    public YamlConfiguration getPortals() {
        return portals;
    }

    public void setPortals(YamlConfiguration portals) {
        this.portals = portals;
    }

    public YamlConfiguration getItems() {
        return items;
    }

    public void setItems(YamlConfiguration items) {
        this.items = items;
    }

    public List<Location> getLocationCache() {
        return locationCache;
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        if (locationCache.size() == 0) return;
        locationCache.forEach(location -> {
            if (event.getSourceBlock().getLocation().distance(location) < 21) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        RecipeManager.getInstance().getRecipes().forEach(recipe -> {
            List<RecipeItem> validItems = new ArrayList<>();
            for (int x = 0; x < event.getInventory().getMatrix().length; x++) {
                if (event.getInventory().getMatrix()[x] == null) continue;
                RecipeItem recipeItem = recipe.getRecipeItem(x);
                if (recipeItem == null) continue;
                if (recipeItem.isValid(event.getInventory().getMatrix()[x], recipeItem.getType())) {
                    validItems.add(recipeItem);
                }
            }

            if (validItems.size() == recipe.getRecipeItemsAmount() && recipe.getRecipeItemsAmount() != 0) {
                event.getInventory().setResult(recipe.getResult());
            }
        });
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            if (event.getInventory().getType() == InventoryType.WORKBENCH) {
                if (event.getCurrentItem() == null) return;
                if (event.getCurrentItem().getType() == Material.AIR) return;
                if (event.getCursor() == null) return;
                if (event.getCursor().getType() == Material.AIR) return;
                ItemStack stack = event.getCurrentItem().clone();
                NBTItem item = new NBTItem(stack);
                if (item.hasKey("portal")) {
                    event.setCancelled(true);
                }
            }
        }
        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
            if (event.getInventory().getType() == InventoryType.WORKBENCH) {
                if (event.getCurrentItem() == null) return;
                if (event.getCurrentItem().getType() == Material.AIR) return;
                ItemStack stack = event.getCurrentItem().clone();
                NBTItem item = new NBTItem(stack);
                if (item.hasKey("portal")) {
                    event.getInventory().clear();
                    event.setCurrentItem(stack);
                }
            }
        }
    }


    public PortalManager getPortalManager() {
        return portalManager;
    }

}
