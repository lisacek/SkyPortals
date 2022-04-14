package net.candorservices.lisacek.skyportals.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.candorservices.lisacek.skyportals.SkyPortals;
import net.candorservices.lisacek.skyportals.cons.PlacedBlock;
import net.candorservices.lisacek.skyportals.cons.Portal;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PortalManager {

    private final SkyPortals plugin;

    private final List<Portal> placedPortals = new ArrayList<>();

    public PortalManager(SkyPortals plugin) {
        this.plugin = plugin;
    }

    public void savePortal(Location loc, File file, String name) {
        List<Block> blocks = getBlocksAroundCenter(loc, 10);
        JsonObject blocksObject = new JsonObject();
        blocksObject.add("blocks", new JsonArray());

        for (Block block : blocks) {
            Bukkit.getLogger().info("Block: " + block.getType());
            if (block.getType() == Material.AIR) continue;
            Bukkit.getLogger().info("Block: " + block.getType());
            JsonArray blocksArray = blocksObject.get("blocks").getAsJsonArray();
            JsonObject serializedBlock = new JsonObject();
            serializedBlock.addProperty("material", block.getType().name());
            JsonObject locationObject = new JsonObject();
            locationObject.addProperty("world", block.getLocation().getWorld().getName());
            locationObject.addProperty("x", loc.getBlockX() - block.getLocation().getX());
            locationObject.addProperty("y", loc.getBlockY() - block.getLocation().getY());
            locationObject.addProperty("z", loc.getBlockZ() - block.getLocation().getZ());
            serializedBlock.add("location", locationObject);
            JsonObject state = new JsonObject();
            state.addProperty("data", block.getBlockData().getAsString());
            serializedBlock.add("state", state);
            blocksArray.add(serializedBlock);
        }
        writeJson(blocksObject, file, name);
    }

    public void placePortal(Location loc, String name, String site, boolean delete) {
        try {
            JsonObject blocksObject = loadBlocks(name, site);

            Portal portal = new Portal(name, "yolo", loc, site);

            JsonArray loadedBlocks = blocksObject.get("blocks").getAsJsonArray();
            AtomicInteger i = new AtomicInteger(0);
            List<JsonObject> portalParts = new ArrayList<>();
            List<JsonObject> parts = new ArrayList<>();

            loadedBlocks.forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.get("material").getAsString().equals("NETHER_PORTAL")) {
                    portalParts.add(jsonObject);
                } else {
                    parts.add(jsonObject);
                }
                PlacedBlock block = new PlacedBlock(
                        new Location(Bukkit.getWorld(jsonObject.get("location").getAsJsonObject().get("world").getAsString()),
                                jsonObject.get("location").getAsJsonObject().get("x").getAsInt(),
                                jsonObject.get("location").getAsJsonObject().get("y").getAsInt(),
                                jsonObject.get("location").getAsJsonObject().get("z").getAsInt()), Material.valueOf(jsonObject.get("material").getAsString()));
                if (!delete) {
                    portal.getPlacedBlocks().add(block);
                }
            });

            File f2 = SkyPortals.getInstance().getDataFolder();
            if (!delete) {
                placedPortals.add(portal);
            } else {
                placedPortals.removeIf(portal12 -> portal12 != null && portal12.getLocation().distance(loc) < 3);
            }

            int max = parts.size();
            if (!delete) {
                Bukkit.getScheduler().runTaskTimer(SkyPortals.getInstance(), (task) -> {
                    if (i.get() + 1 >= max) {
                        portalParts.forEach(blockObject -> {
                            updateBlock(loc, blockObject, false);
                        });
                        task.cancel();
                    }
                    JsonObject blockObject = parts.get(i.getAndIncrement()).getAsJsonObject();
                    updateBlock(loc, blockObject, false);
                }, 0L, 1L);
            } else {
                parts.forEach(blockObject -> {
                    deleteBlock(loc, blockObject);
                });
                portalParts.forEach(blockObject -> {
                    deleteBlock(loc, blockObject);
                });
            }

            JsonArray portalsArray = new JsonArray();
            placedPortals.forEach(portal1 -> {
                portalsArray.add(portal1.serializeToJson());
            });
            writeJsonArray(portalsArray, f2, "data.json");
            Bukkit.getLogger().info(placedPortals.toString());
            Bukkit.getLogger().info("Loaded blocks of: " + blocksObject.get("blocks").getAsJsonArray().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteBlock(Location loc, JsonObject blockObject) {
        JsonObject locationObject = blockObject.get("location").getAsJsonObject();
        Block block = loc.getBlock().getRelative(locationObject.get("x").getAsInt(), locationObject.get("y").getAsInt(), locationObject.get("z").getAsInt());
        block.getChunk().setForceLoaded(true);
        Bukkit.getLogger().info("Deleting block: " + block.getType().name());
        Bukkit.getLogger().info("Location: " + block.getX() + " " + block.getY() + " " + block.getZ());
        if (block.getType().name().equals(blockObject.get("material").getAsString())) {
            block.setType(Material.AIR);
        }
    }

    private void updateBlock(Location loc, JsonObject blockObject, boolean delete) {
        JsonObject locationObject = blockObject.get("location").getAsJsonObject();
        Block block = loc.getBlock().getRelative(locationObject.get("x").getAsInt(), locationObject.get("y").getAsInt(), locationObject.get("z").getAsInt());
        block.getChunk().setForceLoaded(true);
        JsonObject state = blockObject.get("state").getAsJsonObject();
        if (delete) {
            block.setType(Material.AIR);
        } else {
            block.setType(Material.valueOf(blockObject.get("material").getAsString()));
            block.setBlockData(Bukkit.createBlockData(state.get("data").getAsString()));
        }
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 0.5F);
    }

    private void writeJsonArray(JsonArray placedPortals, File file, String name) {
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file, name);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream is = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(placedPortals.toString());
            w.close();
        } catch (IOException e) {
            Bukkit.getLogger().info("Could not write to file");
            e.printStackTrace();
        }
    }

    private void writeJson(JsonObject placedPortals, File file, String name) {
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file, name);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream is = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(placedPortals.toString());
            w.close();
        } catch (IOException e) {
            Bukkit.getLogger().info("Could not write to file");
            e.printStackTrace();
        }
    }

    private List<Block> getBlocksAroundCenter(Location loc, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = (loc.getBlockX() - radius); x <= (loc.getBlockX() + radius); x++) {
            for (int y = (loc.getBlockY() - radius); y <= (loc.getBlockY() + radius); y++) {
                for (int z = (loc.getBlockZ() - radius); z <= (loc.getBlockZ() + radius); z++) {
                    Location l = new Location(loc.getWorld(), x, y, z);
                    if (l.distance(loc) <= radius) {
                        blocks.add(l.getBlock());
                    }
                }
            }
        }
        return blocks;
    }

    private JsonObject loadBlocks(String name, String site) {
        File file = new File(SkyPortals.getInstance().getDataFolder() + "/portals/" + name + "/schematics/" + site + ".json");
        return getJsonObject(file);
    }

    //TODO: Cache
    public void reloadPortals() {
        placedPortals.clear();
        File f = new File(SkyPortals.getInstance().getDataFolder() + "/data.json");
        JsonArray jsonArray = getJsonArray(f);
        jsonArray.forEach(jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject locationObject = jsonObject.get("location").getAsJsonObject();
            Location loc = new Location(Bukkit.getWorld(locationObject.get("world").getAsString()),
                    locationObject.get("x").getAsDouble(),
                    locationObject.get("y").getAsDouble(),
                    locationObject.get("z").getAsDouble());
            Portal portal = new Portal(jsonObject.get("name").getAsString(),
                    jsonObject.get("placedBy").getAsString(),
                    loc,
                    jsonObject.get("face").getAsString());
            placedPortals.add(portal);
        });
    }

    @Nullable
    private JsonArray getJsonArray(File f) {
        if (!f.exists()) {
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return new JsonParser().parse(sb.toString()).getAsJsonArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private JsonObject getJsonObject(File f) {
        if (!f.exists()) {
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return new JsonParser().parse(sb.toString()).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Portal> getPlacedPortals() {
        return placedPortals;
    }
}
