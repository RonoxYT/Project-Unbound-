package net.unbound.mixin;

import net.minecraft.client.render.RenderTickCounter;
import net.unbound.core.UnboundScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// We target the specific "Dynamic" implementation that manages game time
@Mixin(RenderTickCounter.Dynamic.class)
public class RenderTickCounterMixin {

    /**
     * @author Maxence
     * @reason PROJECT UNBOUND - Interpolation Control
     *         We intercept the interpolation calculation to force usage of the
     *         Scheduler's delta.
     *         This fixes movement stuttering.
     */
    @Inject(method = "getTickDelta", at = @At("HEAD"), cancellable = true)
    public void getTickDelta(CallbackInfoReturnable<Float> cir) {
        // We force the return of our Delta and cancel the original calculation
        cir.setReturnValue(UnboundScheduler.currentTickDelta);
    }
}
