package net.unbound.mixin;

import net.minecraft.client.MinecraftClient;
import net.unbound.core.UnboundEngine;
import net.unbound.core.UnboundScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    protected abstract void render(boolean tick);

    @Shadow
    public abstract void tick();

    @Shadow
    private volatile boolean running;

    // The Gatekeeper
    private boolean unbound_allowTick = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onGameInit(CallbackInfo ci) {
        UnboundEngine.start();
    }

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void run(CallbackInfo ci) {
        UnboundScheduler scheduler = new UnboundScheduler();

        scheduler.startLoop(new UnboundScheduler.GameRunner() {
            @Override
            public void runOneTick() {
                if (!running) {
                    scheduler.stop();
                    return;
                }

                // I open the gate for the scheduler
                unbound_allowTick = true;
                tick(); // This tick is allowed
                unbound_allowTick = false; // I close the gate
            }

            @Override
            public void runOneRender(float tickDelta) {
                // IMPORTANT: render(true) so that Mojang calculates interpolation (the pig
                // slides).
                // But since the gate is closed (unbound_allowTick = false),
                // Mojang's internal tick() will be blocked by our mixin below.
                render(true);
            }
        });

        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        // If it's not the scheduler knocking at the door, we don't open.
        if (!unbound_allowTick) {
            ci.cancel();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onGameClose(CallbackInfo ci) {
        UnboundEngine.shutdown();
    }
}