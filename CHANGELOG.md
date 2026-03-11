# Changelog

All notable changes to this project are documented in this file.

## [0.1.1-beta] - 2026-03-11

### Added
- Contextual sanity decay multipliers (`night`, `rain`, `underground`).
- Reworked sanity HUD (more discreet metallic style).
- Ambient fear fog with cave/night/weather pressure.
- Torch-based fog repelling and torch-in-hand visibility support.
- Dynamic horror ambience director (heartbeat, breath, directional sound traps, pressure stingers).
- Near-miss events (behind-you sprint cues + short edge silhouette).
- Rare cave mining hallucination sequence (multi-hit stone break sounds).
- Bloody Creeper fear explosion (visual/audio only, no player damage, no block damage).
- Camera scare pulse system for high-impact hallucination moments.
- Zero sanity collapse mode:
  - visual panic HUD overlays,
  - countdown,
  - accelerating heartbeat,
  - death after configurable delay.
- Sanity Journal system with commands:
  - `/sanity journal`
  - `/sanity journal clear`
- Ritual Safe Zones (torch ring + pill in hand).
- Nightmare sleep outcomes (replayable variants).
- Paranoia mimic sounds (fake environmental threat cues).
- Mental afflictions at low sanity.
- Stalker Hunt v2 behavior (observe/phase/flank behavior, non-lethal hallucination pressure).
- Accessibility and safety options:
  - `accessibilityVisualIntensity`
  - `accessibilityAudioIntensity`
  - `noHardFlashes`
  - `streamerSafeMode`
- Data-driven profile support:
  - `light`, `medium`, `hardcore`, `custom`
  - command: `/sanity profile <light|medium|hardcore|custom>`
- False UI paranoia events.
- Corrupted loot psychological events (no real loot corruption).
- Biome personality events (biome-specific fear signatures).
- Party stress link (nearby panicked players add sanity pressure in multiplayer).
- Anomaly structure detection hooks (ritual-like destabilization zones).
- Mental Shield Totem support (`sanitycraft:mentalshieldtotem`):
  - restores sanity to max on use,
  - blocks hallucinations for a configurable duration.

### Fixed
- Crash on invalid custom packet resource location initialization.
- Hallucination particle spawn safety (no packet encode crash if particle is missing).
- Sanity persistence behavior after death via configurable respawn sanity.
- Multiple mapping/API compatibility issues for Fabric 1.21.8.
- Mental shield item binding mismatch (`mental_shield_totem` vs `mentalshieldtotem`).
- Item texture mip warning by converting `mentalshieldtotem` texture to power-of-two size.

### Notes
- This beta is focused on atmosphere systems and configurable psychological pressure.
- Most systems are designed to remain MCreator-compatible (additive integration).
