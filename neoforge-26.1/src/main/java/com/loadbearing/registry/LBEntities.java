package com.loadbearing.registry;

import com.loadbearing.LoadBearing;
import com.loadbearing.entity.DustCloudEntity;
import com.loadbearing.entity.FallingDebrisEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LBEntities {
    public static final DeferredRegister.Entities REGISTRY =
            DeferredRegister.createEntities(LoadBearing.MODID);

    private LBEntities() {}

    public static final DeferredHolder<EntityType<?>, EntityType<FallingDebrisEntity>> FALLING_DEBRIS =
            REGISTRY.registerEntityType("falling_debris", FallingDebrisEntity::new, MobCategory.MISC,
                    b -> b.sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20).noSummon());

    public static final DeferredHolder<EntityType<?>, EntityType<DustCloudEntity>> DUST_CLOUD =
            REGISTRY.registerEntityType("dust_cloud", DustCloudEntity::new, MobCategory.MISC,
                    b -> b.sized(3.0F, 3.0F).clientTrackingRange(10).updateInterval(10).noSummon());
}
