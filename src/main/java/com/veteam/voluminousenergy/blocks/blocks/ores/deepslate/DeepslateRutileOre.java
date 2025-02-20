package com.veteam.voluminousenergy.blocks.blocks.ores.deepslate;

import com.veteam.voluminousenergy.blocks.blocks.ores.VEOreBlock;
import com.veteam.voluminousenergy.datagen.MaterialConstants;
import com.veteam.voluminousenergy.datagen.VETagDataGenerator;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

import java.util.Random;

public class DeepslateRutileOre extends VEOreBlock {
    public DeepslateRutileOre(){
        super(Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(4.0f)
                .requiresCorrectToolForDrops()
        );
        setRegistryName("deepslate_rutile_ore");
        VETagDataGenerator.setRequiresPickaxe(this);
        MaterialConstants.setRutileTier(this);
    }

    @Override
    protected int xpOnDrop(Random rand) {
        return 0;
    }
}