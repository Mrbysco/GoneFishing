package com.mrbysco.gonefishing.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

public class FishingMetaData {
	public static final String KEY = "GoneFishingBoundBobber";
	public static final BuilderCodec<FishingMetaData> CODEC = BuilderCodec.builder(FishingMetaData.class, FishingMetaData::new)
			.append(new KeyedCodec<>(
							"BoundBobber", Codec.UUID_BINARY), (metaData, value) ->
							metaData.boundBobber = value,
					(config) -> config.boundBobber)
			.documentation("The bobber that is bound to the fishing rod").add()
			.build();
	public static final KeyedCodec<FishingMetaData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);
	private UUID boundBobber = null;

	public void setBoundBobber(UUID uuid) {
		this.boundBobber = uuid;
	}

	public UUID getBoundBobber() {
		return this.boundBobber;
	}
}
