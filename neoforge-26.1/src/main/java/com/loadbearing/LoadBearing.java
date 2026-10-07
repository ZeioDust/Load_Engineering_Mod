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
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(LoadBearing.MODID)
public final class LoadBearing {
    public static final String MODID = "loadbearing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LoadBearing(IEventBus modBus, ModContainer container) {
        LBItems.REGISTRY.register(modBus);
        LBEntities.REGISTRY.register(modBus);
        LBTabs.REGISTRY.register(modBus);
        LBSounds.REGISTRY.register(modBus);
        LBParticles.REGISTRY.register(modBus);
        LBAttachments.REGISTRY.register(modBus);
        LBTriggers.REGISTRY.register(modBus);

        modBus.addListener(LBNetwork::register);

        NeoForge.EVENT_BUS.addListener(LoadBearing::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(LoadBearing::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(LoadBearing::onServerTick);
        NeoForge.EVENT_BUS.addListener(LoadBearing::onServerStopped);
        StructuralEventHandler.register(NeoForge.EVENT_BUS);
        ReinforcementSender.register(NeoForge.EVENT_BUS);

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(id("material_profiles"), new MaterialDataLoader());
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        LoadBearingCommand.register(event.getDispatcher());
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        SolverScheduler.get().runTickBudget(event.getServer());
        CollapseManager.get().tick(event.getServer());
        com.loadbearing.command.SelfTest.get().tick(event.getServer());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        SolverScheduler.get().shutdown();
        com.loadbearing.command.SelfTest.get().cancel();
        StructuralEventHandler.clearPistonState();
        CollapseManager.get().clear();
        MaterialRegistry.clearDatapackOverrides();
    }
}
