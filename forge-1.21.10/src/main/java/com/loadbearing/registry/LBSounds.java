package com.loadbearing.registry;

import com.loadbearing.LoadBearing;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LBSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(Registries.SOUND_EVENT, LoadBearing.MODID);

    private LBSounds() {}

    public static final RegistryObject<SoundEvent> STRESS_CREAK = sound("stress_creak");

    public static final RegistryObject<SoundEvent> CRACK_WARNING = sound("crack_warning");

    public static final RegistryObject<SoundEvent> COLLAPSE_RUMBLE = sound("collapse_rumble");

    public static final RegistryObject<SoundEvent> DEBRIS_IMPACT = sound("debris_impact");

    public static final RegistryObject<SoundEvent> CRANE_MOTOR = sound("crane_motor");

    public static final RegistryObject<SoundEvent> CONCRETE_POUR = sound("concrete_pour");

    public static final RegistryObject<SoundEvent> GAUGE_BEEP = sound("gauge_beep");

    private static RegistryObject<SoundEvent> sound(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(LoadBearing.id(name)));
    }
}
