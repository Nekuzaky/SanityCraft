# Phase 1 Audit: SanityCraft Fabric Horror Migration

## Scope

This phase is intentionally architecture-first. No runtime behavior changes are made here.

Goals of this document:

- audit the current project state
- identify preserved SanityCraft content
- identify generated or unsafe legacy patterns that should be replaced
- define the target production architecture
- define the migration order for controlled implementation
- define the first foundation files for Phase 2

## Current Project State

## Good Foundation Already Present

The project is already a standalone Fabric mod and no longer requires MCreator to build.

Confirmed positives:

- Fabric Loom setup is already in place in `build.gradle`
- Fabric entrypoints already exist in `fabric.mod.json`
- existing content is already registered and running under Fabric
- sanity is already tracked per player on the server
- there is already a sync path from server to client
- the mod already contains preserved identity content:
  - `pill`
  - `mentalshieldtotem`
  - `stalker`
  - `bloody_creeper`
  - `blood` particle
  - `/sanity` command
  - SanityCraft creative tab

This is not a greenfield rewrite. It is a structural migration and systems redesign built on an existing Fabric base.

## Structural Problems To Fix

The current codebase works, but it is still carrying several prototype or generated-style patterns that will become a maintenance problem as the horror systems expand.

Primary issues:

1. Static global orchestration is doing too much.
   - `sanity/SanityManager.java` owns ticking, persistence writes, sync, recovery, journal hooks, safe zone feedback, debug output, and party stress logic.

2. Per-player runtime state is overloaded.
   - `sanity/PlayerSanityComponent.java` stores sanity, update timers, audio cooldowns, hallucination cooldowns, false UI cooldowns, ritual timers, event budget state, and shield state in one object.

3. Threshold logic is inconsistent with the new design target.
   - `sanity/SanityStageResolver.java` currently uses `81/61/41/21`.
   - `client/SanityHudRenderer.java` uses a slightly different presentation.
   - the requested production thresholds are:
     - `100-76 Stable`
     - `75-51 Uneasy`
     - `50-26 Disturbed`
     - `25-11 Fractured`
     - `10-0 Collapse`

4. Persistence is safe but too expensive for long-term use.
   - `sanity/SanityPersistence.java` writes a world JSON file on each state update through `SanityPersistence.set(...)`.
   - `sanity/SanityManager.tick(...)` calls that path whenever sanity changes.
   - this is acceptable for a prototype, not for a production-like gameplay system that updates continuously.

5. Config is monolithic.
   - `sanity/SanityConfig.java` is a large manual JSON reader/writer and currently acts as the central settings dump for unrelated features.

6. Client horror logic is too centralized.
   - `client/HorrorAmbienceDirector.java`
   - `client/HorrorUiOverlays.java`
   - `client/SanityClientState.java`
   These hold multiple unrelated responsibilities that should be split into focused services.

7. Server-side horror logic is split across multiple ad hoc directors without a formal event architecture.
   - `sanity/SanityNarrativeDirector.java`
   - `sanity/SanityStalkerHuntDirector.java`
   - `effect/SanityEffects.java`
   - `sanity/HallucinationSoundManager.java`
   There is useful behavior here, but no clean director model, no unified event registry, and no clear separation between fake events, client deception, and real threats.

8. Legacy callback scaffolding still exists.
   - `event/*`
   - `mixin/PlayerMixin.java`
   - `mixin/LivingEntityMixin.java`
   - `mixin/BlockItemMixin.java`
   - `mixin/ItemStackMixin.java`
   - `mixin/CommandsMixin.java`
   These imitate generated event hooks instead of using direct Fabric lifecycle or gameplay callbacks wherever possible.

9. Some mixin-era files appear stale or disconnected.
   - several client mixin classes exist on disk but are not declared in `src/main/resources/sanitycraft.mixins.json`
   - `META-INF/sanitycraft.accesswidener` exposes fields that do not appear to be part of the current sanity foundation

10. Existing hostile entities do not yet match the requested horror behavior target.
    - `entity/StalkerEntity.java`
    - `entity/BloodyCreeperEntity.java`
    Both are still close to generic melee monsters, even though the surrounding hallucination logic is already stronger than the base AI.

