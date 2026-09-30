package com.loadbearing.registry;

import java.util.ArrayList;
import java.util.List;

import com.loadbearing.LoadBearing;
import com.loadbearing.item.ReinforcingGroutItem;
import com.loadbearing.item.StressWandItem;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LBItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(LoadBearing.MODID);

    private static final List<DeferredItem<? extends Item>> TAB_ORDER = new ArrayList<>();

    private LBItems() {}

    public static final DeferredItem<StressWandItem> STRESS_WAND = tab(REGISTRY.registerItem(
            "stress_wand", StressWandItem::new, p -> p.stacksTo(1).durability(256)));

    public static final DeferredItem<ReinforcingGroutItem> REINFORCING_GROUT = tab(REGISTRY.registerItem(
            "reinforcing_grout", ReinforcingGroutItem::new, p -> p.stacksTo(64)));

    public static final DeferredItem<Item> STEEL_INGOT = tab(REGISTRY.registerSimpleItem("steel_ingot"));

    private static <T extends Item> DeferredItem<T> tab(DeferredItem<T> item) {
        TAB_ORDER.add(item);
        return item;
    }

    public static List<DeferredItem<? extends Item>> tabOrder() {
        return List.copyOf(TAB_ORDER);
    }
}
