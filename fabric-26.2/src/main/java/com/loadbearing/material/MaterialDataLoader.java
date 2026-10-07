package com.loadbearing.material;

import java.util.HashMap;
import java.util.Map;

import com.loadbearing.LoadBearing;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MaterialDataLoader extends SimpleJsonResourceReloadListener<MaterialFile> {
    public static final String DIRECTORY = LoadBearing.MODID + "/materials";

    public MaterialDataLoader() {
        super(MaterialFile.CODEC, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, MaterialFile> parsed, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, MaterialProfile> overrides = new HashMap<>(parsed.size());
        for (Map.Entry<Identifier, MaterialFile> entry : parsed.entrySet()) {
            MaterialFile file = entry.getValue();
            Identifier target = file.block().orElse(entry.getKey());
            overrides.put(target, file.profile());
        }
        MaterialRegistry.setDatapackOverrides(overrides);
    }
}