## Preserved Content Inventory

## Direct Reuse

These assets or identifiers should be preserved directly if possible:

- Mod id: `sanitycraft`
- Item ids:
  - `sanitycraft:pill`
  - `sanitycraft:mentalshieldtotem`
  - `sanitycraft:stalker_spawn_egg`
  - `sanitycraft:bloody_creeper_spawn_egg`
- Entity ids:
  - `sanitycraft:stalker`
  - `sanitycraft:bloody_creeper`
- Particle id:
  - `sanitycraft:blood`
- Painting id:
  - `sanitycraft:sanity_paint`
- Existing texture assets:
  - item textures
  - entity textures
  - particle texture
  - logo
  - painting texture
- Existing source art:
  - `art/blockbench/*.bbmodel`
  - `art/photoshop/*.psd`
- Existing lang keys and naming direction
- Existing recipes for pill and mental shield

These ids should remain stable unless there is a strong compatibility reason to break them.

## Reuse As Reference, Not Drop-In

These systems contain valuable ideas or behavior, but should be refactored into the new architecture instead of copied as-is:

- `sanity/SanityEnvironmentHelper.java`
  - good initial stressor list
  - rewrite into a throttled context sampler and trigger calculator

- `sanity/SanityCalculator.java`
  - usable conceptual basis for decay/recovery math
  - rewrite into modular source-based modifiers

- `sanity/SanityJournal.java`
  - good preserved flavor system
  - should become a contained subsystem, not a side effect scattered through managers

- `sanity/SanityStalkerHuntDirector.java`
  - contains useful stalking behavior ideas
  - should be decomposed into apparition behavior controllers and entity goals

- `sanity/HallucinationSoundManager.java`
  - useful sound palette seed
  - should become part of a weighted fake audio registry

- `util/ServerWorkQueue.java`
  - useful scheduling utility
  - keep if still needed, but move to a more neutral runtime utility package

- `item/SanityPillItem.java`
  - preserve item identity
  - rewrite mechanics around new sanity/recovery rules and side effects

- `item/MentalShieldItem.java`
  - preserve item identity and emergency stabilization direction
  - rewrite effect handling against the new protection/safe-zone model

## Rewrite Required

These should be replaced rather than extended further:

- root package layout under `net.nekuzaky.sanitycraft`
- current static `SanityManager`
- current overloaded `PlayerSanityComponent`
- current `SanityConfig` monolith
- current direct JSON persistence strategy
- current ad hoc horror-event triggering flow
- legacy generated-style event scaffolding in `event/*`
- mixin-based event relay where Fabric callbacks can replace it
- current entity AI classes for stalker-grade behavior
- current client mega-classes for overlay/audio/deception

## Safe Temporary Stubs

The following can exist as temporary shells during migration as long as they compile cleanly and are clearly marked:

- ritual or safe-zone registries before custom blocks are added
- apparition registries before advanced AI goals exist
- world integration registries before biome/structure content is implemented
- profile/config sections that exist before every system is wired

Stubs are acceptable only when:

- they do not fake gameplay completeness
- they do not expose broken or misleading behavior
- they have a clear future hook

## Target Production Architecture

Target root package:

- `com.sanitycraft`

Target structure for the migration:

```text
com.sanitycraft
|- SanityCraft.java
|- client
|  |- SanityCraftClient.java
|  |- hud
|  |- render
|  |- screenfx
|  |- sound
|  `- particles
|- registry
|  |- ModBlocks.java
|  |- ModCommands.java
|  |- ModCreativeTabs.java
|  |- ModEffects.java
|  |- ModEntities.java
|  |- ModGameRules.java
|  |- ModItems.java
|  |- ModParticles.java
|  `- ModSounds.java
|- sanity
|  |- SanityComponent.java
|  |- SanityManager.java
|  |- SanityCalculator.java
|  |- SanityPersistence.java
|  |- SanityThresholds.java
|  |- SanityRecovery.java
|  |- SanityEvents.java
|  |- SanityTriggers.java
|  |- SanityDifficultyProfile.java
|  `- SanityDebug.java
|- horror
|  |- HorrorEventManager.java
|  |- HorrorEventSelector.java
|  |- HorrorBudgetSystem.java
|  |- HorrorCooldowns.java
|  |- HorrorWeights.java
|  |- EnvironmentalManipulation.java
|  |- FakeAudioSystem.java
|  |- FalseFeedbackSystem.java
|  |- HallucinationManager.java
|  |- ApparitionSpawner.java
|  `- ThreatEscalationSystem.java
|- entity
|  |- ai
|  |- client
|  |- custom
|  |- goal
|  `- spawn
|- item
|  |- consumable
|  `- custom
|- block
|  |- ambience
|  |- custom
|  `- ritual
|- effect
|  |- custom
|  `- status
|- command
|  |- admin
|  |- debug
|  `- gameplay
|- network
|  |- handler
|  |- packet
|  `- sync
|- world
|  |- ambient
|  |- biome
|  |- rules
|  `- structure
|- data
|  |- config
|  |- loot
|  `- profile
`- util
   |- debug
   |- math
   |- random
   |- registry
   |- text
   `- time
```

