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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(LoadBearing.MODID)
public final class LoadBearing {
    public static final String MODID = "loadbearing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LoadBearing(FMLJavaModLoadingContext context) {
        BusGroup modBus = context.getModBusGroup();

        LBItems.REGISTRY.register(modBus);
        LBEntities.REGISTRY.register(modBus);
        LBTabs.REGISTRY.register(modBus);
        LBSounds.REGISTRY.register(modBus);
        LBParticles.REGISTRY.register(modBus);
        LBTriggers.REGISTRY.register(modBus);

        FMLCommonSetupEvent.getBus(modBus).addListener(LBNetwork::register);

        AttachCapabilitiesEvent.LevelChunks.BUS.addListener(LBAttachments::attach);
        AddReloadListenerEvent.BUS.addListener(LoadBearing::onAddReloadListeners);
        RegisterCommandsEvent.BUS.addListener(LoadBearing::onRegisterCommands);
        TickEvent.ServerTickEvent.Post.BUS.addListener(LoadBearing::onServerTick);
        ServerStoppedEvent.BUS.addListener(LoadBearing::onServerStopped);
        StructuralEventHandler.register();
        ReinforcementSender.register();

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // DistExecutor is gone on this loader; the client half is entered behind a
        // plain dist check, so the client class is never loaded on a server.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LoadBearingClient.init();
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDataLoader());
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        LoadBearingCommand.register(event.getDispatcher());
    }

    private static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        SolverScheduler.get().runTickBudget(event.server());
        CollapseManager.get().tick(event.server());
        com.loadbearing.command.SelfTest.get().tick(event.server());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        SolverScheduler.get().shutdown();
        com.loadbearing.command.SelfTest.get().cancel();
        StructuralEventHandler.clearPistonState();
        CollapseManager.get().clear();
        MaterialRegistry.clearDatapackOverrides();
    }
}
