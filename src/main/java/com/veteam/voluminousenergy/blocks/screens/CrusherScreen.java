package com.veteam.voluminousenergy.blocks.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.containers.CrusherContainer;
import com.veteam.voluminousenergy.blocks.tiles.CrusherTile;
import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.tools.buttons.ioMenuButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrusherScreen extends ContainerScreen<CrusherContainer> {
    private CrusherTile tileEntity;
    private final static ResourceLocation GUI = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/crushergui.png");
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean openedIOGui = false;

    public CrusherScreen(CrusherContainer screenContainer, PlayerInventory inv, ITextComponent titleIn){
        super(screenContainer,inv,titleIn);
        tileEntity = (CrusherTile) screenContainer.tileEntity;
    }

    @Override
    protected void init() {
        /*
        this.addButton(new ioMenuButton(152,4,20,18, button -> {
            LOGGER.debug("Hit button");
            openedIOGui = ((ioMenuButton) button).openGui();
        }));
         */
        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);
        super.render(matrixStack,mouseX,mouseY,partialTicks);
        this.renderHoveredTooltip(matrixStack,mouseX,mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack,int mouseX, int mouseY) {
        drawString(matrixStack,Minecraft.getInstance().fontRenderer, "Crusher",8,6,0xffffff);
        this.font.func_243246_a(matrixStack,new TranslationTextComponent("container.inventory"), 8.0F, (float)(this.ySize - 96 + 2), 16777215);
    }

    @Override
    protected void renderHoveredTooltip(MatrixStack matrixStack,int mouseX, int mouseY) {
        if (isPointInRegion(11, 16, 12, 49, mouseX, mouseY)) {
            renderTooltip(matrixStack, ITextComponent.getTextComponentOrEmpty(container.getEnergy() + " FE" + " / " + Config.CRUSHER_MAX_POWER.get() + " FE"), mouseX, mouseY);
        } /*else if (isPointInRegion(152, 4, 20, 18, mouseX, mouseY)){
            if (openedIOGui){
                renderTooltip("Close IO Management", mouseX, mouseY);
            } else {
                renderTooltip("Open IO Management", mouseX, mouseY);
            }
        }*/
        super.renderHoveredTooltip(matrixStack,mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack,float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack,i, j, 0, 0, this.xSize, this.ySize);
        if(tileEntity != null){
            int progress = tileEntity.progressCounterPX(24);
            int power = container.powerScreen(49);

            /*Note for this.blit below:
                p_blit_1_ = starting x for blit on screen
                p_blit_2_ = starting y for blit on screen
                p_blit_3_ = starting x for blit to be stitched from in the file
                p_blit_4_ = starting y for blit to be stitched from in the file
                p_blit_5_ = width of the x for the blit to be drawn (make variable for progress illusion on the x)
                p_blit_6_ = width of the y for the blit to be drawn (make variable for progress illusion of the y)
             */
            this.blit(matrixStack,i+79, j+31, 176, 0, 17, progress);
            this.blit(matrixStack,i + 11, j + (16 + (49-power)), 176, 24 + (49-power), 12, power);
            //drawIOSideHelper(i,j,mouseX,mouseY,partialTicks);
        }

    }

    public static ResourceLocation getGUI() {
        return GUI;
    }

    private void drawIOSideHelper(MatrixStack matrixStack,int i, int j, int mouseX, int mouseY, float partialTicks){
        if (isPointInRegion(152, 4, 20, 18, mouseX, mouseY)) {
            this.blit(matrixStack,i+152,j+4,193,19,20,18);
        } else {
            this.blit(matrixStack,i+152,j+4,193,0,20,18);
        }
        for(Widget widget : this.buttons){
            if (widget instanceof ioMenuButton){
                this.openedIOGui = ((ioMenuButton) widget).openGui();
            }
        }
    }
}
