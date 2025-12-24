package net.unbound.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.EntityList;
import net.unbound.core.ParallelTicker;
import net.unbound.core.UnboundStats;
import net.unbound.logic.BrainController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    /**
     * @author Project Unbound
     * @reason Replacing the sequential loop with parallelism.
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"))
    private void redirectEntityTicking(EntityList instance, Consumer<Entity> action) {
        // 0. Update player cache for BrainController (Thread-Safe)
        BrainController.updatePlayerCache((ServerWorld) (Object) this);

        // 1. We extract all active entities into a temporary list
        // (Necessary because Mojang's iterator is not Thread-Safe)
        List<Entity> tasks = new ArrayList<>();
        instance.forEach(entity -> {
            tasks.add((Entity) entity);
            // Little bonus: we update stats here too just to be sure
            UnboundStats.activeEntities++;
        });

        // 2. We send the list to the parallel processor
        ParallelTicker.tickEntitiesParallel((ServerWorld) (Object) this, tasks);
    }
}