## Architecture Rules For The Migration

1. Keep server authority for real sanity state and real hostile manifestations.
2. Keep fake UI and fake audiovisual deception client-private unless server ownership is needed for pacing.
3. Preserve resource ids and naming wherever possible.
4. Split orchestration from data.
5. Split real-danger logic from hallucination logic.
6. Prefer explicit services over giant static helper classes.
7. Prefer Fabric lifecycle callbacks over generic relay mixins where possible.
8. Keep each subsystem independently removable or testable.

## Proposed Runtime Boundaries

### `sanity`

Owns:

- authoritative sanity value
- per-player runtime sanity state
- threshold evaluation
- stress and recovery calculations
- persistence
- sync decisions
- sanity debug commands and diagnostics

Does not own:

- audio hallucination selection
- visual distortion rendering
- apparition spawning policies
- environmental anomaly presentation

### `horror`

Owns:

- contextual event selection
- cooldown and budget enforcement
- hallucination category weighting
- fake versus real event distinction
- escalation logic by threshold and environment

Does not own:

- persistent sanity values
- core player save logic
- raw registry bootstrap

### `client`

Owns:

- HUD
- screen distortion
- fake UI presentation
- fake audio presentation
- client-only apparition visuals
- render hooks

Does not own:

- real sanity authority
- spawn decisions for real threats
- permanent gameplay state

### `network`

Owns:

- packet ids and codecs
- sync throttling
- client-private horror effect dispatch

Rule:

- only send what the client must know
- do not broadcast private hallucinations to unrelated players

## Migration Strategy

## Recommendation: Parallel Package Migration, Not In-Place Surgery

Do not try to rewrite the existing `net.nekuzaky.sanitycraft` package in place all at once.

Recommended approach:

1. create the new `com.sanitycraft` root package and new entrypoints
2. move only the lowest-risk foundation first
3. preserve old content behind temporary adapters where useful
4. retire old packages only after the new path is proven

This reduces the chance of breaking all content at once.

## Recommended Implementation Order

### Phase 2: Foundation

Build the new spine first:

1. new root package and entrypoints
2. common registry bootstrap
3. creative tab
4. basic item registration
5. config skeleton
6. new sanity package skeleton
7. new command skeleton
8. networking skeleton for sanity sync

At the end of Phase 2:

- the mod boots from the new package
- core registries compile from the new architecture
- at least one preserved item is registered through the new path
- `/sanity get` style debug command exists through the new path

### Phase 3: Sanity Core

1. implement authoritative per-player sanity state
2. implement persistence with dirty-save behavior
3. implement sync throttling
4. implement new thresholds
5. implement controlled environmental stress and recovery sampling

### Phase 4: Horror Director

1. formalize horror event definitions
2. add budget and cooldown services
3. port fake audio into a weighted event system
4. port false UI into a dedicated client-safe system
5. add screen effect hooks

### Phase 5: Manifestations And Protection

1. integrate preserved stalker and bloody creeper content into the new horror framework
2. replace generic AI with stalking or apparition behaviors
3. add safe-zone and ritual protection architecture
4. expand items into proper recovery/protection tools
5. add environmental anomalies through controlled event hooks

### Phase 6: Polish

