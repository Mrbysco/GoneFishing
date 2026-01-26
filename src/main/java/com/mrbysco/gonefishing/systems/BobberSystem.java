package com.mrbysco.gonefishing.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mrbysco.gonefishing.GoneFishingPlugin;
import com.mrbysco.gonefishing.component.BobberComponent;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

public class BobberSystem extends EntityTickingSystem<EntityStore> {

	@Override
	public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
	                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

		Ref<EntityStore> bobberRef = archetypeChunk.getReferenceTo(index);
		BobberComponent component = store.getComponent(bobberRef, BobberComponent.getComponentType());
		if (component != null) {
			int newAge = component.getBobberAge() + 1;
			if (newAge < 5) {
				AnimationUtils.playAnimation(bobberRef, AnimationSlot.Status, "Spawn", true, store);
			}
			component.setBobberAge(newAge);

			if (newAge < 50) {
				return; // Wait until bobber is fully spawned
			}

			// Check if a time until catch is set
			if (component.canCatchFish()) {
				int newCatchTimer = component.getCatchTimer() - 1;
				if (newCatchTimer > 0) {
					component.setCatchTimer(newCatchTimer);
				} else {
					// Reset catch state after timer expires
					component.setCanCatch(false);
					component.setRandomTimeUntilCatch();
					AnimationUtils.playAnimation(bobberRef, AnimationSlot.Status, "Idle", true, store);
				}
			} else {
				if (component.getTimeUntilCatch() != -1) {
					if (component.getTimeUntilCatch() <= 0) {
						component.setCanCatch(true);
						AnimationUtils.playAnimation(bobberRef, AnimationSlot.Status, "Catch", true, store);
					} else {
						int newTimeUntilCatch = component.getTimeUntilCatch() - 1;
						component.setTimeUntilCatch(newTimeUntilCatch);
					}
				} else {
					component.setRandomTimeUntilCatch();
					AnimationUtils.playAnimation(bobberRef, AnimationSlot.Status, "Idle", true, store);
				}
			}
		}
	}

	@NullableDecl
	@Override
	public Query<EntityStore> getQuery() {
		return GoneFishingPlugin.bobberComponent;
	}
}
