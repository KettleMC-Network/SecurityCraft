package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.containers.BlockReinforcerContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class UniversalBlockReinforcerItem extends Item {
	public UniversalBlockReinforcerItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		if (!world.isRemote && player instanceof ServerPlayerEntity) {
			NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
				@Override
				public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
					return new BlockReinforcerContainer(windowId, inv, UniversalBlockReinforcerItem.this == SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL_1.get());
				}

				@Override
				public ITextComponent getDisplayName() {
					return new TranslationTextComponent(getTranslationKey());
				}
			}, data -> data.writeBoolean(this == SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL_1.get()));
		}

		return ActionResult.resultConsume(player.getHeldItem(hand));
	}

	public static boolean convertBlock(ItemStack stack, BlockPos pos, PlayerEntity player) {
		if (!player.isCreative()) {
			World world = player.getEntityWorld();
			BlockState vanillaState = world.getBlockState(pos);
			Block block = vanillaState.getBlock();
			Block rb = IReinforcedBlock.VANILLA_TO_SECURITYCRAFT.get(block);

			if (rb != null) {
				BlockState convertedState = ((IReinforcedBlock) rb).getConvertedState(vanillaState);
				TileEntity te = world.getTileEntity(pos);
				CompoundNBT tag = null;

				if (te != null) {
					tag = te.write(new CompoundNBT());

					if (te instanceof IInventory)
						((IInventory) te).clear();
				}

				world.setBlockState(pos, convertedState);
				te = world.getTileEntity(pos);

				if (te != null) { //in case the converted state gets removed immediately after it is placed down
					if (tag != null)
						te.read(tag);

					((IOwnable) te).setOwner(player.getGameProfile().getId().toString(), player.getName().getString());
				}

				stack.damageItem(1, player, p -> p.sendBreakAnimation(p.getActiveHand()));
				return true;
			}
		}

		return false;
	}
}
