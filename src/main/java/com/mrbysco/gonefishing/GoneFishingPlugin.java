package com.mrbysco.gonefishing;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.mrbysco.gonefishing.component.BobberComponent;
import com.mrbysco.gonefishing.config.FishingConfig;
import com.mrbysco.gonefishing.interaction.FishingInteraction;
import com.mrbysco.gonefishing.systems.BobberSystem;
import com.mrbysco.gonefishing.util.FishHelper;

import javax.annotation.Nonnull;

public class GoneFishingPlugin extends JavaPlugin {

	public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
	public static ComponentType<EntityStore, BobberComponent> bobberComponent;
	private final Config<FishingConfig> config;

	public GoneFishingPlugin(@Nonnull JavaPluginInit init) {
		super(init);
		LOGGER.atInfo().log("Initializing Gone Fishing Plugin");
		this.config = this.withConfig("GoneFishingConfig", FishingConfig.CODEC);
	}

	@Override
	protected void shutdown() {

	}

	@Override
	protected void setup() {
		LOGGER.atInfo().log("Setting up Bobber component");
		bobberComponent = this.getEntityStoreRegistry().registerComponent(BobberComponent.class, BobberComponent::new);
		LOGGER.atInfo().log("Registering Fishing Interaction");
		this.getCodecRegistry(Interaction.CODEC).register("GoneFishingFish", FishingInteraction.class, FishingInteraction.CODEC);
		LOGGER.atInfo().log("Registering Bobber System");
		this.getEntityStoreRegistry().registerSystem(new BobberSystem());

	}

	@Override
	protected void start() {
		super.start();
		FishingConfig config = this.config.get();
		FishHelper.setupFishes(config);
	}
}