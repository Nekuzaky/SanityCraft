# SanityCraft 0.2.0-beta - Release Notes

## Highlights
- Massive horror ambience upgrade: adaptive heartbeat, sound traps, near-miss events, cave mining hallucinations.
- New sanity collapse flow at `0 sanity`: panic visuals, countdown tension, accelerating heartbeat, and timed death.
- New gameplay systems: Ritual Safe Zones, Sanity Journal, Nightmare Sleep variants, Fracture Quests.
- Multiplayer pressure mechanics: Party Stress Link.
- New anti-chaos controls: accessibility sliders, no hard flashes, streamer-safe mode.
- New profile workflow: `/sanity profile <light|medium|hardcore|custom>`.
- Mental Shield Totem support: full sanity restore + temporary hallucination immunity.

## Full Feature Pack
- Contextual sanity decay (`night/rain/underground` multipliers).
- Enhanced sanity HUD + cinematic fog behavior.
- Torch interactions with fear systems (fog repel + dark readability support).
- Stalker Hunt v2 hallucination behavior.
- Bloody Creeper fear explosion (no real damage, pure psychological pressure).
- False UI events and corrupted loot moments (visual/audio deception only).
- Biome personality events and anomaly structure influence.

## Commands
- `/sanity`
- `/sanity get [target]`
- `/sanity set <value> [target]`
- `/sanity add <delta>`
- `/sanity reloadconfig`
- `/sanity profile <light|medium|hardcore|custom>`
- `/sanity journal`
- `/sanity journal clear`

## Config Additions (major)
- `balanceProfile`
- `streamerSafeMode`
- `falseUiEventsEnabled`
- `corruptedLootMomentsEnabled`
- `partyStressLinkEnabled`
- `fractureQuestsEnabled`
- `biomePersonalityEnabled`
- `mentalShieldEnabled`
- `mentalShieldDurationSeconds`
- `zeroSanityDeathEnabled`
- `zeroSanityDeathDelaySeconds`
- Accessibility:
  - `accessibilityVisualIntensity`
  - `accessibilityAudioIntensity`
  - `noHardFlashes`

## Fixes Included
- Packet/resource ID crash fixes.
- Particle safety guards to avoid client packet encoding crashes.
- Mental Shield item ID compatibility fix.
- Texture mip warning fix for `mentalshieldtotem` texture dimensions.
- General Fabric 1.21.8 API compatibility corrections.

## Compatibility
- Minecraft: `1.21.8`
- Loader: Fabric
- Workflow: MCreator-first + safe custom Java extension points

## Known Notes
- Mental Shield requires item ID: `sanitycraft:mentalshieldtotem`.
- Recommended after updating: run `/sanity reloadconfig` once in your dev/test world.
