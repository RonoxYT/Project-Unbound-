package net.unbound;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Unbound implements ModInitializer {
    public static final String MOD_ID = "project-unbound";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This is where it all begins.
        LOGGER.info("Project Unbound: Initializing Kernel...");

        // TODO: Start the TaskScheduler here later
        LOGGER.info("Project Unbound: Kernel Ready.");
    }
}
