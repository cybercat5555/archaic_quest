package com.obsidian_core.archaic_quest.common.blockentity;

import com.obsidian_core.archaic_quest.common.block.AztecDungeonDoorBlock;
import com.obsidian_core.archaic_quest.common.core.register.AQBlockEntities;
import com.obsidian_core.archaic_quest.common.misc.AQDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SpikeTrapBlockEntity extends BlockEntity {

    /** The collision box above the trap to check for players to damage, when trap is active. */
    private AABB effectBox = null;
    private boolean active;

    private final int maxSpikeRise = 3;
    private int spikeRise = 0;


    public SpikeTrapBlockEntity(BlockPos pos, BlockState state) {
        super(AQBlockEntities.SPIKE_TRAP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpikeTrapBlockEntity trap) {
        // Tick damage
        if (trap.active) {
            // Rise the spikes
            if (trap.spikeRise < trap.maxSpikeRise)
                ++trap.spikeRise;

            // Create collision box if needed
            if (trap.effectBox == null) {
                trap.effectBox = new AABB(pos.above()).inflate(0.0D, 0.75D, 0.0D);
            }
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, trap.effectBox);

            // Affect/damage entities inside the spikes
            for (LivingEntity entity : entities) {
                entity.hurt(AQDamageSources.SPIKE_TRAP, 2.0F);
                entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05F, 0.25D));
            }
        }
        else {
            // Remove collision box if needed
            if (trap.effectBox != null)
                trap.effectBox = null;

            // Lower the spikes
            if (trap.spikeRise > 0)
                --trap.spikeRise;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public int getSpikeRise() {
        return spikeRise;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        if (compoundTag.contains("Active", Tag.TAG_BYTE)) {
            active = compoundTag.getBoolean("Active");
        }

        if (compoundTag.contains("SpikeRise", Tag.TAG_ANY_NUMERIC)) {
            spikeRise = Mth.clamp(compoundTag.getInt("SpikeRise"), 0, maxSpikeRise);
        }
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        compoundTag.putBoolean("Active", active);
        compoundTag.putInt("SpikeRise", spikeRise);
    }

    private void writeUpdateData(CompoundTag compoundTag) {
        compoundTag.putBoolean("Active", active);
        compoundTag.putInt("SpikeRise", spikeRise);
    }


    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        writeUpdateData(compoundTag);
        return compoundTag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains("Active", Tag.TAG_BYTE)) {
            active = tag.getBoolean("Active");
        }
        if (tag.contains("SpikeRise", Tag.TAG_ANY_NUMERIC)) {
            spikeRise = Mth.clamp(tag.getInt("SpikeRise"), 0, maxSpikeRise);
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if(level.isClientSide) {
            if (pkt.getTag() != null)
                this.handleUpdateTag(pkt.getTag());
        }
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return getBlockState().getBlock() instanceof AztecDungeonDoorBlock
                ? new AABB(pos.offset(0, 0, 0), pos.offset(0, 2, 0))
                : INFINITE_EXTENT_AABB;
    }
}
