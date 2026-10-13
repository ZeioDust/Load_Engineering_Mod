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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = LoadBearing.MODID, dist = Dist.CLIENT)
public final class LoadBearingClient {
    public LoadBearingClient(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(LoadBearingClient::onRegisterRenderers);
        modBus.addListener(LoadBearingClient::onRegisterParticles);

        NeoForge.EVENT_BUS.addListener(ReinforcementOverlay::onSubmitGeometry);
        NeoForge.EVENT_BUS.addListener(LoadBearingClient::onLoggedOut);
        NeoForge.EVENT_BUS.addListener(LoadBearingClient::onPlayerCloned);
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
                double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            SuspendedTownParticle particle = new SuspendedTownParticle(
                    level, x, y, z, xa, ya * 0.4D, za, this.sprites.get(random));
            particle.setColor(0.62F, 0.60F, 0.57F);
            particle.setLifetime(30 + random.nextInt(30));
            return particle;
        }
    }

    private record SparkParticle(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            SuspendedTownParticle particle = new SuspendedTownParticle(
                    level, x, y, z, xa, ya, za, this.sprites.get(random));
            particle.setColor(1.0F, 0.78F, 0.32F);
            particle.setLifetime(8 + random.nextInt(8));
            return particle;
        }
    }

    private record ChipParticle(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            SuspendedTownParticle particle = new SuspendedTownParticle(
                    level, x, y, z, xa, ya, za, this.sprites.get(random));
            particle.setColor(0.45F, 0.44F, 0.42F);
            particle.setLifetime(12 + random.nextInt(12));
            return particle;
        }
    }
}
