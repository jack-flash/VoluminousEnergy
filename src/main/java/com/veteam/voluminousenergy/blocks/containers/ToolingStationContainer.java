package com.veteam.voluminousenergy.blocks.containers;

import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.inventory.slots.VEBucketSlot;
import com.veteam.voluminousenergy.blocks.inventory.slots.VEInsertSlot;
import com.veteam.voluminousenergy.blocks.screens.ToolingStationScreen;
import com.veteam.voluminousenergy.items.tools.multitool.CombustionMultitool;
import com.veteam.voluminousenergy.items.tools.multitool.VEMultitools;
import com.veteam.voluminousenergy.items.tools.multitool.bits.BitItem;
import com.veteam.voluminousenergy.recipe.CombustionGenerator.CombustionGeneratorFuelRecipe;
import com.veteam.voluminousenergy.tools.energy.VEEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

import static com.veteam.voluminousenergy.blocks.blocks.VEBlocks.TOOLING_STATION_CONTAINER;

public class ToolingStationContainer extends VoluminousContainer {

    private static final int NUMBER_OF_SLOTS = 6;

    public ToolingStationContainer(int id, Level world, BlockPos pos, Inventory inventory, Player player) {
        super(TOOLING_STATION_CONTAINER, id);
        this.tileEntity = world.getBlockEntity(pos);
        this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(inventory);

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            addSlot(new VEBucketSlot(h, 0, 38, 18)); // Fluid input slot
            addSlot(new VEBucketSlot(h, 1, 38, 49)); // Extract fluid from input
            addSlot(new VEInsertSlot(h, 2, 86, 32)); // Main Tool slot
            addSlot(new VEInsertSlot(h, 3, 134, 18)); // Bit Slot
            addSlot(new VEInsertSlot(h, 4, 134,49)); // Base Slot
            addSlot(new VEInsertSlot(h, 5,154, -14)); // Upgrade Slot
        });
        layoutPlayerInventorySlots(8, 84);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getEnergy();
            }

            @Override
            public void set(int value) {
                tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> ((VEEnergyStorage) h).setEnergy(value));
            }
        });
    }

    public int getEnergy() {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int powerScreen(int px) {
        int stored = tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
        int max = tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
        int ret = (((stored * 100 / max * 100) / 100) * px) / 100;
        return ret;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()), playerEntity, VEBlocks.TOOLING_STATION_BLOCK);
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(final Player player, final int index) { // TODO: Rework for the Tooling Station
        ItemStack returnStack = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            final ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            if (handleCoreQuickMoveStackLogic(index, NUMBER_OF_SLOTS, slotStack) != null)
                return ItemStack.EMPTY;

            if (slotStack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return returnStack;
    }

    @Override
    public ItemStack handleCoreQuickMoveStackLogic(final int index, final int containerSlots, ItemStack slotStack){
        if (index < containerSlots) { // Container --> Inventory
            if (!moveItemStackTo(slotStack, containerSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else { // Inventory --> Container

            if (slotStack.getItem() instanceof CombustionMultitool){
                if (((CombustionMultitool) slotStack.getItem()).getBit() == null
                        || slotStack.getItem() == VEMultitools.EMPTY_MULTITOOL){ // Multitool Base Slot id is 4
                    if (!this.slots.get(2).hasItem() && !this.slots.get(4).hasItem() && !moveItemStackTo(slotStack, 4, 5, false)){
                        return ItemStack.EMPTY;
                    }
                } else if (!this.slots.get(3).hasItem()
                        && !this.slots.get(4).hasItem()
                        && !moveItemStackTo(slotStack, 2, 3, false)) { // Multitool slot id is 2
                    // Place the main machine in the main result slot
                    System.out.println("This.bit: " + ((CombustionMultitool) slotStack.getItem()).getBit());
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.getItem() instanceof BitItem){ // Bit Slot id is 3
                if (!this.slots.get(2).hasItem() && !this.slots.get(3).hasItem() && !moveItemStackTo(slotStack, 3, 4, false)){
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.getItem() instanceof BucketItem){
                if (slotStack.getItem().equals(Items.BUCKET)){
                    return !moveItemStackTo(slotStack, 0, 1, false) ? ItemStack.EMPTY : null;
                }

                try {
                    // Handle bucket with fluid
                    Fluid slotFluid = ((BucketItem) slotStack.getItem()).getFluid();

                    if (CombustionGeneratorFuelRecipe.rawFluidInputListStatic.contains(slotFluid) && !moveItemStackTo(slotStack, 0, 1, false)){
                        return ItemStack.EMPTY;
                    }
                } catch (Exception e){
                    VoluminousEnergy.LOGGER.error("Item: " + slotStack.getItem().getRegistryName() + " Appears to be a bucket, this error is likely caused by it not containing a fluid. " +
                            "This may be a modded bucket that extends BucketItem, but contains no fluid. If not, here's the stacktrace: ");
                    e.printStackTrace();
                }

            }

        }
        return null;
    }

    // Unauthorized call to this method can be dangerous. Can't not be public AFAIK. :(
    public void setScreen(ToolingStationScreen screen){
        this.screen = screen;
    }

    public void updateDirectionButton(int direction, int slotId){ this.screen.updateButtonDirection(direction,slotId); }

    @Override
    public void updateStatusButton(boolean status, int slotId){
        this.screen.updateBooleanButton(status, slotId);
    }

    public void updateStatusTank(boolean status, int id){
        this.screen.updateTankStatus(status, id);
    }

    public void updateDirectionTank(int direction, int id){
        this.screen.updateTankDirection(direction, id);
    }
}
