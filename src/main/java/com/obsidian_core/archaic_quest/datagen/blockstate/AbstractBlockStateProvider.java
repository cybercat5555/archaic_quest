package com.obsidian_core.archaic_quest.datagen.blockstate;

import com.obsidian_core.archaic_quest.common.block.VerticalSlabBlock;
import com.obsidian_core.archaic_quest.common.core.ArchaicQuest;
import com.obsidian_core.archaic_quest.common.register.AQBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import java.util.*;

public abstract class AbstractBlockStateProvider extends BlockStateProvider {

    public static final List<RegistryObject<Block>> SIMPLE_BLOCKS = new ArrayList<>();

    public AbstractBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ArchaicQuest.MODID, exFileHelper);
    }

    private String name(Block block) {
        return Objects.requireNonNull(block.getRegistryName()).getPath();
    }

    public void simpleBlockAndItem(Block block) {
        this.simpleBlock(block);
        this.simpleBlockItem(block, cubeAll(block));
    }

    public void topBottomCube(Block block, ResourceLocation sides, ResourceLocation topBottom) {
        ModelFile model = models().cubeBottomTop(resLoc(":block/" + name(block)).toString(), sides, topBottom, topBottom);

        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .build());

        simpleBlockItem(block, model);
    }

    public void verticalSlab(VerticalSlabBlock block, Block doubleBlock) {
        verticalSlab(block, doubleBlock, blockTexture(doubleBlock));
    }

    public void verticalSlab(VerticalSlabBlock block, Block doubleBlock, ResourceLocation texture) {
        verticalSlab(block, doubleBlock, texture, texture, texture);
    }

    public void verticalSlab(VerticalSlabBlock block, Block doubleBlock, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        ModelFile model = models().withExistingParent(name(block), resLoc(":block/vertical_slab").toString())
                .texture("side", side)
                .texture("bottom", bottom)
                .texture("top", top);

        verticalSlab(block, model, cubeAll(doubleBlock));
        simpleBlockItem(block, model);
    }

    public void verticalSlab(VerticalSlabBlock block, ModelFile model, ModelFile doubleSlab) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            VerticalSlabBlock.SlabState slabState = state.getValue(VerticalSlabBlock.SLAB_STATE);

            if (slabState == VerticalSlabBlock.SlabState.DOUBLE) {
                return ConfiguredModel.builder()
                        .modelFile(doubleSlab)
                        .build();
            }
            Direction facing = slabState.getDirection();
            int yRot = (int) facing.getOpposite().toYRot();

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(yRot)
                    .uvLock(true)
                    .build();
            }, VerticalSlabBlock.WATERLOGGED);
    }

    public static ResourceLocation resLoc(String path) {
        return ArchaicQuest.resourceLoc(path);
    }

    public static ResourceLocation texture(String textureName) {
        return resLoc("textures/block/" + textureName + ".png");
    }
}
