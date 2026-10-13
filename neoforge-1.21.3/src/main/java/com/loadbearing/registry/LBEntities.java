package com.loadbearing.registry;

import com.loadbearing.LoadBearing;
import com.loadbearing.entity.DustCloudEntity;
import com.loadbearing.entity.FallingDebrisEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * This loader version has no entity helper on DeferredRegister, so the same EntityType.Builder
 * settings go through a plain register call.
 */
public final class LBEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.ENTITY_TYPE, LoadBearing.MODID);

    private LBEntities() {}

    public static final DeferredHolder<EntityType<?>, EntityType<FallingDebrisEntity>> FALLING_DEBRIS =
            REGISTRY.register("falling_debris", () -> EntityType.Builder
                    .<FallingDebrisEntity>of(FallingDebrisEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20).noSummon()
                    .build(key("falling_debris")));

    public static final DeferredHolder<EntityType<?>, EntityType<DustCloudEntity>> DUST_CLOUD =
            REGISTRY.register("dust_cloud", () -> EntityType.Builder
                    .<DustCloudEntity>of(DustCloudEntity::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F).clientTrackingRange(10).updateInterval(10).noSummon()
                    .build(key("dust_cloud")));

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, LoadBearing.id(name));
    }
}