1. optimize scans and packet flow
2. remove obsolete legacy packages
3. update README and migration notes
4. add debug workflow and testing checklist

## First Foundation Files For Phase 2

These are the first files to create or change.

## Create

- `src/main/java/com/sanitycraft/SanityCraft.java`
  - new server/common mod bootstrap

- `src/main/java/com/sanitycraft/client/SanityCraftClient.java`
  - new client bootstrap

- `src/main/java/com/sanitycraft/registry/ModItems.java`
- `src/main/java/com/sanitycraft/registry/ModEntities.java`
- `src/main/java/com/sanitycraft/registry/ModParticles.java`
- `src/main/java/com/sanitycraft/registry/ModCreativeTabs.java`
- `src/main/java/com/sanitycraft/registry/ModCommands.java`
- `src/main/java/com/sanitycraft/registry/ModBlocks.java`
- `src/main/java/com/sanitycraft/registry/ModEffects.java`
- `src/main/java/com/sanitycraft/registry/ModSounds.java`
- `src/main/java/com/sanitycraft/registry/ModGameRules.java`

- `src/main/java/com/sanitycraft/data/config/SanityCraftConfig.java`
- `src/main/java/com/sanitycraft/data/profile/DifficultyProfiles.java`

- `src/main/java/com/sanitycraft/sanity/SanityComponent.java`
- `src/main/java/com/sanitycraft/sanity/SanityManager.java`
- `src/main/java/com/sanitycraft/sanity/SanityCalculator.java`
- `src/main/java/com/sanitycraft/sanity/SanityPersistence.java`
- `src/main/java/com/sanitycraft/sanity/SanityThresholds.java`
- `src/main/java/com/sanitycraft/sanity/SanityRecovery.java`
- `src/main/java/com/sanitycraft/sanity/SanityEvents.java`
- `src/main/java/com/sanitycraft/sanity/SanityTriggers.java`
- `src/main/java/com/sanitycraft/sanity/SanityDifficultyProfile.java`
- `src/main/java/com/sanitycraft/sanity/SanityDebug.java`

- `src/main/java/com/sanitycraft/network/packet/ClientboundSanitySyncPacket.java`
- `src/main/java/com/sanitycraft/network/sync/SanitySyncService.java`
- `src/main/java/com/sanitycraft/network/handler/ClientPacketHandlers.java`

- `src/main/java/com/sanitycraft/command/debug/SanityCommand.java`

- `src/main/java/com/sanitycraft/item/consumable/PillItem.java`

## Change

- `src/main/resources/fabric.mod.json`
  - switch entrypoints to the new package

- `build.gradle`
  - only if package or metadata cleanup is needed for the new root bootstrap

- `gradle.properties`
  - optional if the project group should move from `net.nekuzaky` to `com.sanitycraft`

- `src/main/resources/assets/sanitycraft/lang/en_us.json`
  - extend keys, but preserve existing names

Do not change ids in recipes, lang keys, or assets unless necessary.

## Systems To Keep Out Of Phase 2

Do not expand these yet:

- advanced worldgen
- major mixin cleanup
- new custom blocks with gameplay logic
- advanced apparition AI
- full horror event director
- screen-fx overhaul
- safe-zone block mechanics

Phase 2 should prove the new foundation, not simulate completeness.

## Migration Risks

1. Package migration can silently break entrypoints.
2. Changing registry ids would break saves and recipes.
3. Removing legacy mixins too early can break the current runtime before the replacement callbacks exist.
4. Rebuilding sanity persistence incorrectly can lose per-player state.
5. Pulling client-only effects into common code can create multiplayer leakage or desync.
6. Porting entity logic too early will slow the foundation phase.

## Phase 1 Deliverable Summary

### Files Added/Changed

- Added: `docs/PHASE_1_AUDIT.md`

### Why

- freeze the migration architecture before foundation work starts
- explicitly preserve SanityCraft identity content
- identify which current systems can be reused and which must be replaced

### Code

- no runtime code changes in Phase 1 by design

### Risks

- none to runtime; this phase is documentation only

### TODOs

- implement Phase 2 from the file list above
- keep legacy runtime intact until the new bootstrap compiles and runs

### Test Steps

- verify this document matches the repo state
- use it as the checklist for Phase 2 implementation
