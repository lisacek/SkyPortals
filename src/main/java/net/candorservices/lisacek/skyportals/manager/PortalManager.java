package net.candorservices.lisacek.skyportals.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.candorservices.lisacek.skyportals.SkyPortals;
import net.candorservices.lisacek.skyportals.cons.PlacedBlock;
import net.candorservices.lisacek.skyportals.cons.Portal;
import net.candorservices.lisacek.skyportals.utils.Colors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PortalManager {

    private final SkyPortals plugin;

    private final List<Portal> placedPortals = new ArrayList<>();

    public PortalManager(SkyPortals plugin) {
        this.plugin = plugin;
    }

    public void savePortal(Location loc, File file, String name) {
        List<Block> blocks = getBlocksAroundCenter(loc, 20);
        JsonObject blocksObject = new JsonObject();
        blocksObject.add("blocks", new JsonArray());

        for (Block block : blocks) {
            if (block.getType() == Material.AIR || block.getType() == Material.VOID_AIR) continue;
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

    public void placePortal(Location loc, Player player, String name, String site, boolean delete) {
        try {
            JsonObject blocksObject = loadBlocks(name, site);

            Portal portal = new Portal(name, player.getName(), loc, site);

            int adjust = SkyPortals.getInstance().getPortals().getInt("portals." + name + ".adjust");

            Location hologramLocation = switch (site.toLowerCase()) {
                case "west" -> portal.getLocation().clone().add(adjust, 0, 0);
                case "east" -> portal.getLocation().clone().add((adjust - (adjust * 2)), 0, 0);
                case "north" -> portal.getLocation().clone().add(0, 0, (adjust - (adjust * 2)));
                default -> portal.getLocation().clone().add(0, 0, adjust);
            };

            SkyPortals.getInstance().getLocationCache().add(loc);

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

            int highestY = 0;
            for (PlacedBlock placedBlock : portal.getPlacedBlocks()) {
                if (placedBlock.getLocation().getBlockY() < highestY) {
                    highestY = placedBlock.getLocation().getBlockY();
                }
            }

            AtomicBoolean isValid = new AtomicBoolean(true);

            validateLoc(loc, portal, parts, highestY, isValid);
            validateLoc(loc, portal, portalParts, highestY, isValid);

            if (!delete && isValid.get()) {
                ItemStack st = player.getInventory().getItemInMainHand().clone();
                st.setAmount(st.getAmount() - 1);
                player.getPlayer().getInventory().setItemInMainHand(st);
            }
            if (!delete && !isValid.get()) {
                SkyPortals.getInstance().getLocationCache().remove(loc);
                player.sendMessage(Colors.translateColors(SkyPortals.getInstance().getMessages().getString("portal-cannot-place")));
                return;
            }

            File f2 = SkyPortals.getInstance().getDataFolder();
            if (!delete) {
                loc.add(0, Math.abs(highestY) + SkyPortals.getInstance().getPortals().getInt("portals." + portal.getName() + ".adjust"), 0);
                placedPortals.add(portal);
            } else {
                List<Entity> nearby = player.getNearbyEntities(5, 5, 5);
                for (Entity entity : nearby) {
                    if (entity instanceof ArmorStand) {
                        entity.remove();
                    }
                }
                placedPortals.removeIf(portal12 -> portal12 != null && portal12.getLocation().distance(loc) < 3);
            }

            int max = parts.size();
            if (!delete) {
                Bukkit.getScheduler().runTaskTimer(SkyPortals.getInstance(), (task) -> {
                    if (i.get() + 1 >= max) {
                        portalParts.forEach(blockObject -> {
                            updateBlock(loc, blockObject, false);
                        });
                        hologramLocation.getWorld().spawn(hologramLocation, ArmorStand.class, armorStand -> {
                            armorStand.setCustomName(Colors.translateColors(SkyPortals.getInstance().getPortals().getString("portals." + name + ".hologram.line1")));
                            armorStand.setCustomNameVisible(true);
                            armorStand.setGravity(false);
                            armorStand.setVisible(false);
                            armorStand.setInvulnerable(true);
                        });
                        hologramLocation.getWorld().spawn(hologramLocation.add(0, -0.3, 0), ArmorStand.class, armorStand -> {
                            armorStand.setCustomName(Colors.translateColors(SkyPortals.getInstance().getPortals().getString("portals." + name + ".hologram.line2")));
                            armorStand.setCustomNameVisible(true);
                            armorStand.setGravity(false);
                            armorStand.setVisible(false);
                            armorStand.setInvulnerable(true);
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
            SkyPortals.getInstance().getLocationCache().remove(loc);
            writeJsonArray(portalsArray, f2, "data.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateLoc(Location loc, Portal portal, List<JsonObject> portalParts, int highestY, AtomicBoolean isValid) {
        int finalHighestY = highestY;
        portalParts.forEach(json -> {
            Location virtual = loc.clone().add(0, Math.abs(finalHighestY) + SkyPortals.getInstance().getPortals().getInt("portals." + portal.getName() + ".adjust"), 0);
            JsonObject blockObject = json.getAsJsonObject();
            JsonObject locationObject = blockObject.get("location").getAsJsonObject();
            Block block = virtual.getBlock().getRelative(locationObject.get("x").getAsInt(), locationObject.get("y").getAsInt(), locationObject.get("z").getAsInt());
            block.getChunk().setForceLoaded(true);
            if (block.getType() != Material.AIR && block.getType() != Material.valueOf(blockObject.get("material").getAsString())) {
                isValid.set(false);
            }
        });
    }

    public Portal getNearestPortal(Location loc, double distance) {
        Portal nearest = null;
        for (Portal portal : placedPortals) {
            if (portal.getLocation().getWorld() != loc.getWorld()) continue;
            int highestY = 0;
            for (PlacedBlock placedBlock : portal.getPlacedBlocks()) {
                if (placedBlock.getLocation().getBlockY() < highestY) {
                    highestY = placedBlock.getLocation().getBlockY();
                }
            }
            if (portal.getLocation().distance(loc) < distance) {
                nearest = portal;
                break;
            }
        }
        return nearest;
    }

    private void deleteBlock(Location loc, JsonObject blockObject) {
        JsonObject locationObject = blockObject.get("location").getAsJsonObject();
        Block block = loc.getBlock().getRelative(locationObject.get("x").getAsInt(), locationObject.get("y").getAsInt(), locationObject.get("z").getAsInt());
        block.getChunk().setForceLoaded(true);
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


    private void writeJson(String empty, File file, String name) {
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
            w.write(empty);
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
        if (!f.exists()) {
            try {
                f.createNewFile();
                writeJson("[]", f, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
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
