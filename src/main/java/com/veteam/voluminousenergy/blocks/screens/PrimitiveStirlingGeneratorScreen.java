package com.veteam.voluminousenergy.blocks.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.containers.PrimitiveStirlingGeneratorContainer;
import com.veteam.voluminousenergy.blocks.tiles.PrimitiveStirlingGeneratorTile;
import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.tools.buttons.ioMenuButton;
import com.veteam.voluminousenergy.tools.buttons.slots.SlotBoolButton;
import com.veteam.voluminousenergy.tools.buttons.slots.SlotDirectionButton;
import com.veteam.voluminousenergy.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PrimitiveStirlingGeneratorScreen extends VEContainerScreen<PrimitiveStirlingGeneratorContainer> {

    private final ResourceLocation GUI = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/primitivestirlinggenerator_gui.png");
    private static final ResourceLocation GUI_TOOLS = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/guitools.png");
    private PrimitiveStirlingGeneratorTile tileEntity;
    

    public PrimitiveStirlingGeneratorScreen(PrimitiveStirlingGeneratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        tileEntity = (PrimitiveStirlingGeneratorTile) container.getTileEntity();
        container.setScreen(this);
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

        // Input slot
        addRenderableWidget(new SlotBoolButton(tileEntity.slotManager, (this.width/2)-198, this.topPos, button->{
            // Do nothing
        }));

        addRenderableWidget(new SlotDirectionButton(tileEntity.slotManager, (this.width/2)-184, this.topPos, button ->{
            // Do nothing
        }));
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack,int mouseX, int mouseY) {
        if (isHovering(11, 16, 12, 49, mouseX, mouseY)) {
            tileEntity.getEnergy().ifPresent((veEnergyStorage -> {
                renderTooltip(matrixStack, Component.nullToEmpty(
                        veEnergyStorage.getEnergyStored()
                                + " FE / " + Config.PRIMITIVE_STIRLING_GENERATOR_MAX_POWER.get()
                                + " FE"
                ), mouseX, mouseY);
            }));
        } else if (isHovering(79, 53, 18, 18, mouseX, mouseY)){
            renderTooltip(matrixStack, Component.nullToEmpty(TextUtil.translateString("text.voluminousenergy.percent_burned").getString() + ": " + tileEntity.progressCounterPercent() + "%, "+TextUtil.translateString("text.voluminousenergy.ticks_left").getString()+": " + tileEntity.ticksLeft() + ", "+TextUtil.translateString("text.voluminousenergy.generating").getString()+": " + tileEntity.getEnergyRate() + " FE/t"), mouseX, mouseY);
        }
        super.renderTooltip(matrixStack,mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack,int mouseX, int mouseY){
        //this.font.drawString(this.title.getFormattedText(), 8.0F,6.0F,4210752);
        //drawString(matrixStack,Minecraft.getInstance().fontRenderer, "Primitive Stirling Generator",8,6,0xffffff);
        this.font.drawShadow(matrixStack, TextUtil.translateVEBlock("primitivestirlinggenerator"), 8.0F, 6.0F, 16777215);

        int generationRate;
        if (tileEntity.ticksLeft() > 0) generationRate = Config.PRIMITIVE_STIRLING_GENERATOR_GENERATE.get();
        else generationRate = 0;
        drawString(matrixStack,Minecraft.getInstance().font, TextUtil.translateString("text.voluminousenergy.generating").getString() + ": " + generationRate + " FE/t", 50, 18, 0xffffff);
        //this.font.drawString(matrixStack,this.playerInventory.getDisplayName().getString(),8.0F, (float) (this.ySize - 96 - 12), 4210752);
        this.font.drawShadow(matrixStack,new TranslatableComponent("container.inventory"), 8.0F, (float)(this.imageHeight - 96 + 2), 16777215);
        //drawString(matrixStack,Minecraft.getInstance().fontRenderer, "Energy: " + container.getEnergy(), 10, 22, 0xffffff);
    }

    @Override
    protected void renderBg(PoseStack matrixStack,float partialTicks, int mouseX, int mouseY){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, this.GUI);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack,i, j, 0, 0, this.imageWidth, this.imageHeight); // Actual Gui
        if (tileEntity != null) {
            int power = menu.powerScreen(49);
            int progress = tileEntity.progressCounterPX(14);
            this.blit(matrixStack,i + 81, j + (55 + (14-progress)), 176, (14-progress), 14, progress); // 55 = full, 55+14 = end
            this.blit(matrixStack,i + 11, j + (16 + (49-power)), 176, 14 + (49-power), 12, power);
            RenderSystem.setShaderTexture(0, GUI_TOOLS);
            drawIOSideHelper();
        }
    }
}
