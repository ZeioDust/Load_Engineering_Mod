package com.loadbearing.registry;

import com.loadbearing.LoadBearing;
import com.loadbearing.advancement.LBCriteria;
import com.loadbearing.advancement.StructuralTrigger;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LBTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> REGISTRY =
            DeferredRegister.create(Registries.TRIGGER_TYPE, LoadBearing.MODID);

    private LBTriggers() {}

    public static final DeferredHolder<CriterionTrigger<?>, StructuralTrigger> STRUCTURAL =
            REGISTRY.register("structural", () -> {
                StructuralTrigger trigger = new StructuralTrigger();
                LBCriteria.STRUCTURAL = trigger;
                return trigger;
            });
}
