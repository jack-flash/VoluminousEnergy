package com.veteam.voluminousenergy.blocks.tiles;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.containers.ElectricFurnaceContainer;
import com.veteam.voluminousenergy.items.VEItems;
import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.tools.sidemanager.VESlotManager;
import com.veteam.voluminousenergy.util.SlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ElectricFurnaceTile extends VoluminousTileEntity implements IVEPoweredTileEntity,IVECountable {

    public VESlotManager inputSlotManager = new VESlotManager(0,Direction.UP,true,"slot.voluminousenergy.input_slot", SlotType.INPUT,"input_slot_manager");
    public VESlotManager outputSlotManager = new VESlotManager(1, Direction.DOWN,true,"slot.voluminousenergy.output_slot",SlotType.OUTPUT,"output_slot_manager");

    public List<VESlotManager> slotManagers = new ArrayList<>() {{
       add(inputSlotManager);
       add(outputSlotManager);
    }};

    private final AtomicReference<ItemStack> inputItemStack = new AtomicReference<ItemStack>(new ItemStack(Items.AIR,0));
    private final AtomicReference<ItemStack> referenceStack = new AtomicReference<ItemStack>(new ItemStack(Items.AIR,0));

    public ElectricFurnaceTile(BlockPos pos, BlockState state) {
        super(VEBlocks.ELECTRIC_FURNACE_TILE, pos, state);
    }

    @Deprecated
    public ElectricFurnaceTile(BlockEntityType<?> type, BlockPos pos, BlockState state){
        super(VEBlocks.ELECTRIC_FURNACE_TILE, pos, state);
    }

    public ItemStackHandler inventory = createHandler();

    @Override
    public void tick(){
        updateClients();

        ItemStack furnaceInput = inventory.getStackInSlot(0).copy();
        ItemStack furnaceOutput = inventory.getStackInSlot(1).copy();

        inputItemStack.set(furnaceInput.copy()); // Atomic Reference, use this to query recipes FOR OUTPUT SLOT

        // Main Processing occurs here
        if (canConsumeEnergy()){
            SmeltingRecipe furnaceRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(furnaceInput.copy()), level).orElse(null);
            BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(furnaceInput.copy()), level).orElse(null);

            if ((furnaceRecipe != null || blastingRecipe != null) && countChecker(furnaceRecipe,blastingRecipe,furnaceOutput.copy()) && itemChecker(furnaceRecipe,blastingRecipe,furnaceOutput.copy())){
                if (counter == 1) {
                    // Extract item
                    inventory.extractItem(0,1,false);

                    // Set output based on recipe
                    ItemStack newOutputStack;
                    if (furnaceRecipe != null) {
                        newOutputStack = furnaceRecipe.getResultItem().copy();
                    } else {
                        newOutputStack = blastingRecipe.getResultItem().copy();
                    }
                    //LOGGER.debug("NewOutputStack: " + newOutputStack);

                    // Output Item
                    if (furnaceOutput.getItem() != newOutputStack.getItem() || furnaceOutput.getItem() == Items.AIR) {
                        //LOGGER.debug("The output is not equal to the new output Stack");
                        if(furnaceOutput.getItem() == Items.AIR){ // Fix air >1 jamming slots
                            furnaceOutput.setCount(1);
                        }
                        if (furnaceRecipe != null){
                            newOutputStack.setCount(furnaceRecipe.getResultItem().getCount());
                        } else {
                            newOutputStack.setCount(blastingRecipe.getResultItem().getCount());
                        }
                        //LOGGER.debug("About to insert in pt1: " + newOutputStack);
                        inventory.insertItem(1, newOutputStack.copy(),false); // CRASH the game if this is not empty!

                    } else { // Assuming the recipe output item is already in the output slot
                        // Simply change the stack to equal the output amount
                        if (furnaceRecipe != null){
                            furnaceOutput.setCount(furnaceRecipe.getResultItem().getCount());
                        } else {
                            furnaceOutput.setCount(blastingRecipe.getResultItem().getCount());
                        }
                        //LOGGER.debug("About to insert in pt2: " + furnaceOutput);
                        inventory.insertItem(1, furnaceOutput.copy(),false); // Place the new output stack on top of the old one
                    }

                    consumeEnergy();
                    counter--;
                    this.setChanged();
                } else if (counter > 0) {
                    consumeEnergy();
                    counter--;
                } else {
                    counter = this.calculateCounter(200,inventory.getStackInSlot(2));
                    length = counter;
                    this.referenceStack.set(furnaceInput.copy());
                }

            } else counter = 0;
        } else counter = 0;
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) { //IS ITEM VALID PLEASE DO THIS PER SLOT TO SAVE DEBUG HOURS!!!!
                if (slot == 0) {
                    return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level).orElse(null) != null
                            || level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(stack), level).orElse(null) != null;
                } else if (slot == 1) {
                    SmeltingRecipe furnaceRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(inputItemStack.get()), level).orElse(null);
                    BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(inputItemStack.get()), level).orElse(null);

                    // If both recipes are null, then don't bother
                    if (blastingRecipe == null && furnaceRecipe == null) return false;

                    if (furnaceRecipe != null) {
                        return stack.getItem() == furnaceRecipe.getResultItem().getItem();
                    }

                    return stack.getItem() == blastingRecipe.getResultItem().getItem();
                } else if (slot == 2){
                    return stack.getItem() == VEItems.QUARTZ_MULTIPLIER;
                }
                return false;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){ //ALSO DO THIS PER SLOT BASIS TO SAVE DEBUG HOURS!!!
                if (slot == 0){
                    ItemStack referenceStack = stack.copy();
                    referenceStack.setCount(64);
                    SmeltingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(referenceStack), level).orElse(null);
                    BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(referenceStack),level).orElse(null);

                    if (recipe != null || blastingRecipe != null){
                        return super.insertItem(slot, stack, simulate);
                    }

                } else if (slot == 1){
                    return super.insertItem(slot, stack, simulate);
                } else if (slot == 2 && stack.getItem() == VEItems.QUARTZ_MULTIPLIER){
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            @Nonnull
            public ItemStack extractItem(int slot, int amount, boolean simulate){
                if (level != null){
                    SmeltingRecipe furnaceRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(referenceStack.get()), level).orElse(null);
                    BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(referenceStack.get()), level).orElse(null);
                    if(blastingRecipe != null) {
                        if (inventory.getStackInSlot(slot).getItem() == blastingRecipe.getResultItem().getItem()) {
                            if(blastingRecipe.getExperience() > 0){
                                generateXP(amount, blastingRecipe.getExperience());
                            }
                        }
                    } else if (furnaceRecipe != null) {
                        if (inventory.getStackInSlot(slot).getItem() == furnaceRecipe.getResultItem().getItem()) {
                            if (furnaceRecipe.getExperience() > 0) {
                                generateXP(amount, furnaceRecipe.getExperience());
                            }
                        }
                    }
                }
                return super.extractItem(slot,amount,simulate);
            }
        };
    }

    private void generateXP(int craftedAmount, float experience){
        if(level == null) return;
        int i = Mth.floor((float)craftedAmount * experience);
        float f = Mth.frac((float)craftedAmount * experience);
        if (f != 0.0F && Math.random() < (double)f) ++i;

        while(i > 0) {
            int j = ExperienceOrb.getExperienceValue(i);
            i -= j;
            level.addFreshEntity(new ExperienceOrb(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), j));
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity)
    {
        return new ElectricFurnaceContainer(i,level,worldPosition,playerInventory,playerEntity);
    }

    @Override
    public @Nonnull ItemStackHandler getInventoryHandler() {
        return inventory;
    }

    @NotNull
    @Override
    public List<VESlotManager> getSlotManagers() {
        return slotManagers;
    }

    public int progressCounterPX(int px) {
        if (counter != 0 && length != 0) return (px * (100 - ((counter * 100) / length))) / 100;
        return 0;
    }

    public int progressCounterPercent(){
        if (length != 0){
            return (int)(100-(((float)counter/(float)length)*100));
        } else {
            return 0;
        }
    }

    public boolean countChecker(SmeltingRecipe furnaceRecipe, BlastingRecipe blastingRecipe, ItemStack itemStack){
        if(furnaceRecipe != null){
            return (itemStack.getCount() + furnaceRecipe.getResultItem().getCount()) <= 64;
        } else if (blastingRecipe != null){
            return (itemStack.getCount() + blastingRecipe.getResultItem().getCount()) <= 64;
        }
        return false;
    }

    public boolean itemChecker(SmeltingRecipe furnaceRecipe, BlastingRecipe blastingRecipe, ItemStack itemStack){
        if(furnaceRecipe != null){
            if (itemStack.getItem() == Items.AIR || itemStack.isEmpty()) return true;
            return furnaceRecipe.getResultItem().getItem() == itemStack.getItem();
        } else if (blastingRecipe != null){
            if (itemStack.getItem() == Items.AIR || itemStack.isEmpty()) return true;
            return blastingRecipe.getResultItem().getItem() == itemStack.getItem();
        }
        return false;
    }

    public int getCounter(){
        return counter;
    }

    @Override
    public void updatePacketFromGui(boolean status, int slotId){
        if(slotId == inputSlotManager.getSlotNum()){
            inputSlotManager.setStatus(status);
        } else if (slotId == outputSlotManager.getSlotNum()){
            outputSlotManager.setStatus(status);
        }
    }

    public void updatePacketFromGui(int direction, int slotId){
        if(slotId == inputSlotManager.getSlotNum()){
            inputSlotManager.setDirection(direction);
        } else if (slotId == outputSlotManager.getSlotNum()){
            outputSlotManager.setDirection(direction);
        }
    }

    @Override
    public int getMaxPower() {
        return Config.ELECTRIC_FURNACE_MAX_POWER.get();
    }

    @Override
    public int getPowerUsage() {
        return Config.ELECTRIC_FURNACE_POWER_USAGE.get();
    }

    @Override
    public int getTransferRate() {
        return Config.ELECTRIC_FURNACE_TRANSFER.get();
    }

    @Override
    public int getUpgradeSlotId() {
        return 0;
    }
}