package com.veteam.voluminousenergy.blocks.tiles.tank;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.containers.tank.NetheriteTankContainer;
import com.veteam.voluminousenergy.tools.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NetheriteTankTile extends TankTile{

    public NetheriteTankTile(BlockPos pos, BlockState state) {
        super(VEBlocks.NETHERITE_TANK_TILE, pos, state, Config.NETHERITE_TANK_CAPACITY.get());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
        return new NetheriteTankContainer(i,level,this.worldPosition,playerInventory,player);
    }
}