package com.breakinblocks.auroral.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Generic shimmer particle - used for various effects like basin infusion,
 * star trails, frost, and general shimmer effects.
 */
public class ShimmerParticle extends SimpleAnimatedParticle {

    protected ShimmerParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed,
                              SpriteSet sprites, float r, float g, float b) {
        super(level, x, y, z, sprites, 0.0F);
        this.setLifetime(20 + this.random.nextInt(20));
        this.hasPhysics = false;

        this.rCol = r + (this.random.nextFloat() - 0.5F) * 0.2F;
        this.gCol = g + (this.random.nextFloat() - 0.5F) * 0.2F;
        this.bCol = b + (this.random.nextFloat() - 0.5F) * 0.2F;

        this.quadSize = 0.08F + this.random.nextFloat() * 0.06F;

        this.xd = xSpeed * 0.1;
        this.yd = ySpeed * 0.1;
        this.zd = zSpeed * 0.1;
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out over lifetime
        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0F - progress;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    // Provider for basin infuse particles - blue swirl
    public static class BasinInfuseProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public BasinInfuseProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new ShimmerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                this.sprites, 0.4F, 0.7F, 1.0F);
        }
    }

    // Provider for star trail particles - golden glow
    public static class StarTrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public StarTrailProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new ShimmerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                this.sprites, 1.0F, 0.9F, 0.4F);
        }
    }

    // Provider for frost particles - icy blue/white
    public static class FrostProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public FrostProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new ShimmerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                this.sprites, 0.8F, 0.9F, 1.0F);
        }
    }

    // Provider for generic shimmer particles - silver/white
    public static class ShimmerProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public ShimmerProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new ShimmerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                this.sprites, 0.9F, 0.9F, 0.95F);
        }
    }
}
