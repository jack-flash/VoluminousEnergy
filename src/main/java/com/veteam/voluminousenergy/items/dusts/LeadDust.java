package com.veteam.voluminousenergy.items.dusts;

import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.item.Item;

public class LeadDust extends Item {
    public LeadDust(){
        super(new Item.Properties()
                .maxStackSize(64)
                .group(VESetup.itemGroup));
        setRegistryName("lead_dust");
    }
}
