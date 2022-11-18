package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.blocks.reinforced.BaseReinforcedBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedSnowyDirtBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.trunkplacer.AbstractTrunkPlacer;

/**
 * Mimics IForgeBlockState#onTreeGrow in 1.19.2 by disallowing reinforced blocks to be replaced with dirt when a tree grows
 * above them
 */
@Mixin(AbstractTrunkPlacer.class)
public class AbstractTrunkPlacerMixin {
	@Inject(method = "setDirtAt", at = @At("HEAD"), cancellable = true)
	private static void onSetDirtAt(IWorldGenerationReader world, BlockPos pos, CallbackInfo callback) {
		if (world instanceof IWorldReader) {
			Block block = ((IWorldReader) world).getBlockState(pos).getBlock();

			if (block instanceof BaseReinforcedBlock || block instanceof ReinforcedSnowyDirtBlock)
				callback.cancel();
		}
	}
}