package net.unbound.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.unbound.core.UnboundStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    // Injection at the end of the interface rendering (HUD)
    @Inject(method = "render", at = @At("TAIL"))
    public void renderUnboundHUD(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // We only display if F3 is not open (to avoid clutter)
        if (!client.getDebugHud().shouldShowDebugHud()) {

            // Color: Cyberpunk Cyan (0x00FFFF)
            int color = 0x00FFFF;
            int y = 10;

            // Line 1: Title
            context.drawText(client.textRenderer, "[ PROJECT UNBOUND V1 ]", 10, y, color, true);
            y += 10;

            // Line 2: FPS
            context.drawText(client.textRenderer, "FPS: " + client.getCurrentFps(), 10, y, 0xFFFFFF, true);
            y += 10;

            // Line 3: DAB Stats (This is what we want to see!)
            String entityStats = String.format("ENTITIES: Active[%d] | Lim[%d] | SLEEP[%d] | Threads[%d]",
                    UnboundStats.displayActive,
                    UnboundStats.displayLimited,
                    UnboundStats.displayDormant,
                    UnboundStats.displayThreads);

            // If many are sleeping, we display in green (Success), otherwise white
            int statColor = UnboundStats.displayDormant > 0 ? 0x00FF00 : 0xFFFFFF;
            context.drawText(client.textRenderer, entityStats, 10, y, statColor, true);
        }
    }
}
