package net.unbound.logic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.unbound.Unbound;

public class BrainController {

    // Squared distances (to avoid costly square roots)
    // 32 blocks = 1024
    // 64 blocks = 4096
    private static final double DISTANCE_ACTIVE = 1024.0;
    private static final double DISTANCE_DORMANT = 4096.0;

    // Thread-safe cache of player positions
    // We just store Vec3ds, it is lightweight and sufficient.
    private static java.util.List<net.minecraft.util.math.Vec3d> playerPositions = new java.util.ArrayList<>();

    // Called by the Main Thread before parallel dispatch
    public static void updatePlayerCache(net.minecraft.world.World world) {
        playerPositions.clear();
        for (PlayerEntity player : world.getPlayers()) {
            playerPositions.add(player.getPos());
        }
    }

    /**
     * Decides if an entity should activate its AI at this precise tick.
     * Thread-Safe Version: Uses the cache, does not access the World.
     */
    public static int getBrainStatus(Entity entity) {
        if (playerPositions.isEmpty()) {
            return 2; // No known players? Sleep.
        }

        double closestDistSq = Double.MAX_VALUE;
        net.minecraft.util.math.Vec3d entityPos = entity.getPos();

        for (net.minecraft.util.math.Vec3d playerPos : playerPositions) {
            double ds = entityPos.squaredDistanceTo(playerPos);
            if (ds < closestDistSq) {
                closestDistSq = ds;
            }
        }

        if (closestDistSq < DISTANCE_ACTIVE) {
            return 0; // Close: Full power
        } else if (closestDistSq < DISTANCE_DORMANT) {
            return 1; // Medium: Eco mode
        } else {
            return 2; // Far: Sleep
        }
    }
}
