package com.loadbearing;

import com.loadbearing.client.ReinforcementOverlay;
import com.loadbearing.client.renderer.LBEntityRenderers;
import com.loadbearing.net.LBClientNetwork;
import com.loadbearing.registry.LBParticles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * This loader has no config screen extension point, so the mod does not register one; the config
 * file is still written and read exactly as on the other targets.
 */
public final class LoadBearingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LBClientNetwork.register();
        LBEntityRenderers.register();

        ParticleProviderRegistry.getInstance()
                .register(LBParticles.CONCRETE_DUST.get(), DustParticle::new);
        ParticleProviderRegistry.getInstance()
                .register(LBParticles.SPARK.get(), SparkParticle::new);
        ParticleProviderRegistry.getInstance()
                .register(LBParticles.DEBRIS_CHIP.get(), ChipParticle::new);

        LevelRenderEvents.COLLECT_SUBMITS.register(ReinforcementOverlay::onSubmitGeometry);
        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> ReinforcementOverlay.clear());
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
