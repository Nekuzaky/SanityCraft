package net.nekuzaky.sanitycraft.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ServerWorkQueue {
	private static final Collection<Tuple<Runnable, Integer>> WORK_QUEUE = new ConcurrentLinkedQueue<>();
	private static boolean registered;

	private ServerWorkQueue() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
			WORK_QUEUE.forEach(work -> {
				work.setB(work.getB() - 1);
				if (work.getB() <= 0) {
					actions.add(work);
				}
			});
			actions.forEach(action -> action.getA().run());
			WORK_QUEUE.removeAll(actions);
		});
	}

	public static void queue(int ticks, Runnable action) {
		WORK_QUEUE.add(new Tuple<>(action, Math.max(1, ticks)));
	}
}
