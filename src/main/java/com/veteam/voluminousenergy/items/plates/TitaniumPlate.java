package com.veteam.voluminousenergy.items.plates;

import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.world.item.Item;

public class TitaniumPlate extends Item {
    public TitaniumPlate (){
        super(new Item.Properties()
                .stacksTo(64)
                .tab(VESetup.itemGroup)
        );
        setRegistryName("titanium_plate");
    }
}