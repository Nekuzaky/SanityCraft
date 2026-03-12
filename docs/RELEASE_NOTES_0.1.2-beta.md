# SanityCraft 0.1.2-beta - Release Notes

## Overview
`0.1.2-beta` focuses on horror pacing quality, immersion polish, and debugability.
This update hardens the core loop for long sessions and multiplayer while improving atmosphere smoothness.

## Highlights
- New anti-spam horror guard rails (events, packets, particles).
- New `/sanity debug` workflow with actionable live diagnostics.
- Rebalanced `light`, `medium`, and `hardcore` profiles.
- Sanity Afflictions V1 refinement for cleaner low-sanity pressure.
- Ritual Safe Zone V2 feedback (subtle aura + calm pulse cue).
- Smoother fog visuals with reduced line artifacts.
- HUD polish: darker sanity palette + pill badge icon + cleaner label style.

## Stability & Performance
- Added per-player horror event budget per minute.
- Added global horror trigger cooldown window.
- Added packet limiter for hallucination effect payloads.
- Added particle cap for directed hallucination bursts.
- Added client ambience pacing controls to reduce event stacking.

## Gameplay & Feel
- Afflictions now scale more cleanly across low-sanity stages.
- Ritual safe zones now communicate state clearly without visual spam.
- Ambient dread feels smoother and less pattern-revealing.
- Sanity HUD now aligns better with the mod's dark horror tone.

## Debug & Balancing Tools
- New commands:
  - `/sanity debug on`
  - `/sanity debug off`
  - `/sanity debug status`
- Debug output now includes:
  - sanity gain/loss sources
  - contextual multiplier details
  - stage transitions
  - event budget and cooldown state

## Config Notes
New/important pacing keys in `config/sanitycraft.json`:
- `horrorEventsPerMinute`
- `horrorGlobalCooldownMinTicks`
- `horrorGlobalCooldownMaxTicks`
- `maxDirectedParticlesPerBurst`
- `networkEffectPacketsPerMinute`
- `networkEffectMinSpacingTicks`
- `dreadFogEnabled`
- `dreadFogIntensity`
- `dreadFogNightBoost`

## Recommended Post-Update Checks
1. Run `/sanity reloadconfig` once after update.
2. Validate your preferred profile with `/sanity profile <light|medium|hardcore|custom>`.
3. Use `/sanity debug on` during one test session to confirm pacing and event frequency.

## Compatibility
- Minecraft: `1.21.8`
- Loader: Fabric
- Workflow: MCreator-first with safe additive Java extension points
