package com.veteam.voluminousenergy.blocks.tiles;

import com.veteam.voluminousenergy.items.VEItems;
import com.veteam.voluminousenergy.tools.energy.VEEnergyStorage;
import com.veteam.voluminousenergy.tools.sidemanager.VESlotManager;
import com.veteam.voluminousenergy.util.MultiFluidSlotWrapper;
import com.veteam.voluminousenergy.util.MultiSlotWrapper;
import com.veteam.voluminousenergy.util.RelationalTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class VoluminousTileEntity extends BlockEntity implements MenuProvider {

    public VoluminousTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Lazy Optional of getEnergy which cannot be null but can contain a null VEEnergyStorage
     * for the ifpresent to fail
     */
    LazyOptional<VEEnergyStorage> energy = LazyOptional.of(this::createEnergy);

    public static void serverTick(Level level, BlockPos pos, BlockState state, VoluminousTileEntity voluminousTile) {
        voluminousTile.tick();
    }

    int counter = 0;
    int length = 0;

    /**
     * Must include a call to updateClients();
     * This message can be removed if updateClients(); is found to be useless
     */
    public abstract void tick();

    /**
     * TODO figure out if this is actually useful or not. I imagine this is what allows the tile to update so perfectly.
     * TODO if this is true then move updateClients() to a potential base tick method or something if necessary.
     */
    public void updateClients() {
        if (level == null) return;
        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 1);
    }

    protected int calculateCounter(int processTime, ItemStack upgradeStack) {
        if (upgradeStack.getItem() == VEItems.QUARTZ_MULTIPLIER) {
            int count = upgradeStack.getCount();
            if (count == 4) {
                return 5;
            } else {
                return (-45 * upgradeStack.getCount()) + processTime;
            }
        }
        return processTime;
    }

    protected int consumptionMultiplier(int consumption, ItemStack upgradeStack) {
        if (upgradeStack.getItem() == VEItems.QUARTZ_MULTIPLIER) {
            int count = upgradeStack.getCount();
            if (count == 4) {
                return consumption * 16;
            } else if (count == 3) {
                return consumption * 8;
            } else if (count == 2) {
                return consumption * 4;
            } else if (count == 1) {
                return consumption * 2;
            }
        }
        return consumption;
    }


    /**
     * Quickly get the energy stored in the tile
     * @return int representing the stored energy of the tile entity
     */
    protected int getEnergyStored() {
        return this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    /**
     * TODO another method to check if it's need along with updatePacketFromGui();
     * @param status boolean status of the slot
     * @param slotId int id of the slot
     */
    public void updatePacketFromGui(boolean status, int slotId) {
        for (VESlotManager slot : getSlotManagers()) {
            if (slotId == slot.getSlotNum()) {
                slot.setStatus(status);
                return;
            }
        }
    }

    public void updatePacketFromGui(int direction, int slotId) {
        for (VESlotManager slot : getSlotManagers()) {
            if (slotId == slot.getSlotNum()) {
                slot.setDirection(direction);
                return;
            }
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        this.saveAdditional(compoundTag);
        return compoundTag;
    }

    /**
     *  Loads inventory, energy, slot managers, counter, and length.
     * @param tag CompoundTag
     */
    @Override
    public void load(CompoundTag tag) {
        CompoundTag inv = tag.getCompound("inv");

        ItemStackHandler handler = getInventoryHandler();

        if(handler != null) {
            handler.deserializeNBT(inv);
        }

        energy.ifPresent(h -> h.deserializeNBT(tag));

        if(this instanceof IVECountable) {
            counter = tag.getInt("counter");
            length = tag.getInt("length");
        }

        for(VESlotManager manager : getSlotManagers()) {
            manager.read(tag);
        }

        super.load(tag);
    }

    /**
     * Saves inventory, energy, slot managers, counter, and length.
     * @param tag CompoundTag
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        ItemStackHandler handler = getInventoryHandler();
        if(handler != null) {
            CompoundTag compound = ((INBTSerializable<CompoundTag>)handler).serializeNBT();
            tag.put("inv",compound);
        }
        energy.ifPresent(h -> h.serializeNBT(tag));

        for(VESlotManager manager : getSlotManagers()) {
            manager.write(tag);
        }

        if(this instanceof IVECountable) {
            tag.putInt("counter", counter);
            tag.putInt("length", length);
        }

    }

    /**
     * A default ItemStackHandler creator. Passing in an int size creates it for us
     * DO NOT USE THIS IF YOU REQUIRE EXTRA HANDLING FUNCTIONALITY!!!
     * TODO maybe add recipe functionality to this to allow for removal of recipe ItemStackHandlers
     * @param size the size of the inventory
     * @return a new inventory
     */
    public ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) { //IS ITEM VALID PLEASE DO THIS PER SLOT TO SAVE DEBUG HOURS!!!!
                return true;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    public ItemStackHandler createHandler(int size, IVEPoweredTileEntity tileEntity) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (tileEntity.getUpgradeSlotId() == slot){
                    return stack.getItem() == VEItems.QUARTZ_MULTIPLIER;
                }
                return true;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if(slot == tileEntity.getUpgradeSlotId()) {
                    return stack.getItem() == VEItems.QUARTZ_MULTIPLIER ? super.insertItem(slot, stack, simulate) : stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    /**
     * This handles items,energy and fluids. Handling fluids could be moved to VEFluidTileEntity
     * @param cap  Base capability
     * @param side Base Direction
     * @param <T>  T the type of capability
     * @return A LazyOptional of Optional of type T matching the capability passed into this
     */
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

        ItemStackHandler inventory = getInventoryHandler();
        List<VESlotManager> itemManagers = getSlotManagers();

        if (inventory != null && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) return LazyOptional.of(() -> inventory).cast();
            Direction modifiedSide = normalizeDirection(side);
            List<VESlotManager> managerList = itemManagers
                    .stream()
                    .filter(manager -> manager.getStatus()
                            && manager.getDirection().get3DDataValue() == modifiedSide.get3DDataValue())
                    .toList();
            if (managerList.size() == 0) return super.getCapability(cap, side);
            MultiSlotWrapper slotWrapper = new MultiSlotWrapper(inventory, managerList);
            return LazyOptional.of(() -> slotWrapper).cast();
        } else if (cap == CapabilityEnergy.ENERGY && energy.isPresent()) {
            return energy.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != null && this instanceof VEFluidTileEntity veFluidTileEntity) {
            Direction modifiedSide = normalizeDirection(side);
            List<RelationalTank> relationalTanks = veFluidTileEntity.getRelationalTanks().stream().filter(manager -> manager.getSideStatus() && manager.getSideDirection().get3DDataValue() == modifiedSide.get3DDataValue() || manager.isIgnoreDirection()).toList();
            if (relationalTanks.size() == 0) return super.getCapability(cap, side);
            MultiFluidSlotWrapper slotWrapper = new MultiFluidSlotWrapper(relationalTanks);
            return LazyOptional.of(() -> slotWrapper).cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    /**
     * @return a VEEnergyStorage object or null if this tile is not an instance of poweredTileEntity
     */
    public @Nullable VEEnergyStorage createEnergy() {
        if(this instanceof IVEPoweredTileEntity IVEPoweredTileEntity) {
            return new VEEnergyStorage(IVEPoweredTileEntity.getMaxPower(), IVEPoweredTileEntity.getTransferRate());
        }
        return null;
    }

    /**
     * Call this to consume energy
     * Note that tiles now require an upgrade slot and thus an inventory to properly function here
     * If you need to consume energy WITHOUT an upgrade slot make a new method that does not have this.
     * Throws an error if missing the power consumeEnergy IMPL
     */
    public void consumeEnergy() {
        if (this instanceof IVEPoweredTileEntity IVEPoweredTileEntity) {
            energy.ifPresent(e -> e
                    .consumeEnergy(
                            this.consumptionMultiplier(IVEPoweredTileEntity.getPowerUsage(),
                                    getInventoryHandler().getStackInSlot(IVEPoweredTileEntity.getUpgradeSlotId()).copy())));
        } else {
            throw new NotImplementedException("Missing implementation of IVEPoweredTileEntity in class: " + this.getClass().getName());
        }
    }

    /**
     * Like consumeEnergy this requires that the object has an inventory
     *
     * @return True if the object has enough energy to be able to continue
     * Throws an error if missing the power consumeEnergy IMPL
     */
    public boolean canConsumeEnergy() {
        if (this instanceof IVEPoweredTileEntity ivePoweredTileEntity) {
            return this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0)
                    > this.consumptionMultiplier(ivePoweredTileEntity.getPowerUsage(), getInventoryHandler().getStackInSlot(ivePoweredTileEntity.getUpgradeSlotId()).copy());
        } else {
            throw new NotImplementedException("Missing implementation of IVEPoweredTileEntity in class: " + this.getClass().getName());
        }
    }

    /**
     * Gets the display name and throws an NotImpl exception if missing registry
     * @return Name component
     */
    @Override
    public @Nonnull Component getDisplayName() {
        ResourceLocation name = getType().getRegistryName();
        if(name == null) throw new NotImplementedException("Missing registry name for class: " + this.getClass().getName());
        return new TextComponent(name.getPath());
    }

    /**
     * Make this null if the item does not have a menu
     * @param id the ID
     * @param playerInventory inventory of the player
     * @param player the player themselves
     * @return a new AbstractContainerMenu corresponding to this
     */
    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player);

    /**
     * We do a null check on inventory so this can be null. Might change though
     * REMEMBER YOU NEED TO BUILD YOUR OWN INVENTORY HANDLER
     * USE EITHER A NEWLY CREATED ONE ONE OF THE createHandler's defined here
     * @see #createHandler(int)
     * @see #createHandler(int, IVEPoweredTileEntity)
     * @return a ItemStackHandler or null if the object lacks an inventory
     */
    public abstract @Nullable
    ItemStackHandler getInventoryHandler();

    /**
     * Important note. If the entity has no slot managers return a new ArrayList otherwise this will crash
     *
     * @return A not null List<VESlotManager> list
     */
    public abstract @Nonnull List<VESlotManager> getSlotManagers();

    /**
     * When a data packet is received load it.
     * @param net Connection
     * @param pkt ClientboundBlockEntityDataPacket
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        energy.ifPresent(e -> e.setEnergy(pkt.getTag().getInt("energy")));
        this.load(pkt.getTag());
        super.onDataPacket(net, pkt);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Direction normalizeDirection(Direction direction) {
        Direction currentDirection = this.getBlockState().getValue(BlockStateProperties.FACING);
        int directionInt = direction.get3DDataValue();
        if (directionInt == 0 || directionInt == 1) return direction;
        Direction rotated = currentDirection;
        for (int i = 0; i < 4; i++) {
            rotated = rotated.getClockWise();
            direction = direction.getClockWise();
            if (rotated.get3DDataValue() == 2) break;
        }
        return direction.getClockWise().getClockWise();
    }

    public LazyOptional<VEEnergyStorage> getEnergy() {
        return energy;
    }

}
