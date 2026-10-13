package com.loadbearing.registry;

import com.loadbearing.LoadBearing;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class LBDamage {
    public static final ResourceKey<DamageType> FALLING_DEBRIS =
            ResourceKey.create(Registries.DAMAGE_TYPE, LoadBearing.id("falling_debris"));

    private LBDamage() {}

    public static DamageSource fallingDebris(Level level, Entity debris) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(FALLING_DEBRIS),
                debris);
    }
}
