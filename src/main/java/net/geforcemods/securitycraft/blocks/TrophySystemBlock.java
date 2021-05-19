package net.geforcemods.securitycraft.blocks;

import java.util.stream.Stream;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.containers.GenericTEContainer;
import net.geforcemods.securitycraft.tileentity.TrophySystemTileEntity;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class TrophySystemBlock extends OwnableBlock {

	private static final VoxelShape SHAPE = Stream.of(
			Block.makeCuboidShape(6.5, 0, 12, 9.5, 1.5, 15),
			Block.makeCuboidShape(5.5, 7, 5.5, 10.5, 11, 10.5),
			Block.makeCuboidShape(7, 12, 7, 9, 13, 9),
			Block.makeCuboidShape(6.5, 12.5, 6.5, 9.5, 15, 9.5),
			Block.makeCuboidShape(7, 14.5, 7, 9, 15.5, 9),
			Block.makeCuboidShape(7.25, 9, 7.25, 8.75, 12, 8.75),
			Block.makeCuboidShape(1, 0, 6.5, 4, 1.5, 9.5),
			Block.makeCuboidShape(12, 0, 6.5, 15, 1.5, 9.5),
			Block.makeCuboidShape(6.5, 0, 1, 9.5, 1.5, 4)
			).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).orElse(VoxelShapes.fullCube());

	public TrophySystemBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos){
		return BlockUtils.isSideSolid(world, pos.down(), Direction.UP);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean flag) {
		if(!isValidPosition(state, world, pos)) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (((IOwnable) world.getTileEntity(pos)).getOwner().isOwner(player)) {
			if (!world.isRemote)
				NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider() {
					@Override
					public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
						return new GenericTEContainer(SCContent.cTypeTrophySystem, windowId, world, pos);
					}

					@Override
					public ITextComponent getDisplayName() {
						return new TranslationTextComponent(getTranslationKey());
					}
				}, pos);

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TrophySystemTileEntity();
	}

}
