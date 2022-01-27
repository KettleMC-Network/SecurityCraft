package net.geforcemods.securitycraft.compat.waila;

import java.util.List;
import java.util.Optional;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.entity.SentryEntity;
import net.geforcemods.securitycraft.entity.SentryEntity.SentryMode;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;

@WailaPlugin(SecurityCraft.MODID)
public class WailaDataProvider implements IWailaPlugin, IComponentProvider, IEntityComponentProvider {
	public static final WailaDataProvider INSTANCE = new WailaDataProvider();
	public static final ResourceLocation SHOW_OWNER = new ResourceLocation(SecurityCraft.MODID, "showowner");
	public static final ResourceLocation SHOW_MODULES = new ResourceLocation(SecurityCraft.MODID, "showmodules");
	public static final ResourceLocation SHOW_PASSWORDS = new ResourceLocation(SecurityCraft.MODID, "showpasswords");
	public static final ResourceLocation SHOW_CUSTOM_NAME = new ResourceLocation(SecurityCraft.MODID, "showcustomname");

	static {
		if (FMLEnvironment.dist == Dist.CLIENT)
			MinecraftForge.EVENT_BUS.addListener(WailaDataProvider::onWailaRender);
	}

	@Override
	public void register(IRegistrar registrar) {
		registrar.addSyncedConfig(SHOW_OWNER, true);
		registrar.addSyncedConfig(SHOW_MODULES, true);
		registrar.addSyncedConfig(SHOW_PASSWORDS, true);
		registrar.addSyncedConfig(SHOW_CUSTOM_NAME, true);
		registrar.registerComponentProvider((IComponentProvider) INSTANCE, TooltipPosition.BODY, IOwnable.class);
		registrar.registerStackProvider(INSTANCE, IOverlayDisplay.class);
		registrar.registerComponentProvider((IEntityComponentProvider) INSTANCE, TooltipPosition.BODY, SentryEntity.class);
	}

	@Override
	public ItemStack getStack(IDataAccessor data, IPluginConfig config) {
		if (data.getBlock() instanceof IOverlayDisplay)
			return ((IOverlayDisplay) data.getBlock()).getDisplayStack(data.getWorld(), data.getBlockState(), data.getPosition());

		return ItemStack.EMPTY;
	}

	@Override
	public void appendBody(List<ITextComponent> body, IDataAccessor data, IPluginConfig config) {
		Block block = data.getBlock();
		boolean disguised = false;

		if (block instanceof DisguisableBlock) {
			Optional<BlockState> disguisedBlockState = ((DisguisableBlock) block).getDisguisedBlockState(data.getWorld(), data.getPosition());

			if (disguisedBlockState.isPresent()) {
				disguised = true;
				block = disguisedBlockState.get().getBlock();
			}
		}

		if (block instanceof IOverlayDisplay && !((IOverlayDisplay) block).shouldShowSCInfo(data.getWorld(), data.getBlockState(), data.getPosition()))
			return;

		TileEntity te = data.getTileEntity();

		//last part is a little cheaty to prevent owner info from being displayed on non-sc blocks
		if (config.get(SHOW_OWNER) && te instanceof IOwnable && block.getRegistryName().getNamespace().equals(SecurityCraft.MODID))
			body.add(new StringTextComponent(Utils.localize("waila.securitycraft:owner", PlayerUtils.getOwnerComponent(((IOwnable) te).getOwner().getName())).getColoredString()));

		if (disguised)
			return;

		//if the te is ownable, show modules only when it's owned, otherwise always show
		if (config.get(SHOW_MODULES) && te instanceof IModuleInventory && (!(te instanceof IOwnable) || ((IOwnable) te).getOwner().isOwner(data.getPlayer()))) {
			if (!((IModuleInventory) te).getInsertedModules().isEmpty())
				body.add(Utils.localize("waila.securitycraft:equipped"));

			for (ModuleType module : ((IModuleInventory) te).getInsertedModules()) {
				body.add(new StringTextComponent("- " + new TranslationTextComponent(module.getTranslationKey()).getColoredString()));
			}
		}

		if (config.get(SHOW_PASSWORDS) && te instanceof IPasswordProtected && !(te instanceof KeycardReaderBlockEntity) && ((IOwnable) te).getOwner().isOwner(data.getPlayer())) {
			String password = ((IPasswordProtected) te).getPassword();

			body.add(new StringTextComponent(Utils.localize("waila.securitycraft:password").getColoredString() + " " + (password != null && !password.isEmpty() ? password : Utils.localize("waila.securitycraft:password.notSet").getColoredString())));
		}
	}

	@Override
	public void appendBody(List<ITextComponent> body, IEntityAccessor data, IPluginConfig config) {
		Entity entity = data.getEntity();

		if (entity instanceof SentryEntity) {
			SentryEntity sentry = (SentryEntity) entity;
			SentryMode mode = sentry.getMode();

			if (config.get(SHOW_OWNER))
				body.add(new StringTextComponent(Utils.localize("waila.securitycraft:owner", PlayerUtils.getOwnerComponent(sentry.getOwner().getName())).getColoredString()));

			if (config.get(SHOW_MODULES) && sentry.getOwner().isOwner(data.getPlayer())) {
				if (!sentry.getAllowlistModule().isEmpty() || !sentry.getDisguiseModule().isEmpty() || sentry.hasSpeedModule()) {
					body.add(new StringTextComponent(Utils.localize("waila.securitycraft:equipped").getColoredString()));

					if (!sentry.getAllowlistModule().isEmpty())
						body.add(new StringTextComponent("- " + new TranslationTextComponent(ModuleType.ALLOWLIST.getTranslationKey()).getColoredString()));

					if (!sentry.getDisguiseModule().isEmpty())
						body.add(new StringTextComponent("- " + new TranslationTextComponent(ModuleType.DISGUISE.getTranslationKey()).getColoredString()));

					if (sentry.hasSpeedModule())
						body.add(new StringTextComponent("- " + new TranslationTextComponent(ModuleType.SPEED.getTranslationKey()).getColoredString()));
				}
			}

			String modeDescription = Utils.localize(mode.getModeKey()).getColoredString();

			if (mode != SentryMode.IDLE)
				modeDescription += "- " + Utils.localize(mode.getTargetKey()).getColoredString();

			body.add(new StringTextComponent(TextFormatting.GRAY + modeDescription));
		}
	}

	public static void onWailaRender(WailaRenderEvent.Pre event) {
		if (ClientHandler.isPlayerMountedOnCamera())
			event.setCanceled(true);
	}
}
