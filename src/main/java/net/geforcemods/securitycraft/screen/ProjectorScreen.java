package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.geforcemods.securitycraft.containers.ProjectorContainer;
import net.geforcemods.securitycraft.screen.components.NamedSlider;
import net.geforcemods.securitycraft.tileentity.ProjectorTileEntity;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.HoverChecker;

@OnlyIn(Dist.CLIENT)
public class ProjectorScreen extends ContainerScreen<ProjectorContainer> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private ProjectorTileEntity tileEntity;
	private String blockName;
	
	private HoverChecker[] hoverCheckers = new HoverChecker[3];
	
	private NamedSlider projectionWidth;
	private NamedSlider projectionRange;
	private NamedSlider projectionOffset;
	
	private int sliderWidth = 120;

	public ProjectorScreen(ProjectorContainer container, PlayerInventory inv, ITextComponent name) 
	{
		super(container, inv, name);
		this.tileEntity = container.te;
		blockName = ClientUtils.localize(tileEntity.getBlockState().getBlock().getTranslationKey());
	}
	
	@Override
	public void init()
	{
		super.init();
		
		projectionWidth = new NamedSlider("", blockName, 0, guiLeft + ((xSize - sliderWidth) / 2) + 20, guiTop + 20, sliderWidth, 20, "", "", ProjectorTileEntity.MIN_WIDTH, ProjectorTileEntity.MAX_WIDTH, tileEntity.getProjectionWidth(), true, true, null);
		projectionWidth.setFGColor(14737632);
		
		projectionRange = new NamedSlider("", blockName, 1, guiLeft + ((xSize - sliderWidth) / 2) + 20, guiTop + 50, sliderWidth, 20, "", "", ProjectorTileEntity.MIN_RANGE, ProjectorTileEntity.MAX_RANGE, tileEntity.getProjectionRange(), true, true, null);
		projectionRange.setFGColor(14737632);
		
		projectionOffset = new NamedSlider("", blockName, 2, guiLeft + ((xSize - sliderWidth) / 2) + 20, guiTop + 80, sliderWidth, 20, "", "", ProjectorTileEntity.MIN_OFFSET, ProjectorTileEntity.MAX_OFFSET, tileEntity.getProjectionOffset(), true, true, null);
		projectionOffset.setFGColor(14737632);
		
		addButton(projectionWidth);
		addButton(projectionRange);
		addButton(projectionOffset);

		hoverCheckers[0] = new HoverChecker(projectionWidth, 20);
		hoverCheckers[1] = new HoverChecker(projectionRange, 20);
		hoverCheckers[2] = new HoverChecker(projectionOffset, 20);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		super.render(mouseX, mouseY, partialTicks);
		
		for(int i = 0; i < hoverCheckers.length; i++)
			if(hoverCheckers[i] != null && hoverCheckers[i].checkHover(mouseX, mouseY))
				this.renderTooltip(minecraft.fontRenderer.listFormattedStringToWidth("test", 150), mouseX, mouseY, font);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		font.drawString(blockName, xSize / 2 - font.getStringWidth(blockName) / 2, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		renderBackground();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(startX, startY, 0, 0, xSize, ySize);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		if(button == 0)
			tileEntity.setProjectionWidth(projectionWidth.getValueInt());
		else if(button == 1)
			tileEntity.setProjectionRange(projectionRange.getValueInt());
		else if(button == 2)
			tileEntity.setProjectionOffset(projectionOffset.getValueInt());
		
		return super.mouseReleased(mouseX, mouseY, button);
	}

}
