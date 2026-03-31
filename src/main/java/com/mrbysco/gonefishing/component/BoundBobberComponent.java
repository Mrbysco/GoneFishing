package com.mrbysco.gonefishing.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mrbysco.gonefishing.GoneFishingPlugin;

import java.util.UUID;

public class BoundBobberComponent implements Component<EntityStore> {
	public static final BuilderCodec<BoundBobberComponent> CODEC = BuilderCodec.builder(BoundBobberComponent.class, BoundBobberComponent::new)
			.append(new KeyedCodec<>("BoundEntity", Codec.UUID_BINARY),
					(component, attachedEntity) -> component.attachedEntity = attachedEntity,
					(component) -> component.attachedEntity).add()
			.build();
	private UUID attachedEntity;

	public static ComponentType<EntityStore, BoundBobberComponent> getComponentType() {
		return GoneFishingPlugin.get().getBoundBobberComponent();
	}

	private BoundBobberComponent() {
	}

	public BoundBobberComponent(UUID attachedEntity) {
		this.attachedEntity = attachedEntity;
	}

	public void setAttachedEntity(UUID attachedEntity) {
		this.attachedEntity = attachedEntity;
	}

	public UUID getAttachedEntity() {
		return attachedEntity;
	}

	public Component<EntityStore> clone() {
		return new BoundBobberComponent(this.attachedEntity);
	}
}
