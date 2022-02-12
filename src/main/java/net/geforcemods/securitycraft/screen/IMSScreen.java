package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.IMSBlockEntity;
import net.geforcemods.securitycraft.blockentities.IMSBlockEntity.IMSTargetingMode;
import net.geforcemods.securitycraft.inventory.GenericBEMenu;
import net.geforcemods.securitycraft.network.server.SyncIMSTargetingOption;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class IMSScreen extends ContainerScreen<GenericBEMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private final String imsName = Utils.localize(SCContent.IMS.get().getDescriptionId()).getColoredString();
	private final String target = Utils.localize("gui.securitycraft:ims.target").getColoredString();
	private IMSBlockEntity tileEntity;
	private Button targetButton;
	private IMSTargetingMode targetMode;

	public IMSScreen(GenericBEMenu container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);
		tileEntity = (IMSBlockEntity) container.te;
		targetMode = tileEntity.getTargetingMode();
	}

	@Override
	public void init() {
		super.init();

		addButton(targetButton = new ExtendedButton(width / 2 - 75, height / 2 - 38, 150, 20, "", this::targetButtonClicked));
		updateButtonText();
	}

	@Override
	protected void renderLabels(int mouseX, int mouseY) {
		font.draw(imsName, imageWidth / 2 - font.width(imsName) / 2, 6, 4210752);
		font.draw(target, imageWidth / 2 - font.width(target) / 2, 30, 4210752);
	}

	@Override
	protected void renderBg(float partialTicks, int mouseX, int mouseY) {
		renderBackground();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bind(TEXTURE);
		blit(leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	protected void targetButtonClicked(Button button) {
		targetMode = IMSTargetingMode.values()[(targetMode.ordinal() + 1) % IMSTargetingMode.values().length]; //next enum value
		tileEntity.setTargetingMode(targetMode);
		SecurityCraft.channel.sendToServer(new SyncIMSTargetingOption(tileEntity.getBlockPos(), tileEntity.getTargetingMode()));
		updateButtonText();
	}

	private void updateButtonText() {
		System.out.println(targetMode);
		targetButton.setMessage(Utils.localize("gui.securitycraft:srat.targets" + (((targetMode.ordinal() + 2) % 3) + 1)).getColoredString());
	}
}
