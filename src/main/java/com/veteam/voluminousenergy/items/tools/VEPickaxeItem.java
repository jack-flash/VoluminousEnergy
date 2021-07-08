package com.veteam.voluminousenergy.items.tools;

import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.util.ToolUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class VEPickaxeItem extends PickaxeItem {
    public VEPickaxeItem(IItemTier p_i48478_1_, int p_i48478_2_, float p_i48478_3_, Properties p_i48478_4_) {
        super(p_i48478_1_, p_i48478_2_, p_i48478_3_, p_i48478_4_);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag){
        ToolUtil.SolariumTooltipAppend(stack, tooltip);
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public void setDamage(ItemStack stack, int damage){
        CompoundNBT tag = stack.getTag();

        if (tag == null){
            stack.getOrCreateTag().putInt("bonus", Config.SOLARIUM_PROTECTIVE_SHEATH_HITS.get());
            return;
        }

        if (tag.getInt("bonus") > 0){
            int bonus = tag.getInt("bonus");
            if (bonus >= damage){
                stack.getOrCreateTag().putInt("bonus", (bonus-damage));
            } else {
                int difference = damage - bonus;
                stack.getOrCreateTag().putInt("bonus", difference);
                super.setDamage(stack, difference);
            }
        } else {
            super.setDamage(stack, damage);
        }
    }
}
