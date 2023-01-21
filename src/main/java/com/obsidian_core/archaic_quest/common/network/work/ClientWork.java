package com.obsidian_core.archaic_quest.common.network.work;

import com.obsidian_core.archaic_quest.common.network.message.S2CUpdateDoorState;
import com.obsidian_core.archaic_quest.common.tile.AztecDungeonDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.obsidian_core.archaic_quest.common.tile.AztecDungeonDoorBlockEntity.DoorState;

public class ClientWork {

    public static void handleUpdateDoorState(S2CUpdateDoorState message) {
        BlockPos pos = message.doorPos;
        DoorState doorState = DoorState.byId(message.doorState);
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) return;

        BlockEntity blockEntity = level.getExistingBlockEntity(pos);

        if (blockEntity instanceof AztecDungeonDoorBlockEntity dungeonDoor) {
            if (doorState == null) return;
            dungeonDoor.setDoorState(doorState);
        }
    }
}
