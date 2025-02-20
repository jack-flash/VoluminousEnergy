package com.veteam.voluminousenergy.items.crops;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.blocks.crops.VEWaterCrop;

import net.minecraft.world.item.Item.Properties;

public class RiceItem extends WaterCropItem {

    public RiceItem(Properties properties) {
        super(VEBlocks.RICE_CROP, properties);
        setRegistryName("rice_grain");
    }

    @Override
    public VEWaterCrop getWaterCrop(){
        return VEBlocks.RICE_CROP;
    }

}
