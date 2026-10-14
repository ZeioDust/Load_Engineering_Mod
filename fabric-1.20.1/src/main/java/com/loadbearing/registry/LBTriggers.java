package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.advancement.LBCriteria;
import com.loadbearing.advancement.StructuralTrigger;

import net.minecraft.advancements.CriteriaTriggers;

/**
 * This era had no trigger-type registry; criteria were handed straight to CriteriaTriggers. The
 * registered id is unchanged.
 */
public final class LBTriggers {
    public static final Supplier<StructuralTrigger> STRUCTURAL = register();

    private LBTriggers() {}

    public static void init() {}

    private static Supplier<StructuralTrigger> register() {
        StructuralTrigger trigger = CriteriaTriggers.register(new StructuralTrigger());
        LBCriteria.STRUCTURAL = trigger;
        return () -> trigger;
    }
}
