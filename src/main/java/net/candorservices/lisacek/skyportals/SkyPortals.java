package net.candorservices.lisacek.skyportals;

import net.candorservices.lisacek.skyportals.listener.BlockPlaceListener;
import net.candorservices.lisacek.skyportals.manager.PortalManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.BufferPoolMXBean;


public final class SkyPortals extends JavaPlugin implements Listener {

    private static SkyPortals instance;

    private final PortalManager portalManager = new PortalManager(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        getCommand("playerf").setExecutor(new PlayerCommand());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        portalManager.reloadPortals();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static SkyPortals getInstance() {
        return instance;
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {

        for (int x = 0; x < event.getInventory().getMatrix().length; x++) {
            if (event.getInventory().getMatrix()[x] == null) continue;
            Bukkit.getLogger().info("ID: " + x + " Mat: " +
                    event.getInventory().getMatrix()[x].getType().name()
                    + " Amount: " + event.getInventory().getMatrix()[x].getAmount());
        }

    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

}
