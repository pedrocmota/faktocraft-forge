# Faktocraft

<img src="docs/banners/main.png" alt="Faktocraft" width="700">

Faktocraft is a modern port of several classic tech mods - **IndustrialCraft 2**, **BuildCraft** and **Logistics Pipes** - brought together into a single mod: electrical machines, tiered power grids, item and fluid pipes, quarries and a full logistics system.

> ⚠️ The mod is in its **first alpha version** - bugs may occur.

## Features

- Electrical machines with a tiered voltage system (low to ultra), transformers and circuit breakers
- Power generation: solar, wind, geothermal, combustion and more
- Item and fluid pipes with a request-based logistics network
- BuildCraft-style quarry with landmarks
- Jetpacks, electric armor and tools

## Requirements

- **Minecraft 1.20.1 with Forge** - this is the only supported version.
- The mod will be updated to newer Minecraft versions once Minecraft stabilizes its Vulkan support in the future.

## Compatibility

Faktocraft has no mixins, coremods or access transformers, so it rarely conflicts with other mods.

### Compatible

- **JEI** - recipe categories for every machine, recipe transfer (the `+` button) into machines, the request table and the craft/recipe pipes.
- **Jade** - energy, cable, circuit breaker, valve and machine info in the overlay.
- **REI** - native plugin with the same categories and recipe transfer as JEI.
- **WTHIT** - native plugin with the same overlay information as Jade.
- **Embeddium** and **Oculus** - including shader packs.
- **Canary**.
- **Distant Horizons**.

### Compatible without integration

- Minimaps, inventory tweaks, UI mods and most performance mods.
- **Any mod using Forge item and fluid capabilities** - inventories, tanks and hoppers work with the pipes, the quarry and the machines. Storage networks such as Applied Energistics 2 and Refined Storage are reachable as ordinary inventories.
- **Forge tags** - ores, ingots, dusts and other materials use `forge:` tags, so shared ores and ingredients from other mods are recognized. The quarry mines any ordinary block from any mod and can be tuned with the `faktocraft:quarry_blacklist` and `faktocraft:quarry_mineable` block tags.
- **Datapacks, KubeJS, CraftTweaker** - every recipe type has a JSON serializer, so Faktocraft recipes can be added, changed or removed.
- **Forge Energy mods** (Mekanism, Thermal, Powah, Create and others) - Faktocraft power is its own system and is not exposed as FE. There is no converter yet.
- **Curios** - the jetpack and the night vision goggles must be worn in the armor slots.
- **Land claim mods** (FTB Chunks, Flan and similar) - the quarry and the forester do not enforce claims yet, the hole drill does.

### Incompatible

- **Folia** and other region-threaded servers - the energy networks, the logistics engine and the block entities assume a single server thread per world.

## Fabric?

A Fabric port is unlikely - the mod is deeply tied to Forge internals (energy capabilities, registries, networking), which makes porting it very difficult.

## Wiki

The official wiki is available at **[pedrocmota.github.io/faktocraft-forge](https://pedrocmota.github.io/faktocraft-forge)** (English and Portuguese). It is still under development, so some pages may change.

## Translations

The mod is written in English and has been tested in English and Portuguese, the two languages spoken by me. It also includes the following translations:

- Portuguese, Portugal (pt_pt)
- Spanish (es_es)
- Russian (ru_ru)
- Ukrainian (uk_ua)
- Slovak (sk_sk)
- Turkish (tr_tr)
- Japanese (ja_jp)
- Czech (cs_cz)
- Korean (ko_kr)
- Chinese, Simplified (zh_cn)

These translations were AI-generated and have not been reviewed by a speaker, so they may contain errors. Corrections are welcome!

## Bug reports

Found a bug? Please report it on the [GitHub issue tracker](https://github.com/pedrocmota/faktocraft-forge/issues) of this repository.

## License

[MIT](LICENSE)
