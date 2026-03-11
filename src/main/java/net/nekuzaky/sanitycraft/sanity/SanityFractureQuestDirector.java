package net.nekuzaky.sanitycraft.sanity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;

public class SanityFractureQuestDirector {
	private static final Map<UUID, QuestState> STATES = new ConcurrentHashMap<>();

	private SanityFractureQuestDirector() {
	}

	public static void tick(ServerPlayer player, PlayerSanityComponent component, SanityConfig config) {
		if (!config.fractureQuestsEnabled || component.hasHallucinationShield()) {
			return;
		}
		int sanity = component.getSanity();
		if (sanity > Math.max(1, config.fractureQuestTriggerSanity)) {
			return;
		}

		RandomSource random = player.getRandom();
		QuestState state = STATES.computeIfAbsent(player.getUUID(), id -> new QuestState());
		if (state.spawnCooldown > 0) {
			state.spawnCooldown--;
		}

		if (state.type == QuestType.NONE) {
			if (state.spawnCooldown <= 0 && random.nextFloat() < 0.005F) {
				state.type = random.nextBoolean() ? QuestType.STAY_IN_LIGHT : QuestType.LEAVE_THE_CAVE;
				state.progressTicks = 0;
				state.spawnCooldown = random.nextIntBetweenInclusive(900, 1800);
				sendQuestStart(player, state.type);
				SanityJournal.log(player, "A strange objective formed in my head.");
			}
			return;
		}

		if (state.type == QuestType.STAY_IN_LIGHT) {
			BlockPos pos = player.blockPosition();
			boolean inLight = player.level().getBrightness(LightLayer.BLOCK, pos) >= 11 || player.level().canSeeSky(pos);
			if (inLight) {
				state.progressTicks++;
			} else {
				state.progressTicks = Math.max(0, state.progressTicks - 4);
			}
			if (state.progressTicks >= 20 * 20) {
				complete(player, state, config, "I stayed in the light long enough.");
			}
		} else if (state.type == QuestType.LEAVE_THE_CAVE) {
			BlockPos pos = player.blockPosition();
			boolean escaped = player.level().canSeeSky(pos) || pos.getY() > player.level().getSeaLevel();
			if (escaped) {
				complete(player, state, config, "I escaped the cave.");
			}
		}

		if (player.tickCount % 80 == 0) {
			sendQuestHint(player, state);
		}
	}

	private static void complete(ServerPlayer player, QuestState state, SanityConfig config, String journalLine) {
		SanityManager.addSanity(player, Math.max(1, config.fractureQuestReward));
		player.displayClientMessage(Component.literal("Fracture objective complete. Sanity restored."), true);
		SanityJournal.log(player, journalLine);
		state.type = QuestType.NONE;
		state.progressTicks = 0;
	}

	private static void sendQuestStart(ServerPlayer player, QuestType type) {
		if (type == QuestType.STAY_IN_LIGHT) {
			player.displayClientMessage(Component.literal("FRACTURE QUEST: Stay in the light for 20s."), true);
		} else if (type == QuestType.LEAVE_THE_CAVE) {
			player.displayClientMessage(Component.literal("FRACTURE QUEST: Leave the cave now."), true);
		}
	}

	private static void sendQuestHint(ServerPlayer player, QuestState state) {
		if (state.type == QuestType.STAY_IN_LIGHT) {
			int sec = state.progressTicks / 20;
			player.displayClientMessage(Component.literal("Light objective: " + sec + "/20s"), true);
		} else if (state.type == QuestType.LEAVE_THE_CAVE) {
			player.displayClientMessage(Component.literal("Leave the cave to calm your mind."), true);
		}
	}

	public static void clear(ServerPlayer player) {
		STATES.remove(player.getUUID());
	}

	private enum QuestType {
		NONE,
		STAY_IN_LIGHT,
		LEAVE_THE_CAVE
	}

	private static class QuestState {
		private QuestType type = QuestType.NONE;
		private int progressTicks = 0;
		private int spawnCooldown = 600;
	}
}
