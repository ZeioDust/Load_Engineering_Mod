package com.loadbearing.material;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.loadbearing.LoadBearing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MaterialRegistry {
    private static final Map<Block, MaterialProfile> BUILT_IN = new ConcurrentHashMap<>(512);

    private static volatile Map<Identifier, MaterialProfile> DATAPACK = Map.of();

    private static final Map<BlockState, MaterialProfile> CACHE = new ConcurrentHashMap<>(4096);

    private static volatile boolean vanillaLoaded = false;

    private MaterialRegistry() {}

    public static void put(Block block, MaterialProfile profile) {
        BUILT_IN.put(block, profile);
        CACHE.clear();
    }

    public static void setDatapackOverrides(Map<Identifier, MaterialProfile> overrides) {
        DATAPACK = Map.copyOf(overrides);
        CACHE.clear();
        LoadBearing.LOGGER.info("Loaded {} datapack material profile(s)", overrides.size());
    }

    public static void clearDatapackOverrides() {
        DATAPACK = Map.of();
        CACHE.clear();
    }

    public static MaterialProfile get(BlockState state) {
        if (state == null || state.isAir()) {
            return MaterialProfile.VOID;
        }
        MaterialProfile cached = CACHE.get(state);
        if (cached != null) {
            return cached;
        }
        MaterialProfile resolved = resolve(state);
        CACHE.put(state, resolved);
        return resolved;
    }

    private static MaterialProfile resolve(BlockState state) {
        ensureVanillaLoaded();
        Block block = state.getBlock();

        Map<Identifier, MaterialProfile> pack = DATAPACK;
        if (!pack.isEmpty()) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            MaterialProfile fromPack = pack.get(id);
            if (fromPack != null) {
                return fromPack;
            }
        }

        MaterialProfile builtIn = BUILT_IN.get(block);
        if (builtIn != null) {
            return builtIn;
        }

        try {
            return BuiltinProfiles.infer(state);
        } catch (RuntimeException e) {
            return MaterialProfile.DEFAULT;
        }
    }

    private static void ensureVanillaLoaded() {
        if (vanillaLoaded) {
            return;
        }
        synchronized (MaterialRegistry.class) {
            if (vanillaLoaded) {
                return;
            }
            BuiltinProfiles.registerVanilla(BUILT_IN::put);
            vanillaLoaded = true;
        }
    }

    public static double weight(BlockState state) {
        return get(state).scaledWeight();
    }

    public static int maxSpan(BlockState state) {
        return get(state).scaledSpan();
    }

    public static boolean tensionOnly(BlockState state) {
        return get(state).tensionOnly();
    }

    public static double soilBearing(BlockState state) {
        return get(state).scaledSoilBearing();
    }

    public static boolean brittle(BlockState state) {
        return get(state).brittle();
    }
}
