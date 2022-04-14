package net.candorservices.lisacek.skyportals.cons;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class PlacedBlock {

    private final Location location;

    private final Material material;

    public PlacedBlock(Location location, Material material) {
        this.location = location;
        this.material = material;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public JsonObject serializeToJson() {
        JsonObject block = new JsonObject();
        block.add("location", new JsonObject());
        JsonObject loc = block.get("location").getAsJsonObject();
        loc.addProperty("x", location.getX());
        loc.addProperty("y", location.getY());
        loc.addProperty("z", location.getZ());
        block.addProperty("material", material.name());
        return block;
    }

    public static PlacedBlock deserializeFromJson(JsonObject block) {
        JsonObject loc = block.get("location").getAsJsonObject();
        Location location = new Location(null, loc.get("x").getAsDouble(), loc.get("y").getAsDouble(), loc.get("z").getAsDouble());
        Material material = Material.valueOf(block.get("material").getAsString());
        return new PlacedBlock(location, material);
    }

    public static List<PlacedBlock> deserializeListFromJson(JsonArray list) {
        List<PlacedBlock> blocks = new ArrayList<>();
        list.forEach(block -> {
            JsonObject loc = block.getAsJsonObject().get("location").getAsJsonObject();
            Location location = new Location(null, loc.get("x").getAsDouble(), loc.get("y").getAsDouble(), loc.get("z").getAsDouble());
            Material material = Material.valueOf(block.getAsJsonObject().get("material").getAsString());
            blocks.add(new PlacedBlock(location, material));
        });
        return blocks;
    }

}
