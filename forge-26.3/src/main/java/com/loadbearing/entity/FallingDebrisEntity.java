package com.loadbearing.entity;

import java.util.List;

import com.loadbearing.Config;
import com.loadbearing.material.MaterialProfile;
import com.loadbearing.material.MaterialRegistry;
import com.loadbearing.registry.LBDamage;
import com.loadbearing.registry.LBEntities;
import com.loadbearing.registry.LBParticles;
import com.loadbearing.registry.LBSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoveSimulationType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class FallingDebrisEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> DATA_START_POS =
            SynchedEntityData.defineId(FallingDebrisEntity.class, EntityDataSerializers.BLOCK_POS);

    private static final BlockState DEFAULT_STATE = Blocks.STONE.defaultBlockState();

    private static final int MAX_AGE = 600;

    private BlockState blockState = DEFAULT_STATE;
    private int age;

    private boolean shattered;

    public FallingDebrisEntity(EntityType<? extends FallingDebrisEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    private FallingDebrisEntity(Level level, BlockPos pos, BlockState state, boolean shattered) {
        this(LBEntities.FALLING_DEBRIS.get(), level);
        this.blockState = state;
        this.shattered = shattered;
        setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        setDeltaMovement(Vec3.ZERO);
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        setStartPos(blockPosition());
    }

    public static FallingDebrisEntity fall(ServerLevel level, BlockPos pos, BlockState state, boolean crushed) {
        BlockState carried = state.hasProperty(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false)
                : state;
        FallingDebrisEntity entity = new FallingDebrisEntity(level, pos, carried, crushed);
        return level.addFreshEntity(entity) ? entity : null;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public boolean isShattered() {
        return this.shattered;
    }

    public void setStartPos(BlockPos pos) {
        this.entityData.set(DATA_START_POS, pos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06D;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
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
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public MoveSimulationType getMoveSimulationType() {
        // Falling debris is simulated on both sides, as vanilla falling blocks are:
        // without this the client is not allowed to move it and the fall is not drawn.
        return MoveSimulationType.SERVER_AND_CLIENT;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            discard();
            return;
        }

        this.age++;
        applyGravity();
        move(MoverType.SELF, getDeltaMovement());
        applyEffectsFromBlocks();

        if (this.level() instanceof ServerLevel serverLevel) {
            hurtEntitiesInTheWay(serverLevel);

            if (onGround()) {
                land(serverLevel);
            } else if (this.age > MAX_AGE
                    || blockPosition().getY() < this.level().getMinY() - 8
                    || blockPosition().getY() > this.level().getMaxY() + 8) {
                discard();
            }
        }

        setDeltaMovement(getDeltaMovement().scale(0.98D));
    }

    private void hurtEntitiesInTheWay(ServerLevel level) {
        if (!Config.DEBRIS_DAMAGE_ENABLED.get()) {
            return;
        }
        double fallen = fallenDistance();
        if (fallen < 1.0D) {
            return;
        }
        MaterialProfile profile = MaterialRegistry.get(this.blockState);
        float damage = (float) Math.min(60.0D,
                fallen * profile.scaledWeight() * Config.DEBRIS_DAMAGE_MULTIPLIER.get());
        if (damage <= 0.0F) {
            return;
        }

        DamageSource source = LBDamage.fallingDebris(level, this);
        List<Entity> hit = level.getEntities(this, getBoundingBox(), e -> e.isAlive() && e != this);
        for (Entity entity : hit) {
            if (entity instanceof FallingDebrisEntity) {
                continue;
            }
            entity.hurt(source, damage);
        }
    }

    private void land(ServerLevel level) {
        BlockPos pos = blockPosition();
        BlockState landedOn = level.getBlockState(pos.below());
        double fallen = fallenDistance();

        level.playSound(null, pos, LBSounds.DEBRIS_IMPACT.get(), SoundSource.BLOCKS,
                0.8F, 0.8F + this.random.nextFloat() * 0.3F);
        level.sendParticles(LBParticles.DEBRIS_CHIP.get(),
                getX(), getY(), getZ(), 8, 0.3D, 0.1D, 0.3D, 0.08D);
        level.sendParticles(LBParticles.CONCRETE_DUST.get(),
                getX(), getY(), getZ(), 12, 0.5D, 0.2D, 0.5D, 0.02D);

        if (breaksThrough(level, pos.below(), landedOn, fallen)) {
            com.loadbearing.collapse.CollapseManager.get().collapse(level, pos.below(), true);
            discard();
            return;
        }

        BlockState current = level.getBlockState(pos);
        boolean replaceable = current.canBeReplaced(
                new DirectionalPlaceContext(level, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        boolean survives = this.blockState.canSurvive(level, pos);
        boolean brittle = MaterialRegistry.brittle(this.blockState);

        if (replaceable && survives && !(brittle && fallen > 4.0D)) {
            BlockState placed = this.blockState;
            if (placed.hasProperty(BlockStateProperties.WATERLOGGED)
                    && level.getFluidState(pos).is(Fluids.WATER)) {
                placed = placed.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            if (level.setBlock(pos, placed, 3)) {
                com.loadbearing.solver.StructuralEventHandler.markPlaced(level, pos);
                com.loadbearing.solver.SolverScheduler.get().request(level, pos);
                discard();
                return;
            }
        }

        if (!this.shattered && level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            spawnAtLocation(level, this.blockState.getBlock());
        }
        discard();
    }

    private boolean breaksThrough(ServerLevel level, BlockPos below, BlockState target, double fallen) {
        if (target.isAir() || target.getDestroySpeed(level, below) < 0.0F) {
            return false;
        }
        MaterialProfile falling = MaterialRegistry.get(this.blockState);
        MaterialProfile struck = MaterialRegistry.get(target);
        double impact = falling.scaledWeight() * Math.max(1.0D, fallen);
        return impact > struck.scaledStrength();
    }

    private double fallenDistance() {
        return Math.max(0.0D, getStartPos().getY() - getY());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("BlockState", BlockState.CODEC, this.blockState);
        output.putInt("Age", this.age);
        output.putBoolean("Shattered", this.shattered);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.blockState = input.read("BlockState", BlockState.CODEC).orElse(DEFAULT_STATE);
        this.age = input.getIntOr("Age", 0);
        this.shattered = input.getBooleanOr("Shattered", false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity, Block.getId(this.blockState));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.blockState = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        setPos(packet.getX(), packet.getY(), packet.getZ());
        setStartPos(blockPosition());
    }

    public int tumbleTicks() {
        return Mth.clamp(this.age, 0, MAX_AGE);
    }
}
