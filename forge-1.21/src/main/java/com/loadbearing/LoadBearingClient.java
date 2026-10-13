package com.loadbearing;

import com.loadbearing.client.ReinforcementOverlay;
import com.loadbearing.client.renderer.LBEntityRenderers;
import com.loadbearing.registry.LBParticles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * SuspendedTownParticle's constructor is package private in this era, so the three
 * providers go through the vanilla sprite provider and then recolour the result;
 * Particle.setColor and Particle.setLifetime are public.
 *
 * This era had no config-screen extension point of the kind newer NeoForge exposes, so the mod does
 * not register one; the config file is still written and read exactly as on the other targets.
 */
public final class LoadBearingClient {
    private LoadBearingClient() {}

    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(LoadBearingClient::onRegisterRenderers);
        modBus.addListener(LoadBearingClient::onRegisterParticles);

        MinecraftForge.EVENT_BUS.addListener(ReinforcementOverlay::onSubmitGeometry);
        MinecraftForge.EVENT_BUS.addListener(LoadBearingClient::onLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(LoadBearingClient::onPlayerCloned);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LBEntityRenderers.register(event);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(LBParticles.CONCRETE_DUST.get(), DustParticle::new);
        event.registerSpriteSet(LBParticles.SPARK.get(), SparkParticle::new);
        event.registerSpriteSet(LBParticles.DEBRIS_CHIP.get(), ChipParticle::new);
    }

    private static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ReinforcementOverlay.clear();
    }

    private static void onPlayerCloned(ClientPlayerNetworkEvent.Clone event) {
        ReinforcementOverlay.clear();
    }

    private record DustParticle(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                double x, double y, double z, double xa, double ya, double za) {
            RandomSource random = level.random;
            Particle particle = new SuspendedTownParticle.Provider(this.sprites)
                    .createParticle(options, level, x, y, z, xa, ya * 0.4D, za);
            particle.setColor(0.62F, 0.60F, 0.57F);
            particle.setLifetime(30 + random.nextInt(30));
            return particle;
        }
    }

    private record SparkParticle(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                double x, double y, double z, double xa, double ya, double za) {
            RandomSource random = level.random;
            Particle particle = new SuspendedTownParticle.Provider(this.sprites)
                    .createParticle(options, level, x, y, z, xa, ya, za);
            particle.setColor(1.0F, 0.78F, 0.32F);
            particle.setLifetime(8 + random.nextInt(8));
            return particle;
        }
    }

    private record ChipParticle(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                double x, double y, double z, double xa, double ya, double za) {
            RandomSource random = level.random;
            Particle particle = new SuspendedTownParticle.Provider(this.sprites)
                    .createParticle(options, level, x, y, z, xa, ya, za);
            particle.setColor(0.45F, 0.44F, 0.42F);
            particle.setLifetime(12 + random.nextInt(12));
            return particle;
        }
    }
}
