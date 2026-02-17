package com.mrbysco.gonefishing;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.mrbysco.gonefishing.component.BobberComponent;
import com.mrbysco.gonefishing.component.BoundBobberComponent;
import com.mrbysco.gonefishing.config.FishingConfig;
import com.mrbysco.gonefishing.interaction.FishingInteraction;
import com.mrbysco.gonefishing.interaction.SpawnFishInteraction;
import com.mrbysco.gonefishing.systems.BobberDespawnSystem;
import com.mrbysco.gonefishing.systems.BobberSystem;
import com.mrbysco.gonefishing.util.FishHelper;

import javax.annotation.Nonnull;

public class GoneFishingPlugin extends JavaPlugin {

	public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

	private static GoneFishingPlugin instance;

	private ComponentType<EntityStore, BobberComponent> bobberComponent;
	private ComponentType<EntityStore, BoundBobberComponent> boundBobberComponent;
	private final Config<FishingConfig> config;

	public GoneFishingPlugin(@Nonnull JavaPluginInit init) {
		super(init);
		instance = this;
		LOGGER.atInfo().log("Initializing Gone Fishing Plugin");
		this.config = this.withConfig("GoneFishingConfig", FishingConfig.CODEC);
	}

	public static GoneFishingPlugin get() {
		return instance;
	}

	@Override
	protected void setup() {
		LOGGER.atInfo().log("Setting up Bobber component");
		this.bobberComponent = this.getEntityStoreRegistry().registerComponent(BobberComponent.class, BobberComponent::new);
		this.boundBobberComponent = this.getEntityStoreRegistry().registerComponent(BoundBobberComponent.class, "GoneFishing_BoundBobber", BoundBobberComponent.CODEC);
		LOGGER.atInfo().log("Registering Fishing Interaction");
		this.getCodecRegistry(Interaction.CODEC).register("GoneFishingFish", FishingInteraction.class, FishingInteraction.CODEC);
		this.getCodecRegistry(Interaction.CODEC).register("GoneFishing_Spawn_Fish", SpawnFishInteraction.class, SpawnFishInteraction.CODEC);
		LOGGER.atInfo().log("Registering Bobber Systems");
		this.getEntityStoreRegistry().registerSystem(new BobberSystem());
		this.getEntityStoreRegistry().registerSystem(new BobberDespawnSystem());
	}

	@Override
	protected void start() {
		super.start();
		this.config.save();
		FishingConfig config = this.config.get();
		FishHelper.setupFishes(config);
	}

	public ComponentType<EntityStore, BobberComponent> getBobberComponent() {
		return bobberComponent;
	}

	public ComponentType<EntityStore, BoundBobberComponent> getBoundBobberComponent() {
		return boundBobberComponent;
	}
}