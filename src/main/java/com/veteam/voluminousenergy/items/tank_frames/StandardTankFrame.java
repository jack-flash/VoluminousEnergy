package com.veteam.voluminousenergy.items.tank_frames;

import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.world.item.Item;

public class StandardTankFrame extends Item {
    public StandardTankFrame() {
        super(new Item.Properties()
                .stacksTo(64)
                .tab(VESetup.itemGroup)
        );
        setRegistryName("standard_tank_frame");
    }
}
