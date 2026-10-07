package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.LoadBearing;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

// withTabsBefore is a NeoForge extension with no counterpart on this loader, so the tab takes
// its default place in the creative menu. Its contents and order within the tab are unchanged.
public final class LBTabs {
    private LBTabs() {}

    public static final Supplier<CreativeModeTab> TAB = register();

    public static void init() {}

    private static Supplier<CreativeModeTab> register() {
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                LoadBearing.id("tab"),
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.loadbearing.tab"))
                        .icon(() -> LBItems.REINFORCING_GROUT.get().getDefaultInstance())
                        .displayItems((parameters, output) -> {
                            for (Supplier<? extends Item> item : LBItems.tabOrder()) {
                                output.accept(item.get());
                            }
                        })
                        .build());
        return () -> tab;
    }
}
