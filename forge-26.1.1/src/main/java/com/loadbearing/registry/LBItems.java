package com.loadbearing.registry;

import java.util.ArrayList;
import java.util.List;

import com.loadbearing.LoadBearing;
import com.loadbearing.item.ReinforcingGroutItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LBItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(Registries.ITEM, LoadBearing.MODID);

    private static final List<RegistryObject<? extends Item>> TAB_ORDER = new ArrayList<>();

    private LBItems() {}
    public static final RegistryObject<ReinforcingGroutItem> REINFORCING_GROUT = tab(REGISTRY.register(
            "reinforcing_grout", () -> new ReinforcingGroutItem(new Item.Properties()
                    .setId(REGISTRY.key("reinforcing_grout")).stacksTo(64))));

    public static final RegistryObject<Item> STEEL_INGOT = tab(REGISTRY.register(
            "steel_ingot", () -> new Item(new Item.Properties()
                    .setId(REGISTRY.key("steel_ingot")))));

    private static <T extends Item> RegistryObject<T> tab(RegistryObject<T> item) {
        TAB_ORDER.add(item);
        return item;
    }

    public static List<RegistryObject<? extends Item>> tabOrder() {
        return List.copyOf(TAB_ORDER);
    }
}
