package net.unbound.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnboundStats {
    // Global counters (reset every frame)
    public static int activeEntities = 0;
    public static int dormantEntities = 0;
    public static int limitedEntities = 0;

    // Thread conflict counter (CME)
    public static int threadConflicts = 0;

    // Active threads tracking (to prove parallelism)
    private static final Set<String> activeThreadsSet = Collections.synchronizedSet(new HashSet<>());

    // Display variables (Latched) - read by the HUD
    public static int displayActive = 0;
    public static int displayDormant = 0;
    public static int displayLimited = 0;
    public static int displayConflicts = 0;
    public static int displayThreads = 0; // Number of unique cores/threads used

    public static void trackThread(String threadName) {
        activeThreadsSet.add(threadName);
    }

    public static void startTick() {
        activeEntities = 0;
        dormantEntities = 0;
        limitedEntities = 0;
        threadConflicts = 0;
        activeThreadsSet.clear();
    }

    public static void endTick() {
        displayActive = activeEntities;
        displayDormant = dormantEntities;
        displayLimited = limitedEntities;
        displayConflicts = threadConflicts;
        displayThreads = activeThreadsSet.size();
    }
}
