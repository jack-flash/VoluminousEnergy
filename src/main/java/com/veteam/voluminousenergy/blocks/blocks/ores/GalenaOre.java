package com.veteam.voluminousenergy.blocks.blocks.ores;

import com.veteam.voluminousenergy.datagen.MaterialConstants;
import com.veteam.voluminousenergy.datagen.VETagDataGenerator;
import com.veteam.voluminousenergy.tools.Config;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

import java.util.Random;

public class GalenaOre extends VEOreBlock {
    public GalenaOre(){
        super(Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .lightLevel(l -> Config.GALENA_GLOW.get())
                .requiresCorrectToolForDrops()
                .strength(2.0f)
        );
        setRegistryName("galena_ore");
        VETagDataGenerator.setRequiresPickaxe(this);
        MaterialConstants.setGalenaTier(this);
    }

    @Override
    protected int xpOnDrop(Random rand) {
        return 0;
    }
}
