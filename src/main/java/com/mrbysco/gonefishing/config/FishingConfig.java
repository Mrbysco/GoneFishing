package com.mrbysco.gonefishing.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.mrbysco.gonefishing.util.FishHelper;

import java.util.HashMap;
import java.util.Map;

public class FishingConfig {
	public static final BuilderCodec<FishingConfig> CODEC = BuilderCodec.builder(
					FishingConfig.class, FishingConfig::new
			)
			.append(
					new KeyedCodec<>("FishTable", new MapCodec<>(Codec.FLOAT, HashMap::new)),
					(itemArmor, map) -> itemArmor.fishTable = map,
					itemArmor -> itemArmor.fishTable
			)
			.documentation("A table of fish IDs and their corresponding weights for fishing.").add()
			.append(new KeyedCodec<>(
							"MinFishingTime", Codec.INTEGER), (config, value) ->
							config.minFishingTime = value,
					(config) -> config.minFishingTime)
			.documentation("The minimum time (in ticks) required for a fish to hook onto the line. [default: 100]").add()
			.append(new KeyedCodec<>(
							"MaxFishingTime", Codec.INTEGER), (config, value) ->
							config.maxFishingTime = value,
					(config) -> config.maxFishingTime)
			.documentation("The maximum time (in ticks) before a fish must hook onto the line. [default: 600]").add()
			.append(new KeyedCodec<>(
							"CanRelease", Codec.BOOLEAN), (config, value) ->
							config.canRelease = value,
					(config) -> config.canRelease)
			.documentation("Enable right-click to release a caught fish [default: true]").add()
			.build();


	public Map<String, Float> fishTable = FishHelper.createDefaultFishTable();
	public int minFishingTime = 100;
	public int maxFishingTime = 600;
	public boolean canRelease = true;
}
