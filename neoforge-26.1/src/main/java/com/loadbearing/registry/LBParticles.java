package com.loadbearing.registry;

import com.loadbearing.LoadBearing;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LBParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(Registries.PARTICLE_TYPE, LoadBearing.MODID);

    private LBParticles() {}

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CONCRETE_DUST =
            REGISTRY.register("concrete_dust", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK =
            REGISTRY.register("spark", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DEBRIS_CHIP =
            REGISTRY.register("debris_chip", () -> new SimpleParticleType(true));
}
