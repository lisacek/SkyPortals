package net.candorservices.lisacek.skyportals;

import net.candorservices.lisacek.skyportals.cons.Portal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class AdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) return true;
        switch (args[0]) {
            case "save":
                switch (args[2]) {
                    case "west", "east", "north", "south" -> {
                        File file = new File(SkyPortals.getInstance().getDataFolder(),"/portals/" + args[1] + "/schematics/");
                        if (!file.exists()) file.mkdirs();
                        SkyPortals.getInstance().getPortalManager().savePortal(player.getLocation(), file, args[2] + ".json");
                    }
                    default -> player.sendMessage("Invalid direction");
                }
                break;
            case "delete":
                Portal portal = SkyPortals.getInstance().getPortalManager().getPlacedPortals().get(0);
                SkyPortals.getInstance().getPortalManager().placePortal(portal.getLocation(), (Player) sender,  portal.getName(), portal.getFace(), true);
                break;
            case "load":
                SkyPortals.getInstance().getPortalManager().placePortal(player.getLocation(), (Player) sender, args[1], args[2], false);
                break;
        }
        return true;
    }
}
