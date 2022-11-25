package net.geforcemods.securitycraft.blocks;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FakeLavaBlock extends FlowingFluidBlock {
	private static final EffectInstance SHORT_FIRE_RESISTANCE = new EffectInstance(Effects.FIRE_RESISTANCE, 1);

	public FakeLavaBlock(Block.Properties properties, Supplier<? extends FlowingFluid> fluid) {
		super(fluid, properties);
	}

	@Override
	public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
		super.entityInside(state, world, pos, entity);

		if (entity instanceof LivingEntity) {
			LivingEntity lEntity = (LivingEntity) entity;

			lEntity.clearFire();
			lEntity.setSharedFlag(0, false);

			if (!world.isClientSide) {
				lEntity.addEffect(SHORT_FIRE_RESISTANCE);

				if (!lEntity.hasEffect(Effects.REGENERATION))
					lEntity.addEffect(new EffectInstance(Effects.REGENERATION, 20, 2, false, false));
			}
		}
	}
}
