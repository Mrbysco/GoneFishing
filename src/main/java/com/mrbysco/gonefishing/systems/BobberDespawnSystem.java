package com.mrbysco.gonefishing.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mrbysco.gonefishing.GoneFishingPlugin;
import com.mrbysco.gonefishing.component.BoundBobberComponent;
import com.mrbysco.gonefishing.interaction.FishingMetaData;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.UUID;

public class BobberDespawnSystem extends EntityTickingSystem<EntityStore> {


	@Override
	public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
	                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
		World world = commandBuffer.getExternalData().getWorld();
		Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
		BoundBobberComponent bound = store.getComponent(playerRef, GoneFishingPlugin.get().getBoundBobberComponent());
		if (bound == null) return;

		UUID attached = bound.getAttachedEntity();
		if (attached == null) {
			commandBuffer.tryRemoveComponent(playerRef, GoneFishingPlugin.get().getBoundBobberComponent());
			return;
		}

		Player player = store.getComponent(playerRef, Player.getComponentType());
		if (player == null) return;

		Inventory inventory = player.getInventory();
		ItemStack active = inventory.getActiveHotbarItem();

		boolean holdingBoundRod = false;
		if (active != null) {
			FishingMetaData meta = active.getFromMetadataOrNull(FishingMetaData.KEY, FishingMetaData.CODEC);
			if (meta != null && attached.equals(meta.getBoundBobber())) {
				holdingBoundRod = true;
			}
		}

		// If player is not holding the bound rod, despawn the bobber and clean up the binding
		if (!holdingBoundRod) {
			// Remove the bobber entity if it still exists
			Ref<EntityStore> bobberRef = world.getEntityStore().getRefFromUUID(attached);
			if (bobberRef != null) {
				commandBuffer.removeEntity(bobberRef, RemoveReason.REMOVE);
			}

			// Clear bound metadata from any fishing rods in the player's hotbar
			for (short slot = 0; slot < 9; slot++) {
				ItemStack stack = inventory.getHotbar().getItemStack(slot);
				if (stack == null) continue;
				FishingMetaData meta = stack.getFromMetadataOrNull(FishingMetaData.KEY, FishingMetaData.CODEC);
				if (meta != null && attached.equals(meta.getBoundBobber())) {
					ItemStack newStack = stack.withMetadata(FishingMetaData.KEY, null);
					inventory.getHotbar().replaceItemStackInSlot((byte) slot, stack, newStack);
//					GoneFishingPlugin.LOGGER.atInfo().log("Cleared fishing metadata from hotbar slot %d for player ref %s", slot, playerRef);
				}
			}

			// Remove bound bobber component from player
			commandBuffer.tryRemoveComponent(playerRef, GoneFishingPlugin.get().getBoundBobberComponent());
		}
	}

	@NullableDecl
	@Override
	public Query<EntityStore> getQuery() {
		return GoneFishingPlugin.get().getBoundBobberComponent();
	}
}
