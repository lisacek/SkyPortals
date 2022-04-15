package net.candorservices.lisacek.skyportals.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.candorservices.lisacek.skyportals.SkyPortals;
import net.candorservices.lisacek.skyportals.cons.Portal;
import net.candorservices.lisacek.skyportals.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PortalMenu implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("portal-menu")
            .provider(new PortalMenu())
            .size(3, 9)
            .manager(SkyPortals.getInstance().getInvManag())
            .title(Colors.translateColors(SkyPortals.getInstance().getPortals().getString("portals.title")))
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {
        update(player, contents);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        Portal portal = SkyPortals.getInstance().getPortal(player);
        ConfigurationSection gui = SkyPortals.getInstance().getPortals().getConfigurationSection("portals." + portal.getName() + ".gui");
        gui.getKeys(false).forEach(key -> {
            switch (key) {
                case "delete" -> {
                    ItemStack delete = buildStack(gui, key);
                    contents.set(gui.getInt(key + ".row"), gui.getInt(key + ".column"), ClickableItem.of(delete, e -> {
                        SkyPortals.getInstance().getPortalManager().placePortal(portal.getLocation(), player, portal.getName(), portal.getFace(), true);
                        INVENTORY.close(player);
                        player.sendMessage(Colors.translateColors(SkyPortals.getInstance().getMessages().getString("portal-destroyed")));
                    }));
                }
                case "clear" -> {
                    ItemStack clear = buildStack(gui, key);
                    contents.set(gui.getInt(key + ".row"), gui.getInt(key + ".column"), ClickableItem.of(clear, e -> {
                        List<Entity> nearby = player.getNearbyEntities(5, 5, 5);
                        int i = 0;
                        for (Entity entity : nearby) {
                            if (entity instanceof ArmorStand) {
                                entity.remove();
                                i++;
                            }
                        }
                        if (i == 0) {
                            player.sendMessage(Colors.translateColors(SkyPortals.getInstance().getMessages().getString("portal-no-hologram")));
                        } else {
                            player.sendMessage(Colors.translateColors(SkyPortals.getInstance().getMessages().getString("portal-removed-hologram")));
                        }
                        INVENTORY.close(player);
                    }));
                }
                default -> {
                    ItemStack stack = buildStack(gui, key);
                    contents.set(gui.getInt(key + ".row"), gui.getInt(key + ".column"), ClickableItem.empty(stack));
                }
            }
        });

    }

    private ItemStack buildStack(ConfigurationSection gui, String key) {
        ItemStack stack = new ItemStack(Material.valueOf(gui.getString(key + ".material")));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Colors.translateColors(gui.getString(key + ".name")));
        meta.setLore(Colors.translateColors(gui.getStringList(key + ".lore")));
        meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES);
        meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
        meta.hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
        meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE);
        stack.setItemMeta(meta);
        return stack;
    }
}
