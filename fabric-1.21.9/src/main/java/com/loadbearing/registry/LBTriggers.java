package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.LoadBearing;
import com.loadbearing.advancement.LBCriteria;
import com.loadbearing.advancement.StructuralTrigger;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class LBTriggers {
    private LBTriggers() {}

    public static final Supplier<StructuralTrigger> STRUCTURAL = register();

    public static void init() {}

    private static Supplier<StructuralTrigger> register() {
        StructuralTrigger trigger = Registry.register(BuiltInRegistries.TRIGGER_TYPES,
                LoadBearing.id("structural"), new StructuralTrigger());
        LBCriteria.STRUCTURAL = trigger;
        return () -> trigger;
    }
}
