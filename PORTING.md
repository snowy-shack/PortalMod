# Fabric 1.20.1 port: status

This branch ports PortalMod from **Forge 1.16.5** to **Fabric 1.20.1**.
The code compiles; the portal rendering pipeline has not been ported yet.
Everything marked `TODO(port)` in the source is a known gap.

## How the port was done

1. **Build:** ForgeGradle was replaced with Fabric Loom 1.11 (Gradle 8.14, Java 17 target)
   using Mojang's official mappings (`loom.officialMojangMappings()`).
   `mods.toml` became `fabric.mod.json`, and the access transformer became
   `portalmod.accesswidener`.
2. **Class renames:** Forge 1.16.5 "official" mappings only gave Mojang names to methods and
   fields; classes kept their MCP names (`MatrixStack`, `WorldRenderer`, ...). Every MCP class
   name was renamed to its Mojang 1.20.1 name using a table generated from MCPConfig 1.16.5
   plus Mojang's 1.16.5 client mappings.
3. **Forge APIs:** replaced either with Fabric API or with small classes in the mod
   (listed below).
4. **Vanilla 1.16 → 1.20 changes:** fixed one by one (block entities, entity models, GUI,
   text components, damage types, materials, tags, recipes, commands, ...).

## Forge → Fabric replacements

| Forge 1.16.5 | Now |
|---|---|
| `DeferredRegister` / `RegistryObject` | `core/registry` (registers straight into vanilla registries) |
| `SimpleChannel`, `PacketDistributor`, `NetworkEvent.Context` | `core/network` (Fabric networking API) |
| `IEntityAdditionalSpawnData` / `NetworkHooks` | `core/network/EntitySpawnPackets` |
| `DistExecutor` | `core/util/ClientOnly` |
| `ForgeConfigSpec` | `core/config` (JSON file `config/portalmod-client.json`) |
| Forge event bus (`CommonEvents`, `ClientEvents`, `ClientModEvents`) | Fabric API callbacks, plus hook mixins in `mixins/hooks` |
| `ForgeFlowingFluid` | `common/sorted/goo/GooFluid` |
| Custom model loaders / `IDynamicBakedModel` / `IModelData` | Fabric model loading API (`client/render/PortalModModels`), Fabric Rendering API for the antline |
| `setISTER` | `BuiltinItemRendererRegistry` (portal gun item model is `builtin/entity`) |
| `getArmorModel` / `getArmorTexture` | `ArmorRenderer` (`LongFallBootsClient`) |
| `RenderTarget.enableStencil` | `StencilTarget` + `FramebufferMixin` |
| Block/item extension methods (`getPistonPushReaction`, `isStickyBlock`, `removedByPlayer`, `doesSneakBypassUse`, `canConnectRedstone`, `fillItemCategory`) | Interfaces in `core/interfaces` + mixins in `mixins/hooks` |
| Damage sources by name | Data-driven damage types (`data/portalmod/damage_type`, `DamageTypeInit`) |
| `TextureStitchEvent` | `assets/minecraft/atlases/blocks.json` |

## Known gaps (`TODO(port)`)

- **Portal rendering.** The 1.16 renderer relied on fixed-function OpenGL and the old chunk
  renderer:
  - `RenderUtil.setClipPlane` is a no-op. `glClipPlane` does not exist in the core-profile
    context 1.17+ uses; clipping has to move into shaders (`gl_ClipDistance`).
  - `PortalTransparencyHandler` no longer re-sorts translucent chunks from the portal camera.
  - `PortalGunAnimatedTexture` uses `RenderSystem.setTextureMatrix`, which only shaders with
    `TextureMat` read. The animation may not play.
  - `ChunkRenderMixin`, `LevelRendererMixin`, `GameRendererMixin` and `PortalRenderer` compile,
    but need in-game testing.
- **Data generation** (`core/datagen`) is excluded from the build. Its output is already in
  `src/generated/resources`. Port it to Fabric's datagen API if you need to regenerate.
- **Entity tracking through portals** (`EntityTrackerMixin`) uses the 1.20.1 distance check.
  The 1.16 chunk "checkerboard" check is gone.
- **Portals on chunk load** (`CommonEvents.onChunkLoad`): 1.17 stores entities separately from
  chunks, so re-adding portal entities on chunk load may need rework.
- **Gravity attribute:** Forge's `ENTITY_GRAVITY` doesn't exist on Fabric; the code uses
  vanilla's 0.08.
- **Reach:** Forge's `REACH_DISTANCE` attribute is replaced by vanilla's fixed reach
  (4.5 survival / 5 creative).
- **Fog in goo:** 1.17+ only has linear fog, so the old exponential density is converted to a
  fog end distance (`3 / density`). It may look a little different.
- **`Mat4.to4f()`** transposes on the assumption that Forge's 1.16 `Matrix4f(float[])` read
  row-major. Not verified.

- **Custom shaders** (`assets/portalmod/shaders`) are GLSL 1.10/1.20 using compatibility-profile
  built-ins (`gl_Vertex`, `gl_FragColor`, ...). They compiled on Mesa in testing, but strict
  core-profile drivers (macOS especially) will probably reject them. Rewrite them as
  `#version 150` when redoing the renderer.
- **Faith plate GUI grid** still uses `glBegin`/`glEnd`, which draws nothing on a core profile.

## What has been tested

Tested in a headless client (Xvfb + Mesa software OpenGL):

- `./gradlew build` passes. The 7 existing JUnit tests pass. They never actually ran before,
  because the Forge build had no JUnit dependency.
- The client starts, and the Mixin audit applies every mixin with no failed injection points.
- The PortalMod title screen (custom panorama, "PORTAL MOD" logo, splash texts) shows.
- A new creative world loads and runs without crashing.

Not tested yet: placing or using any mod block, item or entity; portals; multiplayer; a
dedicated server.

## Building and running

```bash
./gradlew build
```

```bash
./gradlew runClient
```

`runClient` passes `-Dportalmod.auditMixins=true`, which applies every mixin at startup so a
broken injection point fails right away.
