package com.veteam.voluminousenergy.blocks.tiles;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.containers.CentrifugalAgitatorContainer;
import com.veteam.voluminousenergy.recipe.CentrifugalAgitatorRecipe;
import com.veteam.voluminousenergy.recipe.VEFluidRecipe;
import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.tools.VEEnergyStorage;
import com.veteam.voluminousenergy.util.RelationalTank;
import com.veteam.voluminousenergy.util.TankType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CentrifugalAgitatorTile extends VEFluidTileEntity {

    private LazyOptional<ItemStackHandler> handler = LazyOptional.of(() -> this.inventory);

    private LazyOptional<IEnergyStorage> energy = LazyOptional.of(this::createEnergy);
    private LazyOptional<IFluidHandler> inputFluidHandler = LazyOptional.of(this::createInputTankFluidHandler);
    private LazyOptional<IFluidHandler> output0FluidHandler = LazyOptional.of(this::createOutputTank0FluidHandler);
    private LazyOptional<IFluidHandler> output1FluidHandler = LazyOptional.of(this::createOutputTank1FluidHandler);

    RelationalTank inputTank = new RelationalTank(new FluidTank(TANK_CAPACITY),0,null,null, TankType.INPUT);
    RelationalTank outputTank0 = new RelationalTank(new FluidTank(TANK_CAPACITY),1,null,null, TankType.INPUT,0);
    RelationalTank outputTank1 = new RelationalTank(new FluidTank(TANK_CAPACITY),2,null,null, TankType.INPUT,1);

    private int counter;
    private int length;


    private static final Logger LOGGER = LogManager.getLogger();


    public CentrifugalAgitatorTile() {
        super(VEBlocks.CENTRIFUGAL_AGITATOR_TILE);
    }

    public final ItemStackHandler inventory = createHandler();

    @Override
    public ItemStackHandler getItemStackHandler() {
        return inventory;
    }

    @Override
    public void tick() {
        updateClients();
        ItemStack input = inventory.getStackInSlot(0).copy();
        ItemStack input1 = inventory.getStackInSlot(1).copy();
        ItemStack output0 = inventory.getStackInSlot(2).copy();
        ItemStack output1 = inventory.getStackInSlot(3).copy();

        inputTank.setInput(input.copy());
        inputTank.setOutput(input1.copy());

        outputTank0.setOutput(output0);
        outputTank1.setOutput(output1);

        if(this.inputFluid(inputTank,0,1)) return;
        if(this.outputFluid(inputTank,0,1)) return;
        if(this.outputFluidStatic(outputTank0,2)) return;
        if(this.outputFluidStatic(outputTank1,3)) return;
        // Main Fluid Processing occurs here
        if (inputTank != null) {
            ItemStack inputFluidStack = new ItemStack(inputTank.getTank().getFluid().getRawFluid().getFilledBucket(), 1);
            VEFluidRecipe recipe = world.getRecipeManager().getRecipe(CentrifugalAgitatorRecipe.RECIPE_TYPE, new Inventory(inputFluidStack), world).orElse(null);
            if (recipe != null) {
                if (outputTank0 != null && outputTank1 != null) {

                    // Tank fluid amount check + tank cap checks
                    if (inputTank.getTank().getFluidAmount() >= recipe.getInputAmount()
                            && outputTank0.getTank().getFluidAmount() + recipe.getOutputAmount() <= TANK_CAPACITY
                            && outputTank1.getTank().getFluidAmount() + recipe.getFluids().get(1).getAmount() <= TANK_CAPACITY) {
                        // Check for power
                        if (this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0) > 0) {
                            if (counter == 1) {

                                // Drain Input
                                inputTank.getTank().drain(recipe.getInputAmount(), IFluidHandler.FluidAction.EXECUTE);

                                // First Output Tank
                                if (outputTank0.getTank().getFluid().getRawFluid() != recipe.getOutputFluid().getRawFluid()) {
                                    outputTank0.getTank().setFluid(recipe.getOutputFluid());
                                } else {
                                    outputTank0.getTank().fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
                                }

                                // Second Output Tank
                                if (outputTank1.getTank().getFluid().getRawFluid() != recipe.getFluids().get(1).getRawFluid()) {
                                    outputTank1.getTank().setFluid(recipe.getFluids().get(1));
                                } else {
                                    outputTank1.getTank().fill(recipe.getFluids().get(1), IFluidHandler.FluidAction.EXECUTE);
                                }

                                counter--;
                                energy.ifPresent(e -> ((VEEnergyStorage) e).consumeEnergy(Config.CENTRIFUGAL_AGITATOR_POWER_USAGE.get()));
                                this.markDirty();
                            } else if (counter > 0) {
                                counter--;
                                energy.ifPresent(e -> ((VEEnergyStorage) e).consumeEnergy(Config.CENTRIFUGAL_AGITATOR_POWER_USAGE.get()));
                            } else {
                                counter = recipe.getProcessTime();
                                length = counter;
                            }
                        } // Energy Check
                    } else { // If fluid tank empty set counter to zero
                        counter = 0;
                    }
                }
            }
        }
    }

    /*
        Read and Write on World save
     */

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        CompoundNBT inv = tag.getCompound("inv");
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(inv));
        createHandler().deserializeNBT(inv);
        CompoundNBT energyTag = tag.getCompound("energy");
        energy.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(energyTag));

        // Tanks
        CompoundNBT inputTank = tag.getCompound("inputTank");
        CompoundNBT outputTank0 = tag.getCompound("outputTank0");
        CompoundNBT outputTank1 = tag.getCompound("outputTank1");

        this.inputTank.getTank().readFromNBT(inputTank);
        this.outputTank0.getTank().readFromNBT(outputTank0);
        this.outputTank1.getTank().readFromNBT(outputTank1);

        this.inputTank.readGuiProperties(tag,"input_tank_gui");
        this.outputTank0.readGuiProperties(tag, "output_tank_0_gui");
        this.outputTank1.readGuiProperties(tag, "output_tank_1_gui");

        super.read(state, tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });
        energy.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("energy", compound);
        });

        // Tanks
        CompoundNBT inputNBT = new CompoundNBT();
        CompoundNBT outputNBT0 = new CompoundNBT();
        CompoundNBT outputNBT1 = new CompoundNBT();

        this.inputTank.getTank().writeToNBT(inputNBT);
        this.outputTank0.getTank().writeToNBT(outputNBT0);
        this.outputTank1.getTank().writeToNBT(outputNBT1);

        tag.put("inputTank", inputNBT);
        tag.put("outputTank0", outputNBT0);
        tag.put("outputTank1", outputNBT1);

        this.inputTank.writeGuiProperties(tag, "input_tank_gui");
        this.outputTank0.writeGuiProperties(tag, "output_tank_0_gui");
        this.outputTank1.writeGuiProperties(tag, "output_tank_1_gui");

        return super.write(tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }

    private IFluidHandler createInputTankFluidHandler() {
        return createFluidHandler(new CentrifugalAgitatorRecipe(), inputTank);
    }

    private IFluidHandler createOutputTank0FluidHandler(){
        return createFluidHandler(new CentrifugalAgitatorRecipe(), outputTank0);
    }

    private IFluidHandler createOutputTank1FluidHandler(){
        return createFluidHandler(new CentrifugalAgitatorRecipe(), outputTank1);
    }


    private ItemStackHandler createHandler() {
        return new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) { //IS ITEM VALID PLEASE DO THIS PER SLOT TO SAVE DEBUG HOURS!!!!
                return true;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) { //ALSO DO THIS PER SLOT BASIS TO SAVE DEBUG HOURS!!!
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private IEnergyStorage createEnergy() {
        return new VEEnergyStorage(Config.CENTRIFUGAL_AGITATOR_MAX_POWER.get(), Config.CENTRIFUGAL_AGITATOR_TRANSFER.get()); // Max Power Storage, Max transfer
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energy.cast();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            if(inputTank.getSideStatus() && inputTank.getSideDirection().getIndex() == side.getIndex()){
                return inputFluidHandler.cast();
            } else if (outputTank0.getSideStatus() && outputTank0.getSideDirection().getIndex() == side.getIndex()){
                return output0FluidHandler.cast();
            } else if (outputTank1.getSideStatus() && outputTank1.getSideDirection().getIndex() == side.getIndex()){
                return output1FluidHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
        return new CentrifugalAgitatorContainer(i, world, pos, playerInventory, playerEntity);
    }

    public int progressCounterPX(int px) {
        if (counter == 0) {
            return 0;
        } else {
            return (px * (100 - ((counter * 100) / length))) / 100;
        }
    }

    public FluidStack getFluidStackFromTank(int num){
        if (num == 0){
            return inputTank.getTank().getFluid();
        } else if (num == 1){
            return outputTank0.getTank().getFluid();
        } else if (num == 2){
            return outputTank1.getTank().getFluid();
        }
        return FluidStack.EMPTY;
    }

    public int getTankCapacity(){
        return TANK_CAPACITY;
    }
}