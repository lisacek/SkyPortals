package net.candorservices.lisacek.skyportals.listener;

import net.candorservices.lisacek.skyportals.SkyPortals;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if(event.getBlock().getType() == Material.CHEST) {
            double rotation = ((event.getPlayer().getLocation().getYaw() - 90.0F) % 360.0F);
            if (rotation < 0.0D)
                rotation += 360.0D;
            if (45.0D <= rotation && rotation < 135.0D) {
                SkyPortals.getInstance().getPortalManager().placePortal(event.getBlock().getLocation(), "lol", "south", false);
            } else if (225.0D <= rotation && rotation < 315.0D) {
                SkyPortals.getInstance().getPortalManager().placePortal(event.getBlock().getLocation(), "lol", "north", false);
            } else if (135.0D <= rotation && rotation < 225.0D) {
                SkyPortals.getInstance().getPortalManager().placePortal(event.getBlock().getLocation(), "lol", "west", false);
            } else if (0.0D <= rotation && rotation < 45.0D) {
                SkyPortals.getInstance().getPortalManager().placePortal(event.getBlock().getLocation(), "lol", "east", false);
            } else if (315.0D <= rotation && rotation < 360.0D) {
                SkyPortals.getInstance().getPortalManager().placePortal(event.getBlock().getLocation(), "lol", "east", false);
            }
            event.setCancelled(true);
        }
    }



}
