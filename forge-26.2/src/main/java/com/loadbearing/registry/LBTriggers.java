package com.loadbearing.registry;

import com.loadbearing.LoadBearing;
import com.loadbearing.advancement.LBCriteria;
import com.loadbearing.advancement.StructuralTrigger;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LBTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> REGISTRY =
            DeferredRegister.create(Registries.TRIGGER_TYPE, LoadBearing.MODID);

    private LBTriggers() {}

    public static final RegistryObject<StructuralTrigger> STRUCTURAL =
            REGISTRY.register("structural", () -> {
                StructuralTrigger trigger = new StructuralTrigger();
                LBCriteria.STRUCTURAL = trigger;
                return trigger;
            });
}
