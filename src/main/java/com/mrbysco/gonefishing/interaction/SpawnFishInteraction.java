package com.mrbysco.gonefishing.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.mrbysco.gonefishing.util.FishHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class SpawnFishInteraction extends SimpleInstantInteraction {
	public static final BuilderCodec<SpawnFishInteraction> CODEC = BuilderCodec.builder(
					SpawnFishInteraction.class, SpawnFishInteraction::new, SimpleInstantInteraction.CODEC
			)
			.documentation("Spawns the right-clicked fish.")
			.build();

	@Override
	protected void firstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler handler) {
		if (!FishHelper.canReleaseFish()) {
			context.getState().state = InteractionState.Failed;
			return;
		}
		CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
		World world = commandBuffer.getExternalData().getWorld();
		if (commandBuffer == null) {
			context.getState().state = InteractionState.Failed;
		} else {
			ItemStack itemstack = context.getHeldItem();
			if (itemstack == null) {
				context.getState().state = InteractionState.Failed;
			} else {
				Ref<EntityStore> ref = context.getEntity();
				PlayerRef playerref = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
				Player player = commandBuffer.getComponent(ref, Player.getComponentType());

				if (player == null || playerref == null) {
					context.getState().state = InteractionState.Failed;
					return;
				}
				Inventory inventory = player.getInventory();
				byte activeSlot = inventory.getActiveHotbarSlot();
				ItemStack hotbarItem = inventory.getActiveHotbarItem();

				Vector3i getTargetWater = getTargetWater(commandBuffer, world, 10F, ref);
				if (getTargetWater == null) {
					context.getState().state = InteractionState.Failed;
					return;
				}
				BlockPosition blockposition = new BlockPosition(getTargetWater.x, getTargetWater.y, getTargetWater.z);

				if (hotbarItem == null || blockposition == null) {
					context.getState().state = InteractionState.Failed;
					return;
				}

				Map<String, String> interactionVars = context.getInteractionVars();
				String s = interactionVars.get("SpawnNPC_Entity");
				if (s != null) {
					RootInteraction rootinteraction = RootInteraction.getRootInteractionOrUnknown(s);
					if (rootinteraction == null) {
						context.getState().state = InteractionState.Failed;
						return;
					}
					context.getState().state = InteractionState.Finished;
					context.execute(rootinteraction);

					ItemStackSlotTransaction itemstackslottransaction = context.getHeldItemContainer()
							.removeItemStackFromSlot(context.getHeldItemSlot(), hotbarItem, 1);
					if (!itemstackslottransaction.succeeded()) {
						context.getState().state = InteractionState.Failed;
						return;
					}

					context.setHeldItem(itemstackslottransaction.getSlotAfter());
				}
			}
		}
	}

	/**
	 * Gets the target water block based on the raycast parameters.
	 *
	 * @param world       the world reference
	 * @param maxDistance the maximum distance for the raycast
	 * @param ref         the entity reference
	 * @return the target water block position, or null if none found
	 */
	@Nullable
	public Vector3i getTargetWater(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull World world, float maxDistance, Ref<EntityStore> ref) {
		Transform transform = TargetUtil.getLook(ref, commandBuffer);
		Vector3d vector3d = transform.getPosition();
		Vector3d vector3d1 = transform.getDirection();
		return TargetUtil.getTargetBlock(
				world,
				(_blockId, fluidId) -> fluidId != 0,
				vector3d.x, vector3d.y, vector3d.z, vector3d1.x, vector3d1.y, vector3d1.z, maxDistance
		);
	}
}
