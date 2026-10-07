package com.loadbearing;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();

    public enum DifficultyMode {
        VISUAL_ONLY,

        NORMAL,

        STRICT
    }

    public static final ForgeConfigSpec.BooleanValue SYSTEM_ENABLED = B
            .comment("Master switch. When false the structural solver never runs and nothing collapses.")
            .define("systemEnabled", true);

    public static final ForgeConfigSpec.EnumValue<DifficultyMode> DIFFICULTY_MODE = B
            .comment("VISUAL_ONLY: analyse and display only, nothing ever falls.",
                    "NORMAL: unsupported and overloaded blocks crack and then collapse.",
                    "STRICT: as NORMAL, but the rules also apply to creative mode players.")
            .defineEnum("difficultyMode", DifficultyMode.NORMAL);

    public static final ForgeConfigSpec.BooleanValue APPLY_IN_CREATIVE = B
            .comment("Apply structural rules to blocks placed by creative mode players.")
            .define("applyInCreative", false);

    public static final ForgeConfigSpec.IntValue SOLVER_RADIUS = B
            .comment("Radius in blocks of the cluster evaluated around a world edit.")
            .defineInRange("solverRadius", 48, 4, 256);

    public static final ForgeConfigSpec.IntValue SOLVER_MAX_NODES = B
            .comment("Hard cap on the number of blocks visited in a single cluster evaluation.")
            .defineInRange("solverMaxNodes", 20000, 256, 500000);

    public static final ForgeConfigSpec.IntValue SOLVER_OPS_PER_TICK = B
            .comment("Server tick budget for the async solver queue, measured in graph operations.")
            .defineInRange("solverOpsPerTick", 4000, 100, 200000);

    public static final ForgeConfigSpec.IntValue COLLAPSE_DELAY_TICKS = B
            .comment("Ticks a block spends cracked and groaning before it actually falls.")
            .defineInRange("collapseDelayTicks", 20, 0, 1200);

    public static final ForgeConfigSpec.IntValue MAX_BLOCKS_PER_COLLAPSE = B
            .comment("Upper bound on how many blocks a single chain reaction may bring down.")
            .defineInRange("maxBlocksPerCollapse", 2000, 1, 200000);

    public static final ForgeConfigSpec.BooleanValue DEBRIS_DAMAGE_ENABLED = B
            .comment("Whether falling debris injures whatever it lands on.")
            .define("debrisDamageEnabled", true);

    public static final ForgeConfigSpec.DoubleValue DEBRIS_DAMAGE_MULTIPLIER = B
            .comment("Scales debris impact damage. Damage is fall distance times block weight times this.")
            .defineInRange("debrisDamageMultiplier", 1.0D, 0.0D, 100.0D);

    public static final ForgeConfigSpec.BooleanValue TUNNEL_COLLAPSE = B
            .comment("Whether cutting into rock makes the rock around the hole answer for itself.",
                    "With this on, a corridor one or two blocks wide stands on its own and anything",
                    "wider needs a pillar left in it or grout in the roof. With it off, excavations",
                    "behave as they always did: the rock around a tunnel anchors itself and never falls.",
                    "Rock nobody has cut into is never affected either way, so an existing world is only",
                    "ever destabilised where a pick has actually gone in.")
            .define("tunnelCollapse", true);

    public static final ForgeConfigSpec.IntValue CAVERN_SPAN = B
            .comment("How far a natural ceiling has to reach before breaking it starts a cave-in.",
                    "Measured as the number of blocks you would have to walk across the ceiling to",
                    "reach the nearest wall or pillar. Six means a hall wider than about thirteen",
                    "blocks is fragile, and anything narrower is as solid as it ever was. Raise it to",
                    "make only truly enormous caverns dangerous, lower it to make most caves risky.")
            .defineInRange("cavernSpan", 6, 1, 64);

    public static final ForgeConfigSpec.BooleanValue CRACK_WARNING_ENABLED = B
            .comment("Play the crack warning and show cracked overlays before a collapse.")
            .define("crackWarningEnabled", true);

    public static final ForgeConfigSpec.DoubleValue WEIGHT_MULTIPLIER = B
            .comment("Global multiplier applied to every material weight.")
            .defineInRange("weightMultiplier", 1.0D, 0.01D, 100.0D);

    public static final ForgeConfigSpec.DoubleValue STRENGTH_MULTIPLIER = B
            .comment("Global multiplier applied to every compressive strength.")
            .defineInRange("strengthMultiplier", 1.0D, 0.01D, 100.0D);

    public static final ForgeConfigSpec.DoubleValue SPAN_MULTIPLIER = B
            .comment("Global multiplier applied to every maximum span.")
            .defineInRange("spanMultiplier", 1.0D, 0.01D, 100.0D);

    public static final ForgeConfigSpec.IntValue ANCHOR_DEPTH = B
            .comment("Depth below the surface at which any block counts as an anchor.")
            .defineInRange("anchorDepth", 6, 0, 128);

    public static final ForgeConfigSpec SPEC = B.build();

    private Config() {}

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
}
