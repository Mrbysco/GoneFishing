package com.mrbysco.gonefishing.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.mrbysco.gonefishing.component.BobberComponent;
import com.mrbysco.gonefishing.util.FishHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

public class FishingInteraction extends SimpleInstantInteraction {
	public static final BuilderCodec<FishingInteraction> CODEC = BuilderCodec.builder(
					FishingInteraction.class, FishingInteraction::new, SimpleInstantInteraction.CODEC
			)
			.documentation("Spawns or reels in a bobber when right-clicked on a block with a fishing rod")
			.build();

	@Override
	protected void firstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler handler) {
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
				FishingMetaData fishingMetaData = itemstack.getFromMetadataOrNull(FishingMetaData.KEY, FishingMetaData.CODEC);
				if (fishingMetaData != null) {
					// Handle the fishing rod reeling logic
					reelBobber(world, commandBuffer, hotbarItem, inventory, activeSlot, fishingMetaData, playerref);
				} else {
					int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_GoneFishing_Cast");
					SoundUtil.playSoundEvent2dToPlayer(playerref, soundEventIndex, SoundCategory.SFX);

					// Handle the fishing rod casting logic
					spawnBobber(world, commandBuffer, context, hotbarItem, new Vector3i(blockposition.x, blockposition.y, blockposition.z), inventory, activeSlot);
				}
			}
		}
	}

	/**
	 * Handles the logic for reeling in the bobber.
	 *
	 * @param world           The game world
	 * @param commandBuffer   The command buffer for entity operations
	 * @param hotbarItem      The fishing rod item in the hotbar
	 * @param inventory       The player's inventory
	 * @param hotbarSlot      The hotbar slot index
	 * @param fishingMetaData The fishing rod's metadata containing the bound bobber UUID
	 */
	private void reelBobber(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer,
	                        @Nonnull ItemStack hotbarItem, Inventory inventory, byte hotbarSlot,
	                        @Nonnull FishingMetaData fishingMetaData, @Nonnull PlayerRef playerRef) {
		// Remove old bobber and adjust metadata to unbind
		adjustMetadata(inventory, hotbarSlot, hotbarItem, null);

		int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_GoneFishing_Reel");
		SoundUtil.playSoundEvent2dToPlayer(playerRef, soundEventIndex, SoundCategory.SFX);

		// Handle the bobber retrieval logic here
		Ref<EntityStore> bobberRef = world.getEntityStore().getRefFromUUID(fishingMetaData.getBoundBobber());
		if (bobberRef == null) {
			return;
		}
		BobberComponent component = commandBuffer.getComponent(bobberRef, BobberComponent.getComponentType());
		if (component == null || !component.canCatchFish()) {
			commandBuffer.removeEntity(bobberRef, RemoveReason.REMOVE);
			playerRef.sendMessage(Message.translation("gonefishing.tooEarly").color(Color.RED));
			return;
		}

		ItemStack fishStack = FishHelper.createRandomFish();
		if (!fishStack.isEmpty()) {
			Vector3d direction = TargetUtil.getLook(playerRef.getReference(), commandBuffer).getDirection().negate().add(0, 0.5, 0);
			ItemUtils.throwItem(bobberRef, commandBuffer, fishStack, direction, 10.0F);

			playerRef.sendMessage(Message.translation("gonefishing.caughtFish").color(Color.GREEN).param("fish", Message.translation(fishStack.getItem().getTranslationKey())));
		}
		commandBuffer.removeEntity(bobberRef, RemoveReason.REMOVE);
	}

	/**
	 * Handles the logic for spawning a new bobber.
	 *
	 * @param world         The game world
	 * @param commandBuffer The command buffer for entity operations
	 * @param context       The interaction context
	 * @param fishingStack  The fishing rod item stack
	 * @param targetBlock   The target block position
	 * @param inventory     The player's inventory
	 * @param hotbarSlot    The hotbar slot index
	 */
	private void spawnBobber(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionContext context,
	                         @Nonnull ItemStack fishingStack, @Nonnull Vector3i targetBlock, Inventory inventory, byte hotbarSlot) {
		Ref<EntityStore> ref = context.getEntity();
		Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
		Vector3d vector3d = targetBlock.toVector3d();
		vector3d.add(0.5, 0.25, 0.5);

		Vector3f rotation = new Vector3f();
		HeadRotation headRotation = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
		if (headRotation != null) {
			// Make the bpbber face upwards
			//Invert the yaw by 180 degrees to make the armor stand face the player
			rotation.setYaw(headRotation.getRotation().getYaw() + (float) (Math.PI / 180.0) * 180.0F);
		}

		WorldChunk worldchunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
		if (worldchunk != null) {
			BlockType blockType = worldchunk.getBlockType(targetBlock);
			@SuppressWarnings("removal") int i = worldchunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
			BlockBoundingBoxes.RotatedVariantBoxes variantBoxes = BlockBoundingBoxes.getAssetMap()
					.getAsset(blockType.getHitboxTypeIndex())
					.get(i);
			vector3d.add(0.0, variantBoxes.getBoundingBox().max.y - 0.5, 0.0);
		}
		holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(vector3d, rotation));
		holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
		holder.ensureComponent(PhysicsValues.getComponentType());

		UUID uuid = UUID.randomUUID();
		holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
		holder.putComponent(NetworkId.getComponentType(), new NetworkId(ref.getStore().getExternalData().takeNextNetworkId()));

		ModelAsset modelasset = ModelAsset.getAssetMap().getAsset("GoneFishingBobber");
		if (modelasset == null)
			modelasset = ModelAsset.DEBUG;
		Model model = Model.createRandomScaleModel(modelasset);
		holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		if (model.getBoundingBox() == null)
			return;

		holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
		holder.addComponent(Velocity.getComponentType(), new Velocity());
		holder.ensureAndGetComponent(BobberComponent.getComponentType());
		commandBuffer.addEntity(holder, AddReason.SPAWN);

		// Update the fishing rod's metadata to bind it to the spawned bobber
		adjustMetadata(inventory, hotbarSlot, fishingStack, uuid);
	}

	/**
	 * Adjusts the fishing rod's metadata to bind or unbind it from a bobber.
	 *
	 * @param inventory  The player's inventory
	 * @param hotbarSlot The hotbar slot index
	 * @param fishingRod The fishing rod item stack
	 * @param bobberUUID The UUID of the bobber to bind to, or null to unbind
	 */
	private void adjustMetadata(Inventory inventory, byte hotbarSlot, @Nonnull ItemStack fishingRod, @Nullable UUID bobberUUID) {
		ItemStack newRod;
		if (bobberUUID == null) {
			newRod = fishingRod.withMetadata(FishingMetaData.KEY, null);
		} else {
			FishingMetaData fishingMetaData = fishingRod.getFromMetadataOrNull(FishingMetaData.KEY, FishingMetaData.CODEC);
			if (fishingMetaData == null) {
				fishingMetaData = new FishingMetaData();
			}
			fishingMetaData.setBoundBobber(bobberUUID);
			newRod = fishingRod.withMetadata(FishingMetaData.KEYED_CODEC, fishingMetaData);
		}
		inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, fishingRod, newRod);
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
