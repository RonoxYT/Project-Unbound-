package net.unbound.core;

import net.unbound.Unbound;
import java.util.concurrent.locks.LockSupport;

public class UnboundScheduler {

    private static final long NANOS_PER_TICK = 50_000_000L; // 50ms
    // SECURITY: Never simulate more than 10 ticks at once (avoids the "Spiral of
    // Death")
    private static final int MAX_TICKS_PER_UPDATE = 10;

    // EXPOSED FOR RENDER MIXIN (Interpolation)
    public static volatile float currentTickDelta = 0.0f;

    private boolean running = true;

    public interface GameRunner {
        void runOneTick();

        void runOneRender(float tickDelta);
    }

    public void startLoop(GameRunner game) {
        Unbound.LOGGER.info(">>> UNBOUND SCHEDULER: STRICT SYNC MODE <<<");

        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();

        // ACCUMULATOR
        double lag = 0.0;

        int frames = 0;
        int ticks = 0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime = now;

            // Absolute security cap:
            // If a frame took more than 1000ms (big lag), we say it only took 1000ms.
            // This prevents the game from trying to catch up whole seconds.
            if (elapsed > 1_000_000_000L) {
                elapsed = 1_000_000_000L;
            }

            lag += elapsed;

            // LOGIC LOOP (TICK)
            // We consume the accumulated time but with SECURITY.
            // We never do more than 10 ticks per frame to avoid freezing.
            int loops = 0;
            while (lag >= NANOS_PER_TICK && loops < MAX_TICKS_PER_UPDATE) {
                UnboundStats.startTick();
                game.runOneTick();
                UnboundStats.endTick();

                ticks++;
                loops++;
                lag -= NANOS_PER_TICK;
            }

            // RENDERING (FRAME)
            // We calculate interpolation for visual smoothness
            float tickDelta = (float) (lag / NANOS_PER_TICK);
            currentTickDelta = tickDelta; // Global update for the Mixin

            game.runOneRender(tickDelta);
            frames++;

            // ANTI-OVERHEAT (CPU Saver)
            // If we rendered the frame very quickly, we let the CPU breathe for 1ms
            // This stabilizes the system clock and avoids micro-drifts.
            LockSupport.parkNanos(1_000_000L);

            // Debug Console
            if (System.currentTimeMillis() - timer > 1000) {
                // Unbound.LOGGER.info(String.format("FPS: %d | TPS: %d", frames, ticks));
                frames = 0;
                ticks = 0;
                timer += 1000;
            }
        }
    }

    public void stop() {
        this.running = false;
    }
}
