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
import net.minecraftforge.client.event.AddFramePassEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

/**
 * This loader has no config-screen extension point of the kind NeoForge exposes, so the mod does
 * not register one; the config file is still written and read exactly as on the other targets.
 */
public final class LoadBearingClient {
    private LoadBearingClient() {}

    public static void init() {
        EntityRenderersEvent.RegisterRenderers.BUS
                .addListener(LoadBearingClient::onRegisterRenderers);
        RegisterParticleProvidersEvent.BUS
                .addListener(LoadBearingClient::onRegisterParticles);

        AddFramePassEvent.BUS.addListener(ReinforcementOverlay::onAddFramePass);
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(LoadBearingClient::onLoggedOut);
        ClientPlayerNetworkEvent.Clone.BUS.addListener(LoadBearingClient::onPlayerCloned);
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
