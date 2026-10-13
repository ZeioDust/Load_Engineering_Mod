package com.loadbearing.registry;

import com.loadbearing.LoadBearing;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LBSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(Registries.SOUND_EVENT, LoadBearing.MODID);

    private LBSounds() {}

    public static final DeferredHolder<SoundEvent, SoundEvent> STRESS_CREAK = sound("stress_creak");

    public static final DeferredHolder<SoundEvent, SoundEvent> CRACK_WARNING = sound("crack_warning");

    public static final DeferredHolder<SoundEvent, SoundEvent> COLLAPSE_RUMBLE = sound("collapse_rumble");

    public static final DeferredHolder<SoundEvent, SoundEvent> DEBRIS_IMPACT = sound("debris_impact");

    public static final DeferredHolder<SoundEvent, SoundEvent> CRANE_MOTOR = sound("crane_motor");

    public static final DeferredHolder<SoundEvent, SoundEvent> CONCRETE_POUR = sound("concrete_pour");

    public static final DeferredHolder<SoundEvent, SoundEvent> GAUGE_BEEP = sound("gauge_beep");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(LoadBearing.id(name)));
    }
}
