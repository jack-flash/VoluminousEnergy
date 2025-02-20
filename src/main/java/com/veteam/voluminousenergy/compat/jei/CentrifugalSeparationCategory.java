package com.veteam.voluminousenergy.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.recipe.CentrifugalSeparatorRecipe;
import com.veteam.voluminousenergy.util.TextUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CentrifugalSeparationCategory implements IRecipeCategory<CentrifugalSeparatorRecipe> {
    private final IDrawable background;
    private IDrawable icon;
    private IDrawable slotDrawable;
    private IDrawable arrow;
    private IDrawable emptyArrow;

    public CentrifugalSeparationCategory(IGuiHelper guiHelper){
        // 68, 12 | 40, 65 -> 10 px added for chance
        ResourceLocation GUI = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/jei/jei.png");
        background = guiHelper.drawableBuilder(GUI, 52, 5, 120, 78).build();
        icon = guiHelper.createDrawableIngredient(new ItemStack(VEBlocks.CENTRIFUGAL_SEPARATOR_BLOCK));
        slotDrawable = guiHelper.getSlotDrawable();
        arrow = guiHelper.drawableBuilder(GUI, 176, 0, 23, 17).build();
        emptyArrow = guiHelper.drawableBuilder(GUI,199,0,23,17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, true);

    }

    @Override
    public ResourceLocation getUid(){
        return VoluminousEnergyPlugin.CENTRIFUGAL_SEPARATION_UID;
    }

    @Override
    public Class<? extends CentrifugalSeparatorRecipe> getRecipeClass() {
        return CentrifugalSeparatorRecipe.class;
    }

    @Override
    public Component getTitle() {
        return TextUtil.translateString("jei.voluminousenergy.centrifugal_separation");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(CentrifugalSeparatorRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        arrow.draw(matrixStack,25, 30);
        emptyArrow.draw(matrixStack,25,30);
        slotDrawable.draw(matrixStack,5,20); // Input
        slotDrawable.draw(matrixStack,5,38); // Bucket
        slotDrawable.draw(matrixStack,49,2); // First Output
        slotDrawable.draw(matrixStack,49,20); // First RNG
        slotDrawable.draw(matrixStack,49,38); // Second RNG
        slotDrawable.draw(matrixStack,49,56); // Third RNG

        if (recipe.getRngItemSlot0() != null && recipe.getRngItemSlot0().getItem() != Items.AIR){
            int chance = (int)(recipe.getChance0()*100);
            Minecraft.getInstance().font.draw(matrixStack,chance + "%",74,26,0x606060);
        }

        if (recipe.getRngItemSlot1() != null && recipe.getRngItemSlot1().getItem() != Items.AIR){
            int chance = (int)(recipe.getChance1()*100);
            Minecraft.getInstance().font.draw(matrixStack,chance + "%",74,44,0x606060);
        }

        if (recipe.getRngItemSlot2() != null && recipe.getRngItemSlot2().getItem() != Items.AIR){
            int chance = (int)(recipe.getChance2()*100);
            Minecraft.getInstance().font.draw(matrixStack,chance + "%",74,62,0x606060);
        }

    }

    @Override
    public void setIngredients(CentrifugalSeparatorRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, recipe.getIngredientMap().keySet().stream()
                .map(ingredient -> Arrays.asList(ingredient.getItems()))
                .collect(Collectors.toList()));


        // STACK needs to be 64 for recipes that require more than 1 of the input item
        // This for loop ensures that every input can be right clicked, maybe it can just fetch the current ingredient
        // to save CPU cycles... but this works.
        for (ItemStack testStack : recipe.getIngredient().getItems()){
            testStack.setCount(64);
            ingredients.setInput(VanillaTypes.ITEM, testStack);
        }

        // OUTPUT
        List<ItemStack> outputStacks = new ArrayList<>();
        outputStacks.add(recipe.getResultItem()); // Normal output

        if (recipe.getRngItemSlot0() != null && recipe.getRngItemSlot0().getItem() != Items.AIR){ // Check RNG 0 if it's not air
            outputStacks.add(recipe.getRngItemSlot0());
        }

        if (recipe.getRngItemSlot1() != null && recipe.getRngItemSlot1().getItem() != Items.AIR){ // Check RNG 1 if it's not air
            outputStacks.add(recipe.getRngItemSlot1());
        }

        if (recipe.getRngItemSlot2() != null && recipe.getRngItemSlot2().getItem() != Items.AIR){ // Check RNG 2 if it's not air
            outputStacks.add(recipe.getRngItemSlot2());
        }

        ingredients.setOutputs(VanillaTypes.ITEM, outputStacks);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CentrifugalSeparatorRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 5, 20);
        itemStacks.init(1, false, 49, 2);

        // Should only be one ingredient...
        List<ItemStack> inputs = new ArrayList<>();
        Arrays.stream(recipe.getIngredient().getItems()).map(s -> {
            ItemStack stack = s.copy();
            stack.setCount(recipe.getIngredientCount());
            return stack;
        }).forEach(inputs::add);
        itemStacks.set(0, inputs);

        // Calculate output
        ItemStack tempStack = recipe.getResultItem(); // Get Item since amount will be wrong
        Item outputItem = tempStack.getItem();
        ItemStack jeiStack = new ItemStack(outputItem, recipe.getOutputAmount()); // Create new stack for JEI with correct amount
        itemStacks.set(1, jeiStack);

        // Calculate RNG stack, only if RNG stack exists
        if (recipe.getRngItemSlot0() != null && recipe.getRngItemSlot0().getItem() != Items.AIR){ // Don't create the slot if the slot will be empty!
            itemStacks.init(2, true, 49, 20);
            tempStack = recipe.getRngItemSlot0();
            Item rngItem = tempStack.getItem();
            ItemStack rngStack = new ItemStack(rngItem, recipe.getOutputRngAmount0());
            itemStacks.set(2, rngStack);
        }

        if (recipe.getRngItemSlot1() != null && recipe.getRngItemSlot1().getItem() != Items.AIR){ // Don't create the slot if the slot will be empty!
            itemStacks.init(3, true, 49, 38);
            tempStack = recipe.getRngItemSlot1();
            Item rngItem = tempStack.getItem();
            ItemStack rngStack = new ItemStack(rngItem, recipe.getOutputRngAmount1());
            itemStacks.set(3, rngStack);
        }

        if (recipe.getRngItemSlot2() != null && recipe.getRngItemSlot2().getItem() != Items.AIR){ // Don't create the slot if the slot will be empty!
            itemStacks.init(4, true, 49, 56);
            tempStack = recipe.getRngItemSlot2();
            Item rngItem = tempStack.getItem();
            ItemStack rngStack = new ItemStack(rngItem, recipe.getOutputRngAmount2());
            itemStacks.set(4, rngStack);
        }

        if (recipe.needsBuckets() > 0){
            itemStacks.init(5, true, 5, 38);
            itemStacks.set(5, new ItemStack(Items.BUCKET, recipe.needsBuckets()));
        }
    }
}