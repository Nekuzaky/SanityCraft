package com.sanitycraft.sanity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class SanitySavedData extends SavedData {
	private static final Codec<SanitySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("legacyMigrated", false).forGetter(SanitySavedData::legacyMigrated),
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT).optionalFieldOf("players", Map.of()).forGetter(SanitySavedData::playersView))
			.apply(instance, SanitySavedData::new));

	public static final SavedDataType<SanitySavedData> TYPE = new SavedDataType<>(
			"sanitycraft_sanity",
			context -> new SanitySavedData(false, Map.of()),
			context -> CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Map<UUID, Integer> playerSanity;
	private boolean legacyMigrated;

	private SanitySavedData(boolean legacyMigrated, Map<UUID, Integer> playerSanity) {
		this.legacyMigrated = legacyMigrated;
		this.playerSanity = new HashMap<>(playerSanity);
	}

	public boolean legacyMigrated() {
		return legacyMigrated;
	}

	public int get(UUID playerId) {
		return SanityThresholds.clamp(playerSanity.getOrDefault(playerId, SanityThresholds.DEFAULT_SANITY));
	}

	public void set(UUID playerId, int sanity) {
		int clamped = SanityThresholds.clamp(sanity);
		Integer previous = playerSanity.put(playerId, clamped);
		if (previous == null || previous.intValue() != clamped) {
			setDirty();
		}
	}

	public void importLegacy(Map<UUID, Integer> legacyValues) {
		boolean changed = false;
		for (Map.Entry<UUID, Integer> entry : legacyValues.entrySet()) {
			int clamped = SanityThresholds.clamp(entry.getValue());
			Integer previous = playerSanity.put(entry.getKey(), clamped);
			if (previous == null || previous.intValue() != clamped) {
				changed = true;
			}
		}
		if (changed) {
			setDirty();
		}
	}

	public void markLegacyMigrated() {
		if (!legacyMigrated) {
			legacyMigrated = true;
			setDirty();
		}
	}

	private Map<UUID, Integer> playersView() {
		return playerSanity;
	}
}
