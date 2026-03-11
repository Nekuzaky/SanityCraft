package net.nekuzaky.sanitycraft.sanity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

public class SanityJournal {
	private static final int MAX_ENTRIES = 24;
	private static final Map<UUID, Deque<String>> ENTRIES = new ConcurrentHashMap<>();

	private SanityJournal() {
	}

	public static void log(ServerPlayer player, String line) {
		if (player == null || line == null || line.isBlank()) {
			return;
		}
		Deque<String> deque = ENTRIES.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
		if (deque.size() >= MAX_ENTRIES) {
			deque.removeFirst();
		}
		deque.addLast(line);
	}

	public static List<String> recent(ServerPlayer player, int limit) {
		Deque<String> deque = ENTRIES.get(player.getUUID());
		if (deque == null || deque.isEmpty()) {
			return List.of();
		}
		int max = Math.max(1, limit);
		List<String> all = new ArrayList<>(deque);
		int from = Math.max(0, all.size() - max);
		return all.subList(from, all.size());
	}

	public static void clear(ServerPlayer player) {
		ENTRIES.remove(player.getUUID());
	}
}
