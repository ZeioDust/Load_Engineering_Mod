package com.loadbearing.material;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record MaterialFile(Optional<ResourceLocation> block, MaterialProfile profile) {
    public static final Codec<MaterialFile> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.optionalFieldOf("block").forGetter(MaterialFile::block),
            MaterialProfile.MAP_CODEC.forGetter(MaterialFile::profile)
    ).apply(i, MaterialFile::new));
}
