package com.loadbearing;

import com.loadbearing.collapse.CollapseManager;
import com.loadbearing.command.LoadBearingCommand;
import com.loadbearing.material.MaterialDataLoader;
import com.loadbearing.material.MaterialRegistry;
import com.loadbearing.net.LBNetwork;
import com.loadbearing.registry.LBAttachments;
import com.loadbearing.registry.LBEntities;
import com.loadbearing.registry.LBItems;
import com.loadbearing.registry.LBParticles;
import com.loadbearing.registry.LBSounds;
import com.loadbearing.registry.LBTabs;
import com.loadbearing.registry.LBTriggers;
import com.loadbearing.solver.ReinforcementSender;
import com.loadbearing.solver.SolverScheduler;
import com.loadbearing.solver.StructuralEventHandler;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public final class LoadBearing implements ModInitializer {
    public static final String MODID = "loadbearing";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Config.load();

        LBItems.init();
        LBEntities.init();
        LBTabs.init();
        LBSounds.init();
        LBParticles.init();
        LBAttachments.init();
        LBTriggers.init();

        LBNetwork.register();

        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(id("material_profiles"),
                        lookup -> new IdentifiedMaterialDataLoader());

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, context, selection) -> LoadBearingCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(LoadBearing::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> onServerStopped());
        StructuralEventHandler.register();
        ReinforcementSender.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void onServerTick(net.minecraft.server.MinecraftServer server) {
        SolverScheduler.get().runTickBudget(server);
        CollapseManager.get().tick(server);
        com.loadbearing.command.SelfTest.get().tick(server);
    }

    private static void onServerStopped() {
        SolverScheduler.get().shutdown();
        com.loadbearing.command.SelfTest.get().cancel();
        StructuralEventHandler.clearPistonState();
        CollapseManager.get().clear();
        MaterialRegistry.clearDatapackOverrides();
    }

    /**
     * This loader identifies reload listeners by id, which the shared listener has no place for.
     */
    private static final class IdentifiedMaterialDataLoader extends MaterialDataLoader
            implements IdentifiableResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return id("material_profiles");
        }
    }
}
