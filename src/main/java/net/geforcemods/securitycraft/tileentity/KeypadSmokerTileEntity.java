package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.containers.KeypadSmokerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;

public class KeypadSmokerTileEntity extends AbstractKeypadFurnaceTileEntity {
	public KeypadSmokerTileEntity() {
		super(SCContent.teTypeKeypadSmoker, IRecipeType.SMOKING);
	}

	@Override
	protected int getBurnTime(ItemStack fuel) {
		return super.getBurnTime(fuel) / 2;
	}

	@Override
	protected Container createMenu(int windowId, PlayerInventory inv) {
		return new KeypadSmokerContainer(windowId, world, pos, inv, this, furnaceData);
	}
}
