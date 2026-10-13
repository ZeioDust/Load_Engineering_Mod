package com.loadbearing.registry;

import com.loadbearing.LoadBearing;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LBTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LoadBearing.MODID);

    private LBTabs() {}

    public static final RegistryObject<CreativeModeTab> TAB = REGISTRY.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.loadbearing.tab"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> LBItems.REINFORCING_GROUT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (RegistryObject<? extends Item> item : LBItems.tabOrder()) {
                            output.accept(item.get());
                        }
                    })
                    .build());
}
