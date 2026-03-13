package com.sanitycraft.client.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class BloodParticle extends TextureSheetParticle {
	public static BloodParticleProvider provider(SpriteSet spriteSet) {
		return new BloodParticleProvider(spriteSet);
	}

	private final SpriteSet spriteSet;

	protected BloodParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
		super(level, x, y, z);
		this.spriteSet = spriteSet;
		this.setSize(0.2F, 0.2F);
		this.lifetime = 8;
		this.gravity = 0.0F;
		this.hasPhysics = true;
		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;
		this.pickSprite(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(spriteSet);
	}

	public static final class BloodParticleProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		private BloodParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new BloodParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
		}
	}
}
