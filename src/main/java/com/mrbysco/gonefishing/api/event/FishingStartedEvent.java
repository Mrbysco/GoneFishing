package com.mrbysco.gonefishing.api.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Event fired when player starts fishing. This event is cancellable, and allows you to know when a player starts fishing, and get the player reference.
 */
public class FishingStartedEvent extends CancellableEcsEvent {
	private final Ref<EntityStore> playerRef;

	public FishingStartedEvent(Ref<EntityStore> playerRef) {
		this.playerRef = playerRef;
	}

	public Ref<EntityStore> getPlayerRef() {
		return playerRef;
	}
}
