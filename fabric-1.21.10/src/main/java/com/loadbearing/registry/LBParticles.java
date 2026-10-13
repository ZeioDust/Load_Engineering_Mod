package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.LoadBearing;

import net.minecraft.core.Registry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class LBParticles {
    private LBParticles() {}

    public static final Supplier<SimpleParticleType> CONCRETE_DUST = particle("concrete_dust", false);

    public static final Supplier<SimpleParticleType> SPARK = particle("spark", false);

    public static final Supplier<SimpleParticleType> DEBRIS_CHIP = particle("debris_chip", true);

    public static void init() {}

    private static Supplier<SimpleParticleType> particle(String name, boolean alwaysShow) {
        SimpleParticleType type = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                LoadBearing.id(name), FabricParticleTypes.simple(alwaysShow));
        return () -> type;
    }
}
