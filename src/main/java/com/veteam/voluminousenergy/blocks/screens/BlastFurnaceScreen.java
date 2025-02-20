package com.veteam.voluminousenergy.blocks.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.containers.BlastFurnaceContainer;
import com.veteam.voluminousenergy.blocks.tiles.BlastFurnaceTile;
import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.tools.VERender;
import com.veteam.voluminousenergy.tools.buttons.ioMenuButton;
import com.veteam.voluminousenergy.tools.buttons.slots.SlotBoolButton;
import com.veteam.voluminousenergy.tools.buttons.slots.SlotDirectionButton;
import com.veteam.voluminousenergy.tools.buttons.tanks.TankBoolButton;
import com.veteam.voluminousenergy.tools.buttons.tanks.TankDirectionButton;
import com.veteam.voluminousenergy.util.TextUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlastFurnaceScreen extends VEContainerScreen<BlastFurnaceContainer> {
    private BlastFurnaceTile tileEntity;
    private final ResourceLocation GUI = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/blast_furnace_gui.png");
    private static final ResourceLocation GUI_TOOLS = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/guitools.png");
    

    public BlastFurnaceScreen(BlastFurnaceContainer screenContainer, Inventory inv, Component titleIn){
        super(screenContainer,inv,titleIn);
        tileEntity = (BlastFurnaceTile) screenContainer.getTileEntity();
        screenContainer.setScreen(this);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);
        super.render(matrixStack,mouseX,mouseY,partialTicks);
        this.renderTooltip(matrixStack,mouseX,mouseY);
    }

    @Override
    protected void init(){
        super.init();

        // Buttons
        addRenderableWidget(new ioMenuButton(64 + (this.width/2), this.topPos +4, buttons ->{

        }));

        // Heat Tank Slot Top
        addRenderableWidget(new SlotBoolButton(tileEntity.heatTankItemTopManager, (this.width/2)-198, this.topPos, button->{
            // Do nothing
        }));

        addRenderableWidget(new SlotDirectionButton(tileEntity.heatTankItemTopManager, (this.width/2)-184, this.topPos, button ->{
            // Do nothing
        }));

        // Heat Tank Slot Bottom
        addRenderableWidget(new SlotBoolButton(tileEntity.heatTankItemBottomManager, (this.width/2)-198, this.topPos+20, button ->{
            // Do nothing
        }));

        addRenderableWidget(new SlotDirectionButton(tileEntity.heatTankItemBottomManager, (this.width/2)-184, this.topPos+20, button ->{
            // Do nothing
        }));

        // First Input Slot
        addRenderableWidget(new SlotBoolButton(tileEntity.firstInputSlotManager, (this.width/2)-198, this.topPos+40, button ->{
            // Do nothing
        }));

        addRenderableWidget(new SlotDirectionButton(tileEntity.firstInputSlotManager, (this.width/2)-184, this.topPos+40, button ->{
            // Do nothing
        }));

        // Second Input Slot
        addRenderableWidget(new SlotBoolButton(tileEntity.secondInputSlotManager, (this.width/2)-198, this.topPos+60, button ->{
            // Do nothing
        }));

        addRenderableWidget(new SlotDirectionButton(tileEntity.secondInputSlotManager, (this.width/2)-184, this.topPos+60, button ->{
            // Do nothing
        }));

        // Output Slot
        addRenderableWidget(new SlotBoolButton(tileEntity.outputSlotManager, (this.width/2)-198, this.topPos+80, button ->{
            // Do nothing
        }));

        addRenderableWidget(new SlotDirectionButton(tileEntity.outputSlotManager, (this.width/2)-184, this.topPos+80, button ->{
            // Do nothing
        }));

        // Heat Tank
        addRenderableWidget(new TankBoolButton(tileEntity.getHeatTank(), (this.width/2)-198, this.topPos+100, button ->{
            // Do nothing
        }));

        addRenderableWidget(new TankDirectionButton(tileEntity.getHeatTank(), (this.width/2)-184, this.topPos+100, button ->{
            // Do nothing
        }));
    }

    @Override
    protected void renderLabels(PoseStack matrixStack,int mouseX, int mouseY) {
        this.font.drawShadow(matrixStack, TextUtil.translateVEBlock("blast_furnace"), 8.0F, 6.0F, 16777215);

        //this.font.drawShadow(matrixStack,new TranslationTextComponent("container.inventory"), 8.0F, (float)(this.imageHeight - 96 + 2), 16777215);
        this.font.drawShadow(matrixStack, TextUtil.translateString("text.voluminousenergy.temperature").getString() + ": " +
                tileEntity.getTemperatureKelvin() + " K (" +
                tileEntity.getTemperatureCelsius() + " \u00B0C) ",  8.0F, (float)(this.imageHeight - 96 + 2), 16777215);

        this.font.drawShadow(matrixStack, tileEntity.getTemperatureFahrenheit() + " \u00B0F", 101.0F, (this.imageHeight - 103), 16777215);
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack,int mouseX, int mouseY) {
        if (isHovering(11, 16, 12, 49, mouseX, mouseY)){
            tileEntity.getEnergy().ifPresent((veEnergyStorage -> {
                renderTooltip(matrixStack, Component.nullToEmpty(
                        veEnergyStorage.getEnergyStored()
                                + " FE / " + Config.BLAST_FURNACE_MAX_POWER.get()
                                + " FE"
                ), mouseX, mouseY);
            }));
        }

        if (isHovering(61, 18, 12, 50, mouseX, mouseY)){ // Input Tank
            int amount = tileEntity.getFluidStackFromTank(0).getAmount();
            String name = tileEntity.getFluidStackFromTank(0).getTranslationKey();
            renderTooltip(matrixStack,TextUtil.tankTooltip(name, amount, tileEntity.getTankCapacity()), mouseX, mouseY);
        }

        super.renderTooltip(matrixStack,mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack,float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, this.GUI);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack,i, j, 0, 0, this.imageWidth, this.imageHeight);
        if(tileEntity != null && tileEntity.getMultiblockValidity()){
            int progress = tileEntity.progressCounterPX(9);
            int power = menu.powerScreen(49);

            /*Note for this.blit below:
                p_blit_1_ = starting x for blit on screen
                p_blit_2_ = starting y for blit on screen
                p_blit_3_ = starting x for blit to be stitched from in the file
                p_blit_4_ = starting y for blit to be stitched from in the file
                p_blit_5_ = width of the x for the blit to be drawn (make variable for progress illusion on the x)
                p_blit_6_ = width of the y for the blit to be drawn (make variable for progress illusion of the y)
             */
            this.blit(matrixStack,i+109, j+32, 176, 0, progress, 17);
            this.blit(matrixStack,i + 11, j + (16 + (49-power)), 176, 24 + (49-power), 12, power);

            VERender.renderGuiTank(tileEntity.getFluidStackFromTank(0),tileEntity.getTankCapacity(), i + 61, j + 18, 0, 12, 50);

            drawIOSideHelper();
            // Upgrade slot
            RenderSystem.setShaderTexture(0, GUI_TOOLS);
            this.blit(matrixStack,i+129, j-16,0,0,18,18);
        } else {
            this.font.drawShadow(matrixStack, TextUtil.translateString("text.voluminousenergy.multiblock_warn"), 8.0F, 6.0F, 16777215);
            this.font.drawShadow(matrixStack, TextUtil.translateString("text.voluminousenergy.multiblock.blast_furnace.requirements"), 8.0F, 16.0F, 16777215);
        }

    }

}