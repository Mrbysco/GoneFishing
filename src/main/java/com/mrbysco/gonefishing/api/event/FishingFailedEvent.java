package com.mrbysco.gonefishing.api.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Event fired when player failed to catch a fish. This event allows you to know when a player failed to catch a fish, and get the player reference.
 */
public class FishingFailedEvent extends EcsEvent {
	private final Ref<EntityStore> playerRef;

	public FishingFailedEvent(Ref<EntityStore> playerRef) {
		this.playerRef = playerRef;
	}

	public Ref<EntityStore> getPlayerRef() {
		return playerRef;
	}
}
