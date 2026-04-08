# Runtime Cutover Checklist

This checklist exists to prevent a blind switch from the live `net.nekuzaky.sanitycraft` runtime to the new `com.sanitycraft` runtime.

Current status:

- `fabric.mod.json` still points to the legacy bootstrap
- the new `com.sanitycraft` sanity core now compiles, persists, ticks, and syncs in parallel
- the new runtime is not ready to become active yet

## New Systems Ready In Parallel

- `com.sanitycraft.data.config.SanityCraftConfig`
  - structured config sections
  - threshold, decay, recovery, and sync tuning fields

- `com.sanitycraft.sanity.SanityPersistence`
  - real world-backed persistence via `SavedData`
  - migration path from the legacy `data/sanitycraft_sanity.json`

- `com.sanitycraft.sanity.SanityManager`
  - authoritative per-player state owner for the new runtime

- `com.sanitycraft.sanity.SanityTickService`
  - deterministic server-side sanity ticking

- `com.sanitycraft.sanity.SanityThresholds`
  - production threshold model:
    - `100-76 Stable`
    - `75-51 Uneasy`
    - `50-26 Disturbed`
    - `25-11 Fractured`
    - `10-0 Collapse`

- `com.sanitycraft.network.sync.SanitySyncService`
  - sync throttling by interval, delta, and keepalive window

- `com.sanitycraft.command.debug.SanityCommand`
  - foundation debug command tree

## Still Live In Legacy Runtime

- `net.nekuzaky.sanitycraft.SanitycraftMod`
- `net.nekuzaky.sanitycraft.SanitycraftModClient`
- legacy registries
- legacy sanity config
- legacy sanity persistence JSON writer
- legacy sanity tick loop
- legacy HUD and client horror systems
- legacy `/sanity` command
- legacy networking payloads

## Duplicated Or Transitional Systems

- sanity config model
- sanity persistence implementation
- sanity manager/state ownership
- sanity command tree
- item logic for `pill` and `mentalshieldtotem`
- sanity sync packet path

These duplicates are intentional only during the migration window.

## Must Be Ready Before Switching `fabric.mod.json`

1. New bootstrap must own all active sanity state.
2. Old and new sanity managers must not both tick in the same runtime.
3. New registries must cover all content that would otherwise double-register or disappear.
4. Client-visible sanity feedback must be present enough to validate sync.
5. SavedData migration from legacy JSON must be verified by relogging and world reload.
6. Debug commands must point to the active runtime, not a parallel shadow system.
7. Item registrations for preserved ids must have exactly one active owner.

## Must Be Disconnected Or Removed At Cutover

- legacy entrypoints in `fabric.mod.json`
- legacy sanity event wiring
- legacy sanity persistence ownership
- legacy sanity networking ownership
- legacy `/sanity` command registration
- duplicate item registration for preserved ids

Do not remove legacy horror systems until the new runtime actually owns sanity state and sync.

## Runtime Risks At Cutover

1. Duplicate item or command registration if both bootstraps initialize.
2. Two competing sanity truths if both tick loops run.
3. Apparent data loss if the new runtime ignores old saved sanity and migration is broken.
4. Client HUD drift if sync is live but the client still reads old state.
5. Misleading debug output if commands target the wrong runtime owner.

## Rollback Plan

1. Revert `fabric.mod.json` entrypoints to the legacy bootstrap only.
2. Keep the legacy `data/sanitycraft_sanity.json` file untouched for rollback safety.
3. Leave the new `SavedData` payload in place; do not delete migrated data during rollback.
4. Rebuild and verify the legacy runtime boots cleanly.

## Validation Steps For The Eventual `runClient` Cutover

1. Start `runClient`.
2. Create a new world.
3. Run `/sanity get`.
4. Run `/sanity set 50`.
5. Confirm the active runtime reports `Disturbed`.
6. Relog into the world.
7. Run `/sanity get` again and confirm the value persisted.
8. Run `/sanity collapse`.
9. Confirm the synced client-facing sanity value updates without errors.
10. Stop and restart the client, reopen the same world, and confirm persistence again.
11. Check logs for duplicate registration warnings or duplicate sanity tick behavior.

## Cutover Gate

Do not switch entrypoints until:

- one sanity runtime is clearly authoritative
- persistence has been verified in play
- sync has been verified in play
- duplicate ownership has been removed or explicitly disabled
