package net.geforcemods.securitycraft.blocks;

import java.util.Iterator;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.entity.EntitySecurityCamera;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.tileentity.TileEntitySecurityCamera;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSecurityCamera extends BlockContainer{

	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	public static final PropertyBool POWERED = PropertyBool.create("powered");

	public BlockSecurityCamera(Material material) {
		super(material);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);

		if(stack.getItem() == Items.REDSTONE)
		{
			TileEntity te = world.getTileEntity(pos);

			if(te instanceof TileEntitySecurityCamera)
			{
				((TileEntitySecurityCamera)te).reactivate();

				if(!player.isCreative())
					stack.shrink(1);

				return true;
			}
		}

		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess access, BlockPos pos){
		return null;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		if(state.getValue(FACING) == EnumFacing.DOWN)
			return EnumBlockRenderType.MODEL;
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		super.breakBlock(world, pos, state);

		world.notifyNeighborsOfStateChange(pos.north(), world.getBlockState(pos).getBlock(), true);
		world.notifyNeighborsOfStateChange(pos.south(), world.getBlockState(pos).getBlock(), true);
		world.notifyNeighborsOfStateChange(pos.east(), world.getBlockState(pos).getBlock(), true);
		world.notifyNeighborsOfStateChange(pos.west(), world.getBlockState(pos).getBlock(), true);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		EnumFacing dir = BlockUtils.getBlockProperty(source, pos, FACING);
		float px = 1.0F/16.0F; //one sixteenth of a block

		if(dir == EnumFacing.SOUTH)
			return new AxisAlignedBB(0.275F, 0.250F, 0.000F, 0.700F, 0.800F, 0.850F);
		else if(dir == EnumFacing.NORTH)
			return new AxisAlignedBB(0.275F, 0.250F, 0.150F, 0.700F, 0.800F, 1.000F);
		else if(dir == EnumFacing.WEST)
			return new AxisAlignedBB(0.125F, 0.250F, 0.275F, 1.000F, 0.800F, 0.725F);
		else if(dir == EnumFacing.DOWN)
			return new AxisAlignedBB(px * 5, 1.0F - px * 2, px * 5, px * 11, 1.0F, px * 11);
		else
			return new AxisAlignedBB(0.000F, 0.250F, 0.275F, 0.850F, 0.800F, 0.725F);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		IBlockState state = getDefaultState().withProperty(POWERED, false);

		if(world.isSideSolid(pos.offset(facing.getOpposite()), facing))
			return state.withProperty(FACING, facing).withProperty(POWERED, false);
		else{
			Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();
			EnumFacing iFacing;

			do{
				if(!iterator.hasNext())
					return state;

				iFacing = (EnumFacing)iterator.next();
			}while (!world.isSideSolid(pos.offset(iFacing.getOpposite()), iFacing));

			return state.withProperty(FACING, facing).withProperty(POWERED, false);
		}
	}

	public void mountCamera(World world, int x, int y, int z, int id, EntityPlayer player){
		if(world.isRemote && player.getRidingEntity() == null)
			PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("tile.securitycraft:securityCamera.name"), ClientUtils.localize("messages.securitycraft:securityCamera.mounted").replace("#w", KeyBinding.getDisplayString("key.forward").get().toUpperCase()).replace("#a", KeyBinding.getDisplayString("key.left").get().toUpperCase()).replace("#s", KeyBinding.getDisplayString("key.back").get().toUpperCase()).replace("#d", KeyBinding.getDisplayString("key.right").get().toUpperCase()).replace("#i", KeyBindings.cameraZoomIn.getDisplayName()).replace("#o", KeyBindings.cameraZoomOut.getDisplayName()), TextFormatting.GREEN);

		if(player.getRidingEntity() instanceof EntitySecurityCamera){
			EntitySecurityCamera dummyEntity = new EntitySecurityCamera(world, x, y, z, id, (EntitySecurityCamera) player.getRidingEntity());
			WorldUtils.addScheduledTask(world, () -> world.spawnEntity(dummyEntity));
			player.startRiding(dummyEntity);
			return;
		}

		EntitySecurityCamera dummyEntity = new EntitySecurityCamera(world, x, y, z, id, player);
		WorldUtils.addScheduledTask(world, () -> world.spawnEntity(dummyEntity));
		player.startRiding(dummyEntity);

		for(Entity e : world.loadedEntityList)
			if(e instanceof EntityLiving)
				if(((EntityLiving)e).getAttackTarget() == player)
					((EntityLiving)e).setAttackTarget(null);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side){
		if(side == EnumFacing.UP)
			return false;
		else
			return world.isSideSolid(pos.offset(side.getOpposite()), side);
	}

	@Override
	public boolean canProvidePower(IBlockState state){
		return true;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side){
		if(state.getValue(POWERED) && ((IModuleInventory) world.getTileEntity(pos)).hasModule(EnumModuleType.REDSTONE))
			return 15;
		else
			return 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side){
		if(state.getValue(POWERED) && ((IModuleInventory) world.getTileEntity(pos)).hasModule(EnumModuleType.REDSTONE))
			return 15;
		else
			return 0;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!canPlaceBlockAt(world, pos) && !canPlaceBlockOnSide(world, pos, state.getValue(FACING))) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		if(meta <= 5)
			return getDefaultState().withProperty(FACING, (EnumFacing.values()[meta] == EnumFacing.UP) ? EnumFacing.NORTH : EnumFacing.values()[meta]).withProperty(POWERED, false);
		else
			return getDefaultState().withProperty(FACING, EnumFacing.values()[meta - 6]).withProperty(POWERED, true);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		if(state.getValue(POWERED))
			return (state.getValue(FACING).getIndex() + 6);
		else
			return state.getValue(FACING).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, FACING, POWERED);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta){
		return new TileEntitySecurityCamera().nameable();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot)
	{
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror)
	{
		EnumFacing facing = state.getValue(FACING);

		switch(mirror)
		{
			case LEFT_RIGHT:
				if(facing.getAxis() == Axis.Z)
					return state.withProperty(FACING, facing.getOpposite());
				break;
			case FRONT_BACK:
				if(facing.getAxis() == Axis.X)
					return state.withProperty(FACING, facing.getOpposite());
				break;
			case NONE: break;
		}

		return state;
	}
}
