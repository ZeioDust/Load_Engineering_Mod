package com.loadbearing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * This loader ships no config system, so the same settings live in a JSON file under the loader's
 * config directory. Every key, default, range and comment matches the other targets, and the
 * accessors keep the get() shape the rest of the mod is written against.
 */
public final class Config {
    private static final Map<String, Entry<?>> ENTRIES = new LinkedHashMap<>();

    public enum DifficultyMode {
        VISUAL_ONLY,

        NORMAL,

        STRICT
    }

    public static final BooleanValue SYSTEM_ENABLED = bool("systemEnabled", true,
            "Master switch. When false the structural solver never runs and nothing collapses.");

    public static final EnumValue<DifficultyMode> DIFFICULTY_MODE = enumValue("difficultyMode",
            DifficultyMode.NORMAL, DifficultyMode.class,
            "VISUAL_ONLY: analyse and display only, nothing ever falls. "
                    + "NORMAL: unsupported and overloaded blocks crack and then collapse. "
                    + "STRICT: as NORMAL, but the rules also apply to creative mode players.");

    public static final BooleanValue APPLY_IN_CREATIVE = bool("applyInCreative", false,
            "Apply structural rules to blocks placed by creative mode players.");

    public static final IntValue SOLVER_RADIUS = intValue("solverRadius", 48, 4, 256,
            "Radius in blocks of the cluster evaluated around a world edit.");

    public static final IntValue SOLVER_MAX_NODES = intValue("solverMaxNodes", 20000, 256, 500000,
            "Hard cap on the number of blocks visited in a single cluster evaluation.");

    public static final IntValue SOLVER_OPS_PER_TICK = intValue("solverOpsPerTick", 4000, 100, 200000,
            "Server tick budget for the async solver queue, measured in graph operations.");

    public static final IntValue COLLAPSE_DELAY_TICKS = intValue("collapseDelayTicks", 20, 0, 1200,
            "Ticks a block spends cracked and groaning before it actually falls.");

    public static final IntValue MAX_BLOCKS_PER_COLLAPSE = intValue("maxBlocksPerCollapse", 2000, 1,
            200000, "Upper bound on how many blocks a single chain reaction may bring down.");

    public static final BooleanValue DEBRIS_DAMAGE_ENABLED = bool("debrisDamageEnabled", true,
            "Whether falling debris injures whatever it lands on.");

    public static final DoubleValue DEBRIS_DAMAGE_MULTIPLIER = doubleValue("debrisDamageMultiplier",
            1.0D, 0.0D, 100.0D,
            "Scales debris impact damage. Damage is fall distance times block weight times this.");

    public static final BooleanValue TUNNEL_COLLAPSE = bool("tunnelCollapse", true,
            "Whether cutting into rock makes the rock around the hole answer for itself. "
                    + "With this on, a corridor one or two blocks wide stands on its own and anything "
                    + "wider needs a pillar left in it or grout in the roof. With it off, excavations "
                    + "behave as they always did: the rock around a tunnel anchors itself and never "
                    + "falls. Rock nobody has cut into is never affected either way, so an existing "
                    + "world is only ever destabilised where a pick has actually gone in.");

    public static final IntValue CAVERN_SPAN = intValue("cavernSpan", 6, 1, 64,
            "How far a natural ceiling has to reach before breaking it starts a cave-in. "
                    + "Measured as the number of blocks you would have to walk across the ceiling to "
                    + "reach the nearest wall or pillar. Six means a hall wider than about thirteen "
                    + "blocks is fragile, and anything narrower is as solid as it ever was. Raise it "
                    + "to make only truly enormous caverns dangerous, lower it to make most caves "
                    + "risky.");

    public static final BooleanValue CRACK_WARNING_ENABLED = bool("crackWarningEnabled", true,
            "Play the crack warning and show cracked overlays before a collapse.");

    public static final DoubleValue WEIGHT_MULTIPLIER = doubleValue("weightMultiplier", 1.0D, 0.01D,
            100.0D, "Global multiplier applied to every material weight.");

    public static final DoubleValue STRENGTH_MULTIPLIER = doubleValue("strengthMultiplier", 1.0D,
            0.01D, 100.0D, "Global multiplier applied to every compressive strength.");

    public static final DoubleValue SPAN_MULTIPLIER = doubleValue("spanMultiplier", 1.0D, 0.01D,
            100.0D, "Global multiplier applied to every maximum span.");

