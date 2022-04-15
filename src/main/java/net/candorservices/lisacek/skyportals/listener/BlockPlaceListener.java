package net.candorservices.lisacek.skyportals.listener;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.candorservices.lisacek.skyportals.SkyPortals;
import net.candorservices.lisacek.skyportals.cons.Portal;
import net.candorservices.lisacek.skyportals.gui.PortalMenu;
import net.candorservices.lisacek.skyportals.manager.PortalManager;
import net.candorservices.lisacek.skyportals.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) return;
            NBTItem nbti = new NBTItem(event.getPlayer().getInventory().getItemInMainHand());
            if (nbti.hasKey("portal")) {

                Portal portal = SkyPortals.getInstance().getPortalManager().getNearestPortal(event.getClickedBlock().getLocation().clone(), 5);
                if (portal != null) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Colors.translateColors(SkyPortals.getInstance().getMessages().getString("portal-already-nearby")));
                    return;
                }

                double rotation = ((event.getPlayer().getLocation().getYaw() - 90.0F) % 360.0F);
                if (rotation < 0.0D)
                    rotation += 360.0D;
                if (45.0D <= rotation && rotation < 135.0D) {
                    SkyPortals.getInstance().getPortalManager().placePortal(event.getClickedBlock().getLocation(), event.getPlayer(), nbti.getString("portal"), "south", false);
                } else if (225.0D <= rotation && rotation < 315.0D) {
                    SkyPortals.getInstance().getPortalManager().placePortal(event.getClickedBlock().getLocation(), event.getPlayer(), nbti.getString("portal"), "north", false);
                } else if (135.0D <= rotation && rotation < 225.0D) {
                    SkyPortals.getInstance().getPortalManager().placePortal(event.getClickedBlock().getLocation(), event.getPlayer(), nbti.getString("portal"), "west", false);
                } else if (0.0D <= rotation && rotation < 45.0D) {
                    SkyPortals.getInstance().getPortalManager().placePortal(event.getClickedBlock().getLocation(), event.getPlayer(), nbti.getString("portal"), "east", false);
                } else if (315.0D <= rotation && rotation < 360.0D) {
                    SkyPortals.getInstance().getPortalManager().placePortal(event.getClickedBlock().getLocation(), event.getPlayer(), nbti.getString("portal"), "east", false);
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            Portal portal = SkyPortals.getInstance().getPortalManager().getNearestPortal(event.getClickedBlock().getLocation().clone(), 5);
            if (portal == null) return;
            if (!portal.getPlacedBy().equalsIgnoreCase(event.getPlayer().getName()) && !event.getPlayer().hasPermission("skyportals.bypass")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Colors.translateColors(SkyPortals.getInstance().getMessages().getString("portal-use-other")));
                return;
            }
            SkyPortals.getInstance().setPortal(event.getPlayer(), portal);
            PortalMenu.INVENTORY.open(event.getPlayer());
        }
    }


    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            Portal portal = SkyPortals.getInstance().getPortalManager().getNearestPortal(event.getFrom().getBlock().getLocation().clone(), 5);
            if (portal == null) return;

            ConfigurationSection portalDest = SkyPortals.getInstance().getPortals().getConfigurationSection("portals." + portal.getName() + ".destination");

            World w = Bukkit.getWorld(portalDest.getString("world"));
            double x = portalDest.getDouble("x");
            double y = portalDest.getDouble("y");
            double z = portalDest.getDouble("z");
            double yaw = portalDest.getDouble("yaw");
            double pitch = portalDest.getDouble("pitch");

            Location loc = new Location(w, x, y, z, (float) yaw, (float) pitch);
            event.setTo(loc);
            event.setCanCreatePortal(false);
            event.setCancelled(true);
            event.getPlayer().teleport(loc);
        }
    }

}
