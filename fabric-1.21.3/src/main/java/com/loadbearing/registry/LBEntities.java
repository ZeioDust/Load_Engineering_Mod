package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.LoadBearing;
import com.loadbearing.entity.DustCloudEntity;
import com.loadbearing.entity.FallingDebrisEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class LBEntities {
    private LBEntities() {}

    public static final Supplier<EntityType<FallingDebrisEntity>> FALLING_DEBRIS =
            register("falling_debris", key -> EntityType.Builder
                    .<FallingDebrisEntity>of(FallingDebrisEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20).noSummon()
                    .build(key));

    public static final Supplier<EntityType<DustCloudEntity>> DUST_CLOUD =
            register("dust_cloud", key -> EntityType.Builder
                    .<DustCloudEntity>of(DustCloudEntity::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F).clientTrackingRange(10).updateInterval(10).noSummon()
                    .build(key));

    public static void init() {}

    private static <T extends net.minecraft.world.entity.Entity> Supplier<EntityType<T>> register(
            String name,
            java.util.function.Function<ResourceKey<EntityType<?>>, EntityType<T>> factory) {
        ResourceKey<EntityType<?>> key =
                ResourceKey.create(Registries.ENTITY_TYPE, LoadBearing.id(name));
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, factory.apply(key));
        return () -> type;
    }
}
