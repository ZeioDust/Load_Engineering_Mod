package com.loadbearing.registry;

import com.loadbearing.LoadBearing;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LBParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(Registries.PARTICLE_TYPE, LoadBearing.MODID);

    private LBParticles() {}

    public static final RegistryObject<SimpleParticleType> CONCRETE_DUST =
            REGISTRY.register("concrete_dust", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> SPARK =
            REGISTRY.register("spark", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> DEBRIS_CHIP =
            REGISTRY.register("debris_chip", () -> new SimpleParticleType(true));
}
