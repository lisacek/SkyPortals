package net.candorservices.lisacek.skyportals.cons;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Portal {

    private final String name;
    private final String placedBy;
    private final Location location;
    private final String face;

    private final List<PlacedBlock> placedBlocks = new ArrayList<>();

    public Portal(String name, String placedBy, Location location, String face) {
        this.name = name;
        this.placedBy = placedBy;
        this.location = location;
        this.face = face;
    }

    public String getName() {
        return name;
    }

    public String getPlacedBy() {
        return placedBy;
    }

    public Location getLocation() {
        return location;
    }

    public String getFace() {
        return face;
    }

    public List<PlacedBlock> getPlacedBlocks() {
        return placedBlocks;
    }

    public JsonObject serializeToJson() {
        JsonObject portal = new JsonObject();
        portal.addProperty("name", name);
        portal.addProperty("placedBy", placedBy);
        portal.add("location", new JsonObject());

        JsonObject location = portal.get("location").getAsJsonObject();
        location.addProperty("world", this.location.getWorld().getName());
        location.addProperty("x", this.location.getX());
        location.addProperty("y", this.location.getY());
        location.addProperty("z", this.location.getZ());

        portal.addProperty("face", face);

        portal.add("placedBlocks", new JsonArray());
        JsonArray placedBlocks = portal.get("placedBlocks").getAsJsonArray();
        for (PlacedBlock placedBlock : this.placedBlocks) {
            placedBlocks.add(placedBlock.serializeToJson());
        }
        return portal;
    }

    public static Portal deserializePortalFromJson(JsonObject p) {
        Portal portal = new Portal(p.get("name").getAsString(), p.get("placedBy").getAsString(),
                new Location(Bukkit.getWorld(p.get("location").getAsJsonObject().get("world").getAsString()),
                        p.get("location").getAsJsonObject().get("x").getAsDouble(),
                        p.get("location").getAsJsonObject().get("y").getAsDouble(),
                        p.get("location").getAsJsonObject().get("z").getAsDouble()),
                p.get("face").getAsString());
        portal.getPlacedBlocks().addAll(PlacedBlock.deserializeListFromJson(p.get("placedBlocks").getAsJsonArray()));
        return portal;
    }
}


