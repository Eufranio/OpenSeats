package io.github.eufranio.openseats.config;

import com.google.common.collect.Maps;
import io.github.eufranio.openseats.OpenSeats;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Frani on 17/11/2018.
 */
@ConfigSerializable
public class SitConfig {

    @Setting // player <-> entity
    private Map<UUID, SitData> sitData = Maps.newHashMap();

    @ConfigSerializable
    public static class SitData {

        @Setting
        public Location<World> currLocation;

        @Setting
        public Location<World> previousLocation;

        @Setting
        public UUID arrow;

        public Entity getEntity() {
            return currLocation.getExtent()
                    .getEntity(arrow)
                    .orElse(null);
        }

    }

    public boolean isSitting(UUID player) {
        return this.sitData.containsKey(player);
    }

    public void add(UUID player, SitData data) {
        this.sitData.put(player, data);
        OpenSeats.getInstance().data.save();
    }

    public SitData remove(UUID player) {
        SitData data = this.sitData.remove(player);
        OpenSeats.getInstance().data.save();
        return data;
    }

    public Map.Entry<UUID, SitData> getSitting(Location<World> loc) {
        return this.sitData.entrySet()
                .stream()
                .filter(d -> d.getValue().currLocation.getBlockPosition().equals(loc.getBlockPosition()) &&
                             d.getValue().currLocation.getExtent().equals(loc.getExtent()))
                .findFirst()
                .orElse(null);
    }

}
