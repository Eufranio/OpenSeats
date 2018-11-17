package io.github.eufranio.openseats.config;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * Created by Frani on 17/11/2018.
 */
@ConfigSerializable
public class MainConfig {

    @Setting
    private List<BlockType> seatBlocks = Lists.newArrayList(BlockTypes.OAK_STAIRS);

    public boolean canSit(Location<World> loc) {
        return this.seatBlocks.contains(loc.getBlockType());
    }

    public boolean canSit(BlockType type) {
        return this.seatBlocks.contains(type);
    }

}
