package com.loadbearing.material;

import java.util.HashMap;
import java.util.Map;

import com.loadbearing.LoadBearing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class MaterialDataLoader extends SimpleJsonResourceReloadListener<MaterialFile> {
    public static final String DIRECTORY = LoadBearing.MODID + "/materials";

    public MaterialDataLoader() {
        super(MaterialFile.CODEC, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, MaterialFile> parsed, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, MaterialProfile> overrides = new HashMap<>(parsed.size());
        for (Map.Entry<ResourceLocation, MaterialFile> entry : parsed.entrySet()) {
            MaterialFile file = entry.getValue();
            ResourceLocation target = file.block().orElse(entry.getKey());
            overrides.put(target, file.profile());
        }
        MaterialRegistry.setDatapackOverrides(overrides);
    }
}
