package com.mrbysco.gonefishing.api.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Event fired when player caught a fish. This event is cancellable, and allows you to modify the caught item.
 */
public class FishCaughtEvent extends CancellableEcsEvent {
	private ItemStack caughtItem;
	private final Ref<EntityStore> playerRef;

	public FishCaughtEvent(ItemStack caughtItem, Ref<EntityStore> playerRef) {
		this.caughtItem = caughtItem;
		this.playerRef = playerRef;
	}

	public ItemStack getCaughtItem() {
		return caughtItem;
	}

	public void setCaughtItem(ItemStack caughtItem) {
		this.caughtItem = caughtItem;
	}

	public Ref<EntityStore> getPlayerRef() {
		return playerRef;
	}
}
