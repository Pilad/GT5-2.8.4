package gregtech.api.util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.StatCollector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ColorResource {

    private static final Logger LOG = LogManager.getLogger(ColorResource.class);
    private static final Set<ColorResource> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

    private final String modId;
    private final String name;
    private final int defaultColor;
    private final boolean argb;
    private volatile int cachedColor;

    public ColorResource(String modId, String name, String hex, boolean argb) {
        this.modId = modId;
        this.name = name;
        this.argb = argb;
        this.defaultColor = parseHex(hex, argb);
        this.cachedColor = resolveColor();
        synchronized (INSTANCES) {
            INSTANCES.add(this);
        }
    }

    private static String stripPrefix(String hex) {
        String s = hex.trim();
        if (s.startsWith("0x") || s.startsWith("0X")) return s.substring(2);
        if (s.startsWith("#")) return s.substring(1);
        return s;
    }

    private static int parseHex(String hex, boolean argb) {
        long value = Long.parseLong(stripPrefix(hex), 16);
        return argb ? (int) value : (int) (0xFF000000L | value);
    }

    public String getLangKey() {
        return "color.resource." + modId + "." + name;
    }

    public int getColor() {
        return cachedColor;
    }

    private int resolveColor() {
        String langKey = getLangKey();
        String translated = StatCollector.translateToLocal(langKey);
        if (langKey != translated) {
            String value = stripPrefix(translated);
            try {
                if (!argb && value.length() > 6) {
                    LOG.warn(
                        "Lang key '{}' received ARGB hex '{}' but this color is RGB-only - alpha will be ignored.",
                        langKey,
                        value);
                }
                long parsed = Long.parseLong(value, 16);
                return argb ? (int) parsed : (int) (0xFF000000L | parsed);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid hex '{}' for lang key '{}', using default.", value, langKey);
                return defaultColor;
            }
        }
        return defaultColor;
    }

    public static class Factory {

        private final String modId;

        public Factory(String modId) {
            this.modId = modId;
        }

        public ColorResource argb(String name, String hex) {
            return new ColorResource(modId, name, hex, true);
        }

        public ColorResource rgb(String name, String hex) {
            return new ColorResource(modId, name, hex, false);
        }
    }

    public static class CacheReloadListener implements IResourceManagerReloadListener {

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            synchronized (INSTANCES) {
                for (ColorResource instance : INSTANCES) {
                    instance.cachedColor = instance.resolveColor();
                }
            }
        }
    }
}
