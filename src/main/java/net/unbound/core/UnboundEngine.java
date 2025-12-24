package net.unbound.core;

import net.unbound.Unbound;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Reactor Core.
 * Manages Java 21 thread pools (Virtual & Platform).
 */
public class UnboundEngine {
    private static UnboundEngine INSTANCE;

    // The "infinite" pool for AI (Virtual Threads - Java 21 Feature)
    private final ExecutorService logicPool;

    // The "heavy" pool for physics (Platform Threads - 4 to 8 cores)
    private final ExecutorService simulationPool;

    private boolean isRunning = false;

    private UnboundEngine() {
        // 1. Initialization of the Logic Pool (Virtual Threads)
        // This is the magic of Java 21: we can have 100,000 threads like this without
        // lag.
        ThreadFactory virtualFactory = Thread.ofVirtual().name("Unbound-Logic-", 0).factory();
        this.logicPool = Executors.newThreadPerTaskExecutor(virtualFactory);

        // 2. Initialization of the Physics Pool (Work Stealing)
        // Uses real CPU cores for heavy calculations.
        this.simulationPool = Executors.newWorkStealingPool();
    }

    public static void start() {
        if (INSTANCE == null) {
            INSTANCE = new UnboundEngine();
            INSTANCE.isRunning = true;
            Unbound.LOGGER.info(">>> UNBOUND ENGINE STARTED <<<");
            Unbound.LOGGER.info("   > Logic Pool: VIRTUAL THREADS (Java 21) [ACTIVE]");
            Unbound.LOGGER.info("   > Sim Pool:   WORK STEALING (Multi-Core) [ACTIVE]");

            // TEST: Launch an immediate task to prove it works
            INSTANCE.runDiagnostics();
        }
    }

    public static UnboundEngine getInstance() {
        return INSTANCE;
    }

    /**
     * A test task to see if we are indeed off the Main Thread.
     */
    public void runDiagnostics() {
        logicPool.submit(() -> {
            String threadName = Thread.currentThread().toString();
            Unbound.LOGGER.info("DIAGNOSTIC: Running on thread: " + threadName);

            if (threadName.contains("VirtualThread")) {
                Unbound.LOGGER.info("SUCCESS: The engine is using Virtual Threads correctly!");
            } else {
                Unbound.LOGGER.error("FAILURE: I am on a normal thread...");
            }
        });
    }

    public ExecutorService getSimulationPool() {
        return this.simulationPool;
    }

    public static void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.isRunning = false;
            INSTANCE.logicPool.shutdown();
            INSTANCE.simulationPool.shutdown();
            Unbound.LOGGER.info("Unbound Engine: Systems Offline.");
        }
    }
}