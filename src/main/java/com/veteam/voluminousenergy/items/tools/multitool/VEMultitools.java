package com.veteam.voluminousenergy.items.tools.multitool;

import com.veteam.voluminousenergy.items.tools.multitool.bits.BitItem;
import com.veteam.voluminousenergy.items.tools.multitool.bits.VEMultitoolBits;
import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.world.item.Item;

public class VEMultitools {

    public static Item.Properties MULTITOOL_PROPERTIES = new Item.Properties().tab(VESetup.itemGroup).stacksTo(1);

    // Multitools
    public static Multitool EMPTY_MULTITOOL = new Multitool(null, "empty_multitool", MULTITOOL_PROPERTIES);
    public static Multitool IRON_DRILL_MULTITOOL = new Multitool(VEMultitoolBits.IRON_DRILL_BIT, "iron_drill_multitool", MULTITOOL_PROPERTIES);
    public static Multitool DIAMOND_DRILL_MULTITOOL = new Multitool(VEMultitoolBits.DIAMOND_DRILL_BIT, "diamond_drill_multitool", MULTITOOL_PROPERTIES);
    public static Multitool IRON_CHAIN_MULTITOOL = new Multitool(VEMultitoolBits.IRON_CHAIN_BIT, "iron_chain_multitool", MULTITOOL_PROPERTIES);
    public static Multitool IRON_SCOOPER_MULTITOOL = new Multitool(VEMultitoolBits.IRON_SCOOPER_BIT, "iron_scooper_multitool", MULTITOOL_PROPERTIES);
    public static Multitool IRON_TRIMMER_MULTITOOL = new Multitool(VEMultitoolBits.IRON_TRIMMER_BIT, "iron_trimmer_multitool", MULTITOOL_PROPERTIES);

    public static BitItem IRON_DRILL_BIT = new BitItem(VEMultitoolBits.IRON_DRILL_BIT, "iron_drill_bit", MULTITOOL_PROPERTIES);
    public static BitItem DIAMOND_DRILL_BIT = new BitItem(VEMultitoolBits.DIAMOND_DRILL_BIT, "diamond_drill_bit", MULTITOOL_PROPERTIES);
    public static BitItem IRON_CHAIN_BIT = new BitItem(VEMultitoolBits.IRON_CHAIN_BIT,"iron_chain_bit", MULTITOOL_PROPERTIES);
    public static BitItem IRON_SCOOPER_BIT = new BitItem(VEMultitoolBits.IRON_SCOOPER_BIT, "iron_scooper_bit",MULTITOOL_PROPERTIES);
    public static BitItem IRON_TRIMMER_BIT = new BitItem(VEMultitoolBits.IRON_TRIMMER_BIT, "iron_trimmer_bit", MULTITOOL_PROPERTIES);
}