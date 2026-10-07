package com.loadbearing.entity;

import com.loadbearing.registry.LBEntities;
import com.loadbearing.registry.LBParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DustCloudEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(DustCloudEntity.class, EntityDataSerializers.FLOAT);

    private static final int LIFETIME = 120;

    private int age;

    public DustCloudEntity(EntityType<? extends DustCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static DustCloudEntity create(ServerLevel level, BlockPos pos, float radius) {
        DustCloudEntity cloud = new DustCloudEntity(LBEntities.DUST_CLOUD.get(), level);
        cloud.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        cloud.setRadius(radius);
        return cloud;
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, Math.max(0.5F, radius));
    }

    public float progress(float partialTick) {
        return Math.min(1.0F, (this.age + partialTick) / LIFETIME);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_RADIUS, 2.0F);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public final boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return false;
    }

    @Override
    public void tick() {
        this.age++;
        if (this.age > LIFETIME) {
            discard();
            return;
        }

        double lift = this.age < LIFETIME / 3 ? 0.03D : -0.01D;
        setPos(getX(), getY() + lift, getZ());

        if (this.level() instanceof ServerLevel serverLevel && this.age % 4 == 0) {
            float radius = getRadius() * (0.4F + 0.6F * progress(0.0F));
            serverLevel.sendParticles(LBParticles.CONCRETE_DUST.get(),
                    getX(), getY(), getZ(),
                    6, radius, radius * 0.4D, radius, 0.01D);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Age", this.age);
        output.putFloat("Radius", getRadius());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.age = input.getIntOr("Age", 0);
        setRadius(input.getFloatOr("Radius", 2.0F));
    }
}
