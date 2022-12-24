package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.IgnoreOwnerOption;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.misc.EnumModuleType;

public class TileEntityCageTrap extends TileEntityDisguisable {
	private OptionBoolean shouldCaptureMobsOption = new OptionBoolean("captureMobs", false);
	private DisabledOption disabled = new DisabledOption(false);
	private IgnoreOwnerOption ignoreOwner = new IgnoreOwnerOption(true);

	@Override
	public EnumModuleType[] acceptedModules() {
		return new EnumModuleType[] {
				EnumModuleType.DISGUISE, EnumModuleType.ALLOWLIST
		};
	}

	public boolean capturesMobs() {
		return shouldCaptureMobsOption.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public boolean ignoresOwner() {
		return ignoreOwner.get();
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				shouldCaptureMobsOption, disabled, ignoreOwner
		};
	}
}
