package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.blocks.ProtectoBlock;
import net.geforcemods.securitycraft.entity.SentryEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class ProtectoTileEntity extends DisguisableTileEntity {

	public ProtectoTileEntity()
	{
		super(SCContent.teTypeProtecto);
	}

	@Override
	public boolean attackEntity(Entity entity){
		if (entity instanceof LivingEntity && !(entity instanceof SentryEntity) && !EntityUtils.isInvisible(((LivingEntity)entity))) {
			if (entity instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity)entity;

				if(player.isCreative() || player.isSpectator() || getOwner().isOwner(player) || ModuleUtils.isAllowed(this, entity))
					return false;
			}

			if(!world.isRemote)
				((ServerWorld)world).addLightningBolt(new LightningBoltEntity(world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), false));

			world.setBlockState(pos, getBlockState().with(ProtectoBlock.ACTIVATED, false));
			return true;
		}

		return false;
	}

	@Override
	public boolean canAttack() {
		boolean canAttack = (getAttackCooldown() >= getTicksBetweenAttacks() && world.canBlockSeeSky(pos) && world.isRaining());

		if(canAttack && !getBlockState().get(ProtectoBlock.ACTIVATED))
			world.setBlockState(pos, getBlockState().with(ProtectoBlock.ACTIVATED, true));
		else if(!canAttack && getBlockState().get(ProtectoBlock.ACTIVATED))
			world.setBlockState(pos, getBlockState().with(ProtectoBlock.ACTIVATED, false));

		return canAttack;
	}

	@Override
	public boolean shouldAttackEntityType(Entity entity)
	{
		return entity instanceof LivingEntity;
	}

	@Override
	public boolean shouldRefreshAttackCooldown() {
		return false;
	}

	@Override
	public int getTicksBetweenAttacks()
	{
		return hasModule(ModuleType.SPEED) ? 100 : 200;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[]{ModuleType.ALLOWLIST, ModuleType.SPEED, ModuleType.DISGUISE};
	}

	@Override
	public Option<?>[] customOptions() {
		return null;
	}
}
