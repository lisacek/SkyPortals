package net.candorservices.lisacek.skyportals.manager;

import net.candorservices.lisacek.skyportals.SkyPortals;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private static final FileManager INSTANCE = new FileManager();

    private FileManager() {

    }

    public void loadPortals() {
        File customConfigFile = new File(SkyPortals.getInstance().getDataFolder(), "portals.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            SkyPortals.getInstance().saveResource("portals.yml", false);
        }
        SkyPortals.getInstance().setPortals(new YamlConfiguration());
        try {
            SkyPortals.getInstance().getPortals().load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void loadItems() {
        File customConfigFile = new File(SkyPortals.getInstance().getDataFolder(), "items.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            SkyPortals.getInstance().saveResource("items.yml", false);
        }
        SkyPortals.getInstance().setItems(new YamlConfiguration());
        try {
            SkyPortals.getInstance().getItems().load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void loadMessages() {
        File customConfigFile = new File(SkyPortals.getInstance().getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            SkyPortals.getInstance().saveResource("messages.yml", false);
        }
        SkyPortals.getInstance().setMessages(new YamlConfiguration());
        try {
            SkyPortals.getInstance().getMessages().load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static FileManager getInstance() {
        return INSTANCE;
    }
}
