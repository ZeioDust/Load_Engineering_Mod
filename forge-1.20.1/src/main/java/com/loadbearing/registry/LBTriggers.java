package com.loadbearing.registry;

import com.loadbearing.advancement.LBCriteria;
import com.loadbearing.advancement.StructuralTrigger;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * This era had no trigger-type registry to defer into; criteria were handed straight to
 * CriteriaTriggers during common setup. The registered id is unchanged.
 */
public final class LBTriggers {
    public static StructuralTrigger STRUCTURAL;

    private LBTriggers() {}

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            StructuralTrigger trigger = new StructuralTrigger();
            CriteriaTriggers.register(trigger);
            STRUCTURAL = trigger;
            LBCriteria.STRUCTURAL = trigger;
        });
    }
}
