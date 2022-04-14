package net.candorservices.lisacek.skyportals;

import com.sk89q.worldedit.WorldEditException;
import net.candorservices.lisacek.skyportals.cons.Portal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class PlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) return true;
        switch (args[0]) {
            case "save":
                switch (args[2]) {
                    case "west", "east", "north", "south" -> {
                        File file = new File(SkyPortals.getInstance().getDataFolder(),"/portals/" + args[1] + "/schematics/");
                        SkyPortals.getInstance().getPortalManager().savePortal(player.getLocation(), file, args[2] + ".json");
                    }
                    default -> player.sendMessage("Invalid direction");
                }
                break;
            case "delete":
                Portal portal = SkyPortals.getInstance().getPortalManager().getPlacedPortals().get(0);
                SkyPortals.getInstance().getPortalManager().placePortal(portal.getLocation(), portal.getName(), portal.getFace(), true);
                break;
            case "load":
                SkyPortals.getInstance().getPortalManager().placePortal(player.getLocation(), args[1], args[2], false);
                break;
            case "create":
                if (args.length < 2) {
                    player.sendMessage("Usage: /portal create <name>");
                    return true;
                }
                String name = args[1];
                File file = new File(SkyPortals.getInstance().getDataFolder(), name);
                if(!file.exists()) file.mkdir();
                file = new File(file, "portal.yml");
                if (file.exists()) {
                    player.sendMessage("Portal already exists");
                } else {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                file = new File(file, "schematics");
                if(!file.exists()) file.mkdir();
        }
        return true;
    }
}
