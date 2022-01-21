package net.geforcemods.securitycraft.screen.components;

import java.util.function.IntFunction;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fmlclient.gui.widget.ExtendedButton;

public class ToggleComponentButton extends ExtendedButton {
	private final IntFunction<Component> onValueChange;
	private int currentIndex = 0;
	private final int toggleCount;

	public ToggleComponentButton(int xPos, int yPos, int width, int height, IntFunction<Component> onValueChange, int initialValue, int toggleCount, OnPress onPress) {
		super(xPos, yPos, width, height, TextComponent.EMPTY, onPress);

		this.onValueChange = onValueChange;
		this.currentIndex = initialValue;
		this.toggleCount = toggleCount;
		onValueChange();
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (Screen.hasShiftDown())
			cycleIndex(-1);
		else
			cycleIndex(1);

		super.onClick(mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		cycleIndex(-(int) Math.signum(delta));
		onPress.onPress(this);
		return true;
	}

	public void cycleIndex(int value) {
		currentIndex = Math.floorMod(currentIndex + value, toggleCount);
		onValueChange();
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void onValueChange() {
		setMessage(onValueChange.apply(currentIndex));
	}
}