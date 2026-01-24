package com.mrbysco.gonefishing.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mrbysco.gonefishing.GoneFishingPlugin;
import com.mrbysco.gonefishing.util.FishHelper;

public class BobberComponent implements Component<EntityStore> {
	// Max time to wait for a catch
	private static final int MIN_CATCH_TIME = 100;

	// Age of the bobber
	private int bobberAge;
	// Time remaining until a catch is available
	private int timeUntilCatch;
	// Flag to indicate if the bobber has a catch available
	private boolean canCatch;
	// Timer to track how long a catch has been available
	private int catchTimer;

	public BobberComponent() {
		this.bobberAge = 0;
		this.canCatch = false;
		this.timeUntilCatch = -1;
		this.catchTimer = 0;
	}

	public static ComponentType<EntityStore, BobberComponent> getComponentType() {
		return GoneFishingPlugin.bobberComponent;
	}

	public int getBobberAge() {
		return bobberAge;
	}

	public void setBobberAge(int bobberAge) {
		this.bobberAge = bobberAge;
	}

	public void setCanCatch(boolean canCatch) {
		this.canCatch = canCatch;
		if (canCatch) {
			this.catchTimer = MIN_CATCH_TIME;
		} else {
			this.catchTimer = 0;
		}
	}

	public void setCatchTimer(int catchTimer) {
		this.catchTimer = catchTimer;
	}

	public int getTimeUntilCatch() {
		return timeUntilCatch;
	}

	public void setTimeUntilCatch(int timeUntilCatch) {
		this.timeUntilCatch = timeUntilCatch;
	}

	public void setRandomTimeUntilCatch() {
		this.timeUntilCatch = FishHelper.getTimeUntilCatch();
	}

	public int getCatchTimer() {
		return catchTimer;
	}

	public boolean canCatchFish() {
		return this.canCatch && this.catchTimer > 0;
	}

	@Override
	public Component<EntityStore> clone() {
		BobberComponent component = new BobberComponent();
		component.bobberAge = this.bobberAge;
		component.canCatch = this.canCatch;
		component.timeUntilCatch = this.timeUntilCatch;
		component.catchTimer = this.catchTimer;
		return component;
	}
}