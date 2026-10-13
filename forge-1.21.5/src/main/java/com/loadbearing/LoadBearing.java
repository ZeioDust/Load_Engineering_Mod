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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

@Mod(LoadBearing.MODID)
public final class LoadBearing {
    public static final String MODID = "loadbearing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LoadBearing() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        LBItems.REGISTRY.register(modBus);
        LBEntities.REGISTRY.register(modBus);
        LBTabs.REGISTRY.register(modBus);
        LBSounds.REGISTRY.register(modBus);
        LBParticles.REGISTRY.register(modBus);

        modBus.addListener(LBNetwork::register);
        modBus.addListener(LBAttachments::registerCapabilities);
        LBTriggers.REGISTRY.register(modBus);

        MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, LBAttachments::attach);
        MinecraftForge.EVENT_BUS.addListener(LoadBearing::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(LoadBearing::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(LoadBearing::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(LoadBearing::onServerStopped);
        StructuralEventHandler.register(MinecraftForge.EVENT_BUS);
        ReinforcementSender.register(MinecraftForge.EVENT_BUS);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> LoadBearingClient::init);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDataLoader());
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        LoadBearingCommand.register(event.getDispatcher());
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
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
