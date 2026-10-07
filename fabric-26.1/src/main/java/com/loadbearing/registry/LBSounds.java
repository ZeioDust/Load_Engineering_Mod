package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.LoadBearing;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class LBSounds {
    private LBSounds() {}

    public static final Supplier<SoundEvent> STRESS_CREAK = sound("stress_creak");

    public static final Supplier<SoundEvent> CRACK_WARNING = sound("crack_warning");

    public static final Supplier<SoundEvent> COLLAPSE_RUMBLE = sound("collapse_rumble");

    public static final Supplier<SoundEvent> DEBRIS_IMPACT = sound("debris_impact");

    public static final Supplier<SoundEvent> CRANE_MOTOR = sound("crane_motor");

    public static final Supplier<SoundEvent> CONCRETE_POUR = sound("concrete_pour");

    public static final Supplier<SoundEvent> GAUGE_BEEP = sound("gauge_beep");

    public static void init() {}

    private static Supplier<SoundEvent> sound(String name) {
        SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, LoadBearing.id(name),
                SoundEvent.createVariableRangeEvent(LoadBearing.id(name)));
        return () -> event;
    }
}
