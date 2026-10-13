package com.loadbearing.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.loadbearing.LoadBearing;
import com.loadbearing.item.ReinforcingGroutItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * This loader registers eagerly rather than through a deferred register. The fields stay Suppliers
 * so that every call site outside this class keeps the get() it is written with.
 */
public final class LBItems {
    private static final List<Supplier<? extends Item>> TAB_ORDER = new ArrayList<>();

    private LBItems() {}
    public static final Supplier<ReinforcingGroutItem> REINFORCING_GROUT = tab(register(
            "reinforcing_grout",
            key -> new ReinforcingGroutItem(new Item.Properties().setId(key).stacksTo(64))));

    public static final Supplier<Item> STEEL_INGOT = tab(register("steel_ingot",
            key -> new Item(new Item.Properties().setId(key))));

    public static void init() {}

    private static <T extends Item> Supplier<T> register(String name,
            java.util.function.Function<ResourceKey<Item>, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, LoadBearing.id(name));
        T item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(key));
        return () -> item;
    }

    private static <T extends Item> Supplier<T> tab(Supplier<T> item) {
        TAB_ORDER.add(item);
        return item;
    }

    public static List<Supplier<? extends Item>> tabOrder() {
        return List.copyOf(TAB_ORDER);
    }
}
