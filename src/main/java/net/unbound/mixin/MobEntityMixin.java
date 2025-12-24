package net.unbound.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import net.unbound.logic.BrainController;
import net.unbound.core.UnboundStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private int unbound_ticksSinceLastThink = 0;

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void onMobTick(CallbackInfo ci) {
        // 1. Ask the controller what to do
        int status = BrainController.getBrainStatus(this);

        if (status == 2) {
            // RED ZONE: Total Lobotomy
            UnboundStats.dormantEntities++;
            ci.cancel();
        } else if (status == 1) {
            // ORANGE ZONE: Limited AI (1 time per second)
            UnboundStats.limitedEntities++;
            unbound_ticksSinceLastThink++;
            if (unbound_ticksSinceLastThink < 20) {
                // It is not time to think yet. We cancel.
                ci.cancel();
            } else {
                // It's time! We let the AI pass and reset the counter.
                unbound_ticksSinceLastThink = 0;
            }
        } else {
            // GREEN ZONE (Status 0)
            UnboundStats.activeEntities++;
        }
    }
}
