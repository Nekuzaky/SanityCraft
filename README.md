# SanityCraft

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-3C8527?style=for-the-badge)
![Fabric](https://img.shields.io/badge/Loader-Fabric-AEC9C1?style=for-the-badge)
![Fabric API](https://img.shields.io/badge/Fabric_API-0.133.4%2B1.21.8-AEC9C1?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

Psychological survival horror for Minecraft Java Edition (Fabric).

SanityCraft introduces a per-player sanity system that reacts to environmental pressure:
darkness, storms, hostile mobs, and isolation slowly break your mind.

## Download
- Project page: https://www.curseforge.com/minecraft/mc-mods/sanitycraft
- Install page: https://www.curseforge.com/minecraft/mc-mods/sanitycraft/install

## Design Goals
- Atmosphere over cheap jumpscares
- Progressive mental breakdown
- Multiplayer-safe sanity logic
- Standalone Fabric workflow with maintainable Java systems
- Future-ready architecture for visual hallucinations

## Current Features (v0.1 Foundation)
- Persistent sanity value per player (`0..100`, default `100`)
- Server-side sanity update loop with configurable interval
- Sanity loss in:
  - darkness
  - caves
  - nearby hostile mobs
  - thunderstorms
  - deep-dark-like conditions
- Sanity recovery in:
  - bright/safe areas
  - villages
  - music proximity
  - sleep
- Stage-based states:
  - `81-100`: Stable
  - `61-80`: Mild discomfort
  - `41-60`: Uneasy
  - `21-40`: Unstable
  - `0-20`: Severe breakdown
- Audio-first hallucination framework
- Narrative paranoia events (whispers and false cues)
- Ambient horror director:
  - adaptive heartbeat and pressure stingers
  - directional sound traps
  - rare near-miss events (behind-you sprint cues + 0.3s edge silhouette)
  - rare cave miner hallucinations (multi-hit stone-breaking sequence)
- Stability/performance guard rails:
  - global horror-event anti-spam cooldown
  - per-minute horror event budget
  - packet limiter for jumpscare/scare-pulse networking
  - particle burst clamping
- Contextual ambient fog:
  - cave/night/weather pressure
  - torches repel fog
- Torch hand support (maintains visual readability in darkness)
- Bloody Creeper hallucination encounters
- Bloody Creeper fear explosion (sound + particles + camera pulse, no damage, no block break)
- Zero-sanity collapse mode:
  - blinking heart panic layer (visual only)
  - fake durability flicker (visual only, no real durability change)
  - collapse countdown + accelerating heartbeat
  - death after configured delay (default `30s`)
- Sanity Journal system:
  - automatic mental-log entries from key events
  - `/sanity journal` and `/sanity journal clear`
- Ritual Safe Zones:
  - torch circle + Pill in hand stabilizes sanity
  - V2 aura feedback (subtle particles + calm pulse sound)
- Sanity Afflictions V1:
  - short breath pressure (light exhaustion pulses)
  - light tremor cues
  - soft disorientation/slowdown by low-sanity stages
- Nightmare Sleep Events:
  - low-sanity sleep can trigger replayable nightmare outcomes
  - safer sleep gives minor recovery
- Mental Fracture Quests:
  - irrational mini-objectives (stay in light, leave cave)
  - sanity rewards on completion
- False UI Events:
  - fake system warnings, fake ping spikes, fake advancement messages
- Biome Personalities:
  - biome-dependent fear signatures (sound + journal tone)
- Corrupted Loot Moments:
  - chest/inventory paranoia moments (psychological only, no loot corruption)
- Party Stress Link (multiplayer):
  - nearby low-sanity players increase mental pressure
- Anomaly Structures:
  - ritual-like structures can destabilize sanity when nearby
- Mental Shield Totem:
  - consumable emergency item
  - instantly restores sanity to max
  - blocks hallucinations temporarily
- Data-driven profiles:
  - `light`, `medium`, `hardcore`, `custom`
- Accessibility controls:
  - visual intensity
  - audio intensity
  - `noHardFlashes` mode
  - `streamerSafeMode` option
- Debug command toolkit: `/sanity`

## Tech Stack
- Minecraft Java Edition `1.21.8`
- Fabric Loader `0.17.2+`
- Fabric API `0.133.4+1.21.8`
- Java `21`
- Fabric Loom

## Standalone Fabric Workflow
This project now builds as a normal standalone Fabric mod.

- Registries live in `net.nekuzaky.sanitycraft.registry.*`
- Client-only bootstrap lives in `net.nekuzaky.sanitycraft.client.*`
- Commands live in `net.nekuzaky.sanitycraft.command.*`
- Runtime sanity systems live in `net.nekuzaky.sanitycraft.sanity.*` and `net.nekuzaky.sanitycraft.effect.*`
- Reusable runtime assets stay in `src/main/resources/**`
- Source art references live in `art/**`

Migration references:
- `docs/MIGRATION.md`
- `docs/PROJECT_TREE.md`
- `.vscode/launch.json`
- `.vscode/tasks.json`

## Project Structure
- `sanity/PlayerSanityComponent` - per-player runtime state and cooldowns
- `sanity/SanityManager` - orchestration and tick update entry point
- `sanity/SanityEnvironmentHelper` - environment checks
- `sanity/SanityCalculator` - sanity delta calculation
- `sanity/SanityStageResolver` - stage mapping
- `effect/SanityEffects` - stage-driven hallucinations and afflictions
- `sanity/HallucinationSoundManager` - stage-driven audio events
- `sanity/SanityPersistence` - player sanity save/load
- `sanity/SanityEvents` - event wiring (join/disconnect/copy/tick)
- `sanity/SanityFractureQuestDirector` - irrational objective system
- `sanity/SanityJournal` - per-player mental event log
- `registry/ModItems`, `registry/ModEntities`, `registry/ModParticles`, `registry/ModCreativeTabs` - content registration
- `command/SanityCommand` - `/sanity` toolkit
- `item/SanityPillItem`, `item/MentalShieldItem` - consumable item behavior
- `util/ServerWorkQueue` - delayed server work scheduler
- `client/SanityHudRenderer` - sanity HUD rendering
- `client/HorrorUiOverlays` - fear overlays, silhouette flashes, collapse visuals
- `client/HorrorAmbienceDirector` - dynamic horror ambience and rare scripted sound events

## Configuration
Generated at:
- `config/sanitycraft.json`

Main options include:
- sanity gain/loss rates and update interval
- contextual sanity decay multipliers (night/rain/underground)
- hallucination and narrative toggles
- false UI event tuning
- biome personality tuning
- corrupted loot moment tuning
- fracture quest tuning
- mental shield duration
- streamer safe mode toggle
- party stress link tuning
- stalker and bloody creeper hallucination tuning
- ambience director toggles and master volume
- near-miss rarity windows
- cave-mining hallucination rarity windows
- ambient fog tuning
- torch fog-repel and torch-hand-light tuning
- zero-sanity death toggle and delay seconds
- horror anti-spam budget and cooldown settings
- packet limiter settings for hallucination effects
- directed particle cap per event burst
- ritual safe zone tuning
- nightmare sleep tuning
- paranoia mimic sound tuning
- accessibility safety toggles
- profile-based balancing via `balanceProfile`

## Debug Commands
Requires permission level 2 (operator).

- `/sanity`
- `/sanity get [target]`
- `/sanity set <value> [target]`
- `/sanity add <delta>`
- `/sanity reloadconfig`
- `/sanity profile <light|medium|hardcore|custom>`
- `/sanity debug <on|off|status>`
- `/sanity journal`
- `/sanity journal clear`

## Running in Development
1. Install Java 21
2. On Windows, `gradlew.bat` will auto-detect a local JDK 21 in common locations if your global `JAVA_HOME` or `java` still points to an older version
3. Run:
```bash
./gradlew runClient
```

## VS Code
- `Run and Debug` -> `Fabric Client` launches the client from VS Code
- `Run and Debug` -> `Fabric Client (Debug)` starts the client suspended on port `5005` and attaches the Java debugger
- `Terminal` -> `Run Task` -> `fabric-run-client` also works if you just want a one-click launch task

## Roadmap
- [ ] Data-driven profile presets (Light/Medium/Hardcore)
- [ ] More biome-aware paranoia events
- [ ] Expanded stalker hunt behaviors
- [ ] Optional post-processing layer with strict performance cap
- [ ] Packaging and release pipeline (Modrinth/CurseForge)

## License
MIT
