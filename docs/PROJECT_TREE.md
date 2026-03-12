# SanityCraft Project Tree

Local/generated folders such as `.gradle/`, `build/`, `run/`, `tmp/`, and the leftover local `.mcreator/` settings folder are intentionally omitted here.

```text
sanitycraft/
|-- .gitattributes
|-- .gitignore
|-- CHANGELOG.md
|-- README.md
|-- RELEASE_NOTES_0.1.2-beta.md
|-- build.gradle
|-- gradle.properties
|-- gradlew
|-- gradlew.bat
|-- settings.gradle
|-- .vscode/
|   |-- extensions.json
|   |-- launch.json
|   `-- tasks.json
|-- art/
|   |-- blockbench/
|   |   |-- creeper.bbmodel
|   |   `-- stalker.bbmodel
|   `-- photoshop/
|       |-- MentalShieldTotem.psd
|       `-- Sanity Pill.psd
|-- gradle/
|   `-- wrapper/
|       |-- gradle-wrapper.jar
|       `-- gradle-wrapper.properties
`-- src/
    `-- main/
        |-- java/
        |   `-- net/
        |       `-- nekuzaky/
        |           `-- sanitycraft/
        |               |-- SanitycraftMod.java
        |               |-- SanitycraftModClient.java
        |               |-- client/
        |               |   |-- ClientBootstrap.java
        |               |   |-- ClientEntityRenderers.java
        |               |   |-- ClientParticles.java
        |               |   |-- DiscordRichPresenceManager.java
        |               |   |-- HorrorAmbienceDirector.java
        |               |   |-- HorrorUiOverlays.java
        |               |   |-- MainMenuHorrorOverlay.java
        |               |   |-- SanityClientState.java
        |               |   |-- SanityHudRenderer.java
        |               |   |-- SanitycraftTitleScreen.java
        |               |   |-- particle/
        |               |   |   `-- BloodParticle.java
        |               |   `-- renderer/
        |               |       |-- BloodyCreeperRenderer.java
        |               |       `-- StalkerRenderer.java
        |               |-- command/
        |               |   `-- SanityCommand.java
        |               |-- effect/
        |               |   `-- SanityEffects.java
        |               |-- entity/
        |               |   |-- BloodyCreeperEntity.java
        |               |   `-- StalkerEntity.java
        |               |-- event/
        |               |   |-- BlockEvents.java
        |               |   |-- ItemEvents.java
        |               |   |-- LivingEntityEvents.java
        |               |   |-- MiscEvents.java
        |               |   `-- PlayerEvents.java
        |               |-- item/
        |               |   |-- MentalShieldItem.java
        |               |   `-- SanityPillItem.java
        |               |-- mixin/
        |               |   |-- AbstractContainerScreenAccessor.java
        |               |   |-- AbstractContainerScreenMixin.java
        |               |   |-- BlockItemMixin.java
        |               |   |-- BoneMealItemMixin.java
        |               |   |-- CommandsMixin.java
        |               |   |-- ExperienceOrbMixin.java
        |               |   |-- FogRendererMixin.java
        |               |   |-- ItemStackMixin.java
        |               |   |-- LivingEntityMixin.java
        |               |   |-- MinecraftTitleMixin.java
        |               |   |-- PlayerMixin.java
        |               |   |-- RepairItemRecipeMixin.java
        |               |   |-- ServerPlayerMixin.java
        |               |   `-- TitleScreenMixin.java
        |               |-- registry/
        |               |   |-- ModCommands.java
        |               |   |-- ModCreativeTabs.java
        |               |   |-- ModEntities.java
        |               |   |-- ModItems.java
        |               |   `-- ModParticles.java
        |               |-- sanity/
        |               |   |-- HallucinationSoundManager.java
        |               |   |-- PlayerSanityComponent.java
        |               |   |-- SanityCalculator.java
        |               |   |-- SanityConfig.java
        |               |   |-- SanityDebugState.java
        |               |   |-- SanityDeltaResult.java
        |               |   |-- SanityEnvironmentHelper.java
        |               |   |-- SanityEnvironmentSnapshot.java
        |               |   |-- SanityEvents.java
        |               |   |-- SanityFractureQuestDirector.java
        |               |   |-- SanityJournal.java
        |               |   |-- SanityJumpscarePayload.java
        |               |   |-- SanityManager.java
        |               |   |-- SanityNarrativeDirector.java
        |               |   |-- SanityNetworking.java
        |               |   |-- SanityPersistence.java
        |               |   |-- SanityScarePulsePayload.java
        |               |   |-- SanityStage.java
        |               |   |-- SanityStageResolver.java
        |               |   |-- SanityStalkerHuntDirector.java
        |               |   |-- SanitySyncPayload.java
        |               |   `-- TorchHandLightHandler.java
        |               `-- util/
        |                   `-- ServerWorkQueue.java
        `-- resources/
            |-- META-INF/
            |   `-- sanitycraft.accesswidener
            |-- assets/
            |   `-- sanitycraft/
            |       |-- items/
            |       |   |-- bloody_creeper_spawn_egg.json
            |       |   |-- mentalshieldtotem.json
            |       |   |-- pill.json
            |       |   `-- stalker_spawn_egg.json
            |       |-- lang/
            |       |   `-- en_us.json
            |       |-- models/
            |       |   `-- item/
            |       |       |-- bloody_creeper_spawn_egg.json
            |       |       |-- mentalshieldtotem.json
            |       |       |-- pill.json
            |       |       `-- stalker_spawn_egg.json
            |       |-- particles/
            |       |   `-- blood.json
            |       `-- textures/
            |           |-- entities/
            |           |   |-- creeper.png
            |           |   `-- stalker.png
            |           |-- item/
            |           |   |-- bloody_creeper_spawn_egg_generated.png
            |           |   |-- mentalshieldtotem.png
            |           |   |-- sanity_pill.png
            |           |   `-- stalker_spawn_egg_generated.png
            |           |-- painting/
            |           |   `-- sanity_paint.png
            |           |-- particle/
            |           |   `-- blood.png
            |           `-- logo.png
            |-- data/
            |   |-- minecraft/
            |   |   `-- tags/
            |   |       `-- painting_variant/
            |   |           `-- placeable.json
            |   `-- sanitycraft/
            |       |-- painting_variant/
            |       |   `-- sanity_paint.json
            |       `-- recipe/
            |           |-- mental_shieldcraft.json
            |           `-- pillcraft.json
            |-- fabric.mod.json
            |-- logo.png
            |-- pack.mcmeta
            `-- sanitycraft.mixins.json
```
