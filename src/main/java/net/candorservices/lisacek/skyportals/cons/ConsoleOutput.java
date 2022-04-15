package net.candorservices.lisacek.skyportals.cons;

import net.candorservices.lisacek.skyportals.utils.Colors;
import org.bukkit.Bukkit;

public class ConsoleOutput {

    private final String prefix;

    public ConsoleOutput(String prefix) {
        this.prefix = prefix;
    }

    public void info(String message) {
        Bukkit.getLogger().info(Colors.translateColors(prefix + message));
    }

    public void warn(String message) {
        Bukkit.getLogger().warning(Colors.translateColors(prefix + message));
    }

}