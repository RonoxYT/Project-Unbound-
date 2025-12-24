package net.unbound.core;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.unbound.Unbound;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ParallelTicker {

    // Optimization: Batch Size
    // Instead of creating 1 task per entity (too much overhead), we group by
    // batches.
    // Ideally, we want ~number of cores tasks.

    public static void tickEntitiesParallel(World world, List<Entity> entities) {
        int size = entities.size();
        if (size == 0)
            return;

        // Sequential mode for small numbers
        if (size < 64) {
            for (Entity e : entities) {
                tickSingleEntity(e);
            }
            return;
        }

        // We divide into batches based on available threads
        // For 1000 entities on 12 threads, that's ~83 entities per thread.
        int cores = Runtime.getRuntime().availableProcessors();
        // We aim for a few more tasks than cores to balance (work stealing)
        int targetTasks = cores * 2;
        int batchSize = Math.max(32, size / targetTasks);

        // Splitting the list into sublists (Views)
        int numBatches = (size + batchSize - 1) / batchSize;
        CountDownLatch latch = new CountDownLatch(numBatches);

        for (int i = 0; i < size; i += batchSize) {
            final int start = i;
            final int end = Math.min(size, i + batchSize);

            // We submit a BIG work package
            UnboundEngine.getInstance().getSimulationPool().submit(() -> {
                try {
                    // We note the thread only once per batch
                    UnboundStats.trackThread(Thread.currentThread().getName());

                    // Fast local loop without overhead
                    for (int k = start; k < end; k++) {
                        tickSingleEntity(entities.get(k));
                    }
                } catch (java.util.ConcurrentModificationException cme) {
                    UnboundStats.threadConflicts++;
                } catch (Exception e) {
                    Unbound.LOGGER.error("Crash in Parallel Batch", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // 1s Timeout (generous)
            if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                Unbound.LOGGER.warn("Time Dilation detected! (Batch Timeout)");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void tickSingleEntity(Entity entity) {
        if (!entity.isRemoved()) {
            try {
                entity.tick();
            } catch (Exception e) {
                // Individual protection in case an entity crashes
                Unbound.LOGGER.error("Entity tick failed: " + entity.getName().getString(), e);
            }
        }
    }
}
