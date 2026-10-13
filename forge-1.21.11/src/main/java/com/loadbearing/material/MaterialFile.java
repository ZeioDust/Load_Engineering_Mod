package com.loadbearing.material;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

public record MaterialFile(Optional<Identifier> block, MaterialProfile profile) {
    public static final Codec<MaterialFile> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.optionalFieldOf("block").forGetter(MaterialFile::block),
            MaterialProfile.MAP_CODEC.forGetter(MaterialFile::profile)
    ).apply(i, MaterialFile::new));
}
