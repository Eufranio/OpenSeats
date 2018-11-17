package io.github.eufranio.openseats;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.eufranio.openseats.config.SitConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.EnumTraits;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.SlabData;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.data.type.PickupRules;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.type.StairShapes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Frani on 17/11/2018.
 */
public class SeatListeners {

    @Listener
    public void onInteract(InteractBlockEvent.Secondary.MainHand event, @Root Player player) {
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Location<World> target = event.getTargetBlock().getLocation().orElse(null);
        if (target == null) return;
        if (!OpenSeats.getInstance().getConfig().canSit(target)) return;
        if (OpenSeats.getInstance().getData().isSitting(player.getUniqueId())) return;
        if (!player.hasPermission("openseats.sit")) return;

        if (target.add(0, 1, 0).getBlockType() != BlockTypes.AIR) return;

        target = new Location<World>(target.getExtent(), target.getBlockPosition()).add(0.5, 0, 0.5);
        if (target.getBlock().getProperty(SolidCubeProperty.class).isPresent() && !target.getBlockType().getId().contains("stairs")) {
            target = target.add(0, 0.5, 0);
        }

        if (target.getBlockType().getId().contains("slab")) {
            if (!target.getBlock().getId().contains("half=top")) {
                target = target.sub(0, 0.5, 0);
            }
        }

        Direction dir = target.getBlock().get(Keys.DIRECTION).orElse(null);
        if (dir != null && target.getBlockType().getId().contains("stairs")) {
            if (target.getBlock().get(Keys.STAIR_SHAPE).get() != StairShapes.STRAIGHT) return;

            dir = dir.getOpposite();
            switch (dir) {
                case NORTH:
                    target = target.sub(0, 0, 0.2);
                    break;
                case EAST:
                    target = target.add(0.2, 0, 0);
                    break;
                case SOUTH:
                    target = target.add(0, 0, 0.2);
                    break;
                case WEST:
                    target = target.sub(0.2, 0, 0);
                    break;
            }
        }

        Entity arrow = target.createEntity(EntityTypes.TIPPED_ARROW);
        arrow.offer(Keys.PERSISTS, true);
        arrow.offer(Keys.PICKUP_RULE, PickupRules.DISALLOWED);
        arrow.offer(Keys.HAS_GRAVITY, false);
        target.getExtent().spawnEntity(arrow);
        arrow.addPassenger(player);
        Task.builder()
                .delayTicks(1)
                .execute(() -> arrow.offer(Keys.INVISIBLE, true))
                .submit(OpenSeats.getInstance());

        SitConfig.SitData data = new SitConfig.SitData();
        data.currLocation = target;
        data.previousLocation = player.getLocation();
        data.arrow = arrow.getUniqueId();
        OpenSeats.getInstance().getData().add(player.getUniqueId(), data);

        //player.setLocation(target.add(0.5, 0.2, 0.5));
    }

    @Listener
    public void onUnmount(RideEntityEvent.Dismount event, @First Player player) {
        if (OpenSeats.getInstance().getData().isSitting(player.getUniqueId())) {
            SitConfig.SitData data = OpenSeats.getInstance().getData().remove(player.getUniqueId());
            event.getTargetEntity().clearPassengers();
            event.getTargetEntity().remove();
            player.setLocation(data.previousLocation);
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        if (OpenSeats.getInstance().getData().isSitting(event.getTargetEntity().getUniqueId())) {
            SitConfig.SitData data = OpenSeats.getInstance().getData().remove(event.getTargetEntity().getUniqueId());
            data.getEntity().clearPassengers();
            data.getEntity().remove();
            event.getTargetEntity().setLocation(data.previousLocation);
        }
    }

    @Listener
    public void onBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().forEach(transaction -> {
            transaction.getOriginal().getLocation().ifPresent(loc -> {
                if (OpenSeats.getInstance().getConfig().canSit(transaction.getOriginal().getState().getType())) {
                    Map.Entry<UUID, SitConfig.SitData> data = OpenSeats.getInstance().getData().getSitting(loc);
                    if (data == null) return;
                    data.getValue().getEntity().clearPassengers();
                    data.getValue().getEntity().remove();
                    OpenSeats.getInstance().getData().remove(data.getKey());
                    Sponge.getServer().getPlayer(data.getKey()).ifPresent(p -> p.setLocation(data.getValue().previousLocation));
                }
            });
        });
    }

    @Listener
    public void onDestructEntity(DestructEntityEvent event) {
        //
    }

}
