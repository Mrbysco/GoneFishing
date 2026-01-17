package com.mrbysco.gonefishing.util;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.mrbysco.gonefishing.GoneFishingPlugin;
import com.mrbysco.gonefishing.config.FishingConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FishHelper {
	private static final Random RANDOM = new Random();
	private static final Map<String, Float> FISHES = new HashMap<>();
	private static int minFishingTime = 100;
	private static int maxFishingTime = 600;

	/**
	 * Create the default fish table with predefined weights.
	 *
	 * @return A map of fish identifiers to their weights.
	 */
	public static Map<String, Float> createDefaultFishTable() {
		Map<String, Float> fishTable = new HashMap<>();
		fishTable.put("Fish_Bluegill_Item", 5.0F);
		fishTable.put("Fish_Catfish_Item", 5.0F); // Common
		fishTable.put("Fish_Minnow_Item", 5.0F); // Common
		fishTable.put("Fish_Tang_Blue_Item", 3.0F); // Uncommon
		fishTable.put("Fish_Tang_Chevron_Item", 3.0F); // Uncommon
		fishTable.put("Fish_Tang_Lemon_Peel_Item", 3.0F); // Uncommon
		fishTable.put("Fish_Tang_Sailfin_Item", 3.0F); // Uncommon
		fishTable.put("Fish_Clownfish_Item", 1.5F); // Rare
		fishTable.put("Fish_Pufferfish_Item", 1.5F); // Rare
		fishTable.put("Fish_Trout_Rainbow_Item", 1.5F); // Rare
		fishTable.put("Fish_Salmon_Item", 1.5F); // Rare
		fishTable.put("Fish_Jellyfish_Blue_Item", 0.75F); // Epic
		fishTable.put("Fish_Jellyfish_Cyan_Item", 0.75F); // Epic
		fishTable.put("Fish_Jellyfish_Green_Item", 0.75F); // Epic
		fishTable.put("Fish_Jellyfish_Red_Item", 0.75F); // Epic
		fishTable.put("Fish_Jellyfish_Yellow_Item", 0.75F); // Epic
		fishTable.put("Fish_Jellyfish_Man_Of_War_Item", 0.25F); // Legendary
		fishTable.put("Fish_Crab_Item", 0.25F); // Legendary
		fishTable.put("Fish_Eel_Moray_Item", 0.25F); // Legendary
		fishTable.put("Fish_Frostgill_Item", 0.25F); // Legendary
		fishTable.put("Fish_Lobster_Item", 0.25F); // Legendary
		fishTable.put("Fish_Pike_Item", 0.25F); // Legendary
		fishTable.put("Fish_Piranha_Black_Item", 0.25F); // Legendary
		fishTable.put("Fish_Piranha_Item", 0.25F); // Legendary
		fishTable.put("Fish_Shark_Hammerhead_Item", 5.0F);
		fishTable.put("Fish_Shellfish_Lava_Item", 0.25F); // Legendary
		fishTable.put("Fish_Snapjaw_Item", 0.25F); // Legendary
		fishTable.put("Fish_Trilobite_Black_Item", 0.25F); // Legendary
		fishTable.put("Fish_Trilobite_Item", 0.25F); // Legendary
		fishTable.put("Fish_Whale_Humpback_Item", 0.25F); // Legendary
		return fishTable;
	}

	/**
	 * Setup fishes from config.
	 *
	 * @param config The fishing configuration.
	 */
	public static void setupFishes(FishingConfig config) {
		FISHES.clear();
		FISHES.putAll(config.fishTable);
		GoneFishingPlugin.LOGGER.atInfo().log("Fish table loaded with %s entries.", FISHES.size());

		minFishingTime = config.minFishingTime;
		maxFishingTime = config.maxFishingTime;
	}

	/**
	 * Get a random fish based on defined weights.
	 *
	 * @return The identifier of the randomly selected fish.
	 */
	public static String getRandomFish() {
		if (FISHES.isEmpty()) return "";

		float totalWeight = 0.0F;
		for (Float w : FISHES.values()) {
			totalWeight += w;
		}

		float r = RANDOM.nextFloat() * totalWeight;
		for (Map.Entry<String, Float> entry : FISHES.entrySet()) {
			r -= entry.getValue();
			if (r <= 0.0F) {
				return entry.getKey();
			}
		}

		// Fallback (shouldn't normally reach here due to floating point)
		return FISHES.keySet().stream().findFirst().orElse(null);
	}

	/**
	 * Create a random fish ItemStack.
	 *
	 * @return An ItemStack of a randomly selected fish.
	 */
	public static ItemStack createRandomFish() {
		String fishId = getRandomFish();
		ItemStack fishStack = ItemStack.EMPTY;
		if (fishId.isEmpty()) {
			return fishStack;
		}
		fishStack = InventoryHelper.createItem(fishId);
		if (fishStack == null) {
			return ItemStack.EMPTY;
		}
		return fishStack;
	}

	public static int getTimeUntilCatch() {
		return RANDOM.nextInt((maxFishingTime - minFishingTime) + 1) + minFishingTime;
	}
}
