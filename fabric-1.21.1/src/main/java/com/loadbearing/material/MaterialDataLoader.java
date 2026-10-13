package com.loadbearing.material;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.loadbearing.LoadBearing;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MaterialDataLoader extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = LoadBearing.MODID + "/materials";

    private static final Gson GSON = new Gson();

    public MaterialDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> parsed, ResourceManager manager,
            ProfilerFiller profiler) {
        Map<ResourceLocation, MaterialProfile> overrides = new HashMap<>(parsed.size());
        for (Map.Entry<ResourceLocation, JsonElement> entry : parsed.entrySet()) {
            MaterialFile file = MaterialFile.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(message -> new IllegalStateException(
                            "Invalid material profile " + entry.getKey() + ": " + message));
            ResourceLocation target = file.block().orElse(entry.getKey());
            overrides.put(target, file.profile());
        }
        MaterialRegistry.setDatapackOverrides(overrides);
    }
}
