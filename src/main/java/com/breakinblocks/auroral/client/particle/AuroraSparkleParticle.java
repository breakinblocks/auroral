package com.breakinblocks.auroral.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Aurora sparkle particle - creates a shimmering, colorful effect
 * reminiscent of the Northern Lights.
 */
public class AuroraSparkleParticle extends SimpleAnimatedParticle {
    private final float rotSpeed;

    protected AuroraSparkleParticle(ClientLevel level, double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed,
                                    SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.0F);
        this.setLifetime(40 + this.random.nextInt(20));
        this.hasPhysics = false;

        // Aurora colors - greens and pinks
        float colorChoice = this.random.nextFloat();
        if (colorChoice < 0.5F) {
            // Green tones
            this.rCol = 0.2F + this.random.nextFloat() * 0.3F;
            this.gCol = 0.8F + this.random.nextFloat() * 0.2F;
            this.bCol = 0.3F + this.random.nextFloat() * 0.3F;
        } else {
            // Pink/purple tones
            this.rCol = 0.8F + this.random.nextFloat() * 0.2F;
            this.gCol = 0.3F + this.random.nextFloat() * 0.3F;
            this.bCol = 0.8F + this.random.nextFloat() * 0.2F;
        }

        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.rotSpeed = (this.random.nextFloat() - 0.5F) * 0.1F;

        this.xd = xSpeed;
        this.yd = ySpeed + 0.01;
        this.zd = zSpeed;

        // Pick initial sprite from the sprite set
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += this.rotSpeed;

        // Fade out over lifetime
        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0F - progress;

        // Gentle float upward
        this.yd += 0.001;
    }

    @Override
    public int getLightColor(float partialTick) {
        // Self-illuminated
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new AuroraSparkleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