    public static final IntValue ANCHOR_DEPTH = intValue("anchorDepth", 6, 0, 128,
            "Depth below the surface at which any block counts as an anchor.");

    private Config() {}

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("loadbearing.json");
        JsonObject json = new JsonObject();
        if (Files.isRegularFile(path)) {
            try {
                json = JsonParser.parseString(
                        Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                LoadBearing.LOGGER.error("Could not read {}, using defaults", path, e);
                json = new JsonObject();
            }
        }
        for (Entry<?> entry : ENTRIES.values()) {
            entry.read(json);
        }

        JsonObject out = new JsonObject();
        for (Entry<?> entry : ENTRIES.values()) {
            out.addProperty("_comment_" + entry.key, entry.comment);
            entry.write(out);
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path,
                    new GsonBuilder().setPrettyPrinting().create().toJson(out) + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoadBearing.LOGGER.error("Could not write {}", path, e);
        }
    }

    public static boolean solverActive() {
        return SYSTEM_ENABLED.get();
    }

    public static boolean collapseActive() {
        return SYSTEM_ENABLED.get() && DIFFICULTY_MODE.get() != DifficultyMode.VISUAL_ONLY;
    }

    public static boolean strictMode() {
        return DIFFICULTY_MODE.get() == DifficultyMode.STRICT;
    }

    public static boolean appliesToCreative() {
        return APPLY_IN_CREATIVE.get() || strictMode();
    }

    private abstract static class Entry<T> {
        final String key;

        final String comment;

        T value;

        Entry(String key, T initial, String comment) {
            this.key = key;
            this.value = initial;
            this.comment = comment;
            ENTRIES.put(key, this);
        }

        public T get() {
            return this.value;
        }

        abstract void read(JsonObject json);

        abstract void write(JsonObject json);
    }

    public static final class BooleanValue extends Entry<Boolean> {
        BooleanValue(String key, boolean initial, String comment) {
            super(key, initial, comment);
        }

        @Override
        void read(JsonObject json) {
            if (json.has(this.key)) {
                this.value = json.get(this.key).getAsBoolean();
            }
        }

        @Override
        void write(JsonObject json) {
            json.addProperty(this.key, this.value);
        }
    }

    public static final class IntValue extends Entry<Integer> {
        private final int min;

        private final int max;

        IntValue(String key, int initial, int min, int max, String comment) {
            super(key, initial, comment);
            this.min = min;
            this.max = max;
        }

        @Override
        void read(JsonObject json) {
            if (json.has(this.key)) {
                this.value = Math.max(this.min, Math.min(this.max, json.get(this.key).getAsInt()));
            }
        }

        @Override
        void write(JsonObject json) {
            json.addProperty(this.key, this.value);
        }
    }

    public static final class DoubleValue extends Entry<Double> {
        private final double min;

        private final double max;

        DoubleValue(String key, double initial, double min, double max, String comment) {
            super(key, initial, comment);
            this.min = min;
            this.max = max;
        }

        @Override
        void read(JsonObject json) {
            if (json.has(this.key)) {
                this.value = Math.max(this.min,
                        Math.min(this.max, json.get(this.key).getAsDouble()));
            }
        }

        @Override
        void write(JsonObject json) {
            json.addProperty(this.key, this.value);
        }
    }

    public static final class EnumValue<E extends Enum<E>> extends Entry<E> {
        private final Class<E> type;

        EnumValue(String key, E initial, Class<E> type, String comment) {
            super(key, initial, comment);
            this.type = type;
        }

        @Override
        void read(JsonObject json) {
            if (!json.has(this.key)) {
                return;
            }
            try {
                this.value = Enum.valueOf(this.type, json.get(this.key).getAsString());
            } catch (IllegalArgumentException e) {
                LoadBearing.LOGGER.error("Unknown value for {}, keeping {}", this.key, this.value);
            }
        }

        @Override
        void write(JsonObject json) {
            json.addProperty(this.key, this.value.name());
        }
    }

    private static BooleanValue bool(String key, boolean initial, String comment) {
        return new BooleanValue(key, initial, comment);
    }

    private static IntValue intValue(String key, int initial, int min, int max, String comment) {
        return new IntValue(key, initial, min, max, comment);
    }

    private static DoubleValue doubleValue(String key, double initial, double min, double max,
            String comment) {
        return new DoubleValue(key, initial, min, max, comment);
    }

    private static <E extends Enum<E>> EnumValue<E> enumValue(String key, E initial, Class<E> type,
            String comment) {
        return new EnumValue<>(key, initial, type, comment);
    }
}
