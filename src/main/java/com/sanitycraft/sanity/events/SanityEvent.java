package com.sanitycraft.sanity.events;

public interface SanityEvent {
	String id();

	float triggerChance();

	int cooldownTicks();

	int sanityRequirement();

	int durationTicks();

	default int budgetCost() {
		return 1;
	}

	default boolean canTrigger(SanityEventContext context) {
		return context.sanity() <= sanityRequirement();
	}

	boolean trigger(SanityEventContext context);
}
