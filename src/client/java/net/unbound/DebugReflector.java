package net.unbound;

import net.minecraft.client.render.RenderTickCounter;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class DebugReflector {
    public static void dump(Class<?> clazz) {
        Unbound.LOGGER.info(">>> REFLECTION DUMP FOR: " + clazz.getName() + " <<<");
        Unbound.LOGGER.info(" IS RECORD: " + clazz.isRecord());
        for (Field field : clazz.getDeclaredFields()) {
            Unbound.LOGGER.info(String.format(" FIELD: %s | TYPE: %s", field.getName(), field.getType().getName()));
        }
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            Unbound.LOGGER.info(" CONSTRUCTOR: " + c.toString());
        }
        for (Method m : clazz.getDeclaredMethods()) {
            Unbound.LOGGER.info(String.format(" METHOD: %s | RET: %s", m.getName(), m.getReturnType().getName()));
        }
    }

    public static void run() {
        dump(RenderTickCounter.Dynamic.class);
    }
}
