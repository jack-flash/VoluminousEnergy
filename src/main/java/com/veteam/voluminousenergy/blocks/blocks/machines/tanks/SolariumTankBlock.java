package com.veteam.voluminousenergy.blocks.blocks.machines.tanks;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.tiles.tank.SolariumTankTile;
import com.veteam.voluminousenergy.blocks.tiles.tank.TankTile;
import com.veteam.voluminousenergy.datagen.VETagDataGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;

public class SolariumTankBlock extends TankBlock implements EntityBlock {

    public SolariumTankBlock(){
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(20.0f)
                .lightLevel(l -> 0)
                .requiresCorrectToolForDrops()
        );
        setRegistryName("solarium_tank");
        VETagDataGenerator.setRequiresPickaxe(this);
        VETagDataGenerator.setRequiresSolarium(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolariumTankTile(pos, state);
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> passedBlockEntity, BlockEntityType<? extends SolariumTankTile> tile) {
        return level.isClientSide ? null : createTickerHelper(passedBlockEntity, tile, TankTile::serverTick);
    }

    public static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> createTickerHelper(BlockEntityType<T> blockEntityType, BlockEntityType<? extends SolariumTankTile> tile, BlockEntityTicker<E> serverTick) {
        return blockEntityType == tile ? (BlockEntityTicker<T>)serverTick : null;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTicker(level, blockEntityType, VEBlocks.SOLARIUM_TANK_TILE);
    }

}