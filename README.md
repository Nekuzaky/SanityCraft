# SanityCraft

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-3C8527?style=for-the-badge)
![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=for-the-badge)
![Fabric API](https://img.shields.io/badge/Fabric_API-0.133.4%2B1.21.8-DBD0B4?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Alpha-bb2d3b?style=for-the-badge)

> A psychological horror mod for Minecraft Java Edition (Fabric).  
> Survive the world... and your own mind.

## Concept
**SanityCraft** ajoute un système de santé mentale (`sanity`) qui évolue selon l'environnement du joueur.

- Dans l'obscurité, les grottes, les orages ou près des mobs hostiles, la sanity baisse.
- Dans des zones sûres (lumière, village, sommeil, musique), elle remonte.
- Plus la sanity descend, plus les effets deviennent oppressants: sons étranges, brouillage visuel, hallucinations et apparitions.

## Fonctionnalités
- Système de sanity par joueur (`0 -> 100`, par défaut `100`)
- Logique multiplayer (chaque joueur a sa propre sanity)
- Mise à jour par intervalle configurable
- Effets progressifs par paliers de sanity
- Hallucinations côté joueur (sons/visuels)
- Intégration du **Stalker** en apparition mentale temporaire
- HUD sanity custom + ambiance visuelle horror
- Refonte visuelle de l'inventaire (overlay mental)
- Fichier de configuration JSON

## Paliers de Sanity
- `80-100`: stable
- `60-80`: malaise (sons étranges)
- `40-60`: trouble (effets visuels légers)
- `20-40`: hallucinations
- `0-20`: phase critique (apparitions/paranormal)

## Stack
- Minecraft Java Edition `1.21.8`
- Fabric Loader `0.17.2+`
- Fabric API `0.133.4+1.21.8`
- Java `21`
- MCreator + Fabric Loom

## Installation (dev)
1. Installer Java 21.
2. Cloner ce repo.
3. Lancer `./gradlew runClient` (ou via MCreator).

## Configuration
Le fichier est généré automatiquement dans:

- `config/sanitycraft.json`

Paramètres principaux:
- vitesse de perte/gain de sanity
- intervalle d'update
- activation des hallucinations
- paramètres du Stalker (chance, durée, distance de spawn)

## Structure
- `sanity/PlayerSanityComponent` : état sanity par joueur
- `sanity/SanityManager` : calcul et mise à jour de la sanity
- `sanity/SanityEvents` : hooks événements/tick
- `sanity/SanityEffects` : effets/hallucinations
- `client/SanityHudRenderer` : HUD sanity

## Roadmap
- [ ] Commandes debug (`/sanity get/set`)
- [ ] Sauvegarde persistante avancée des états mentaux
- [ ] Événements narratifs (faux messages, jumpscares sonores)
- [ ] Compatibilité datapack/config avancée
- [ ] Polish audio/visuel horror

## License
MIT License
