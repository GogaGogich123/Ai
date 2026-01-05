# Minecraft 3D build generator (Forge 1.19.2, Litematica) — working notes

## Goal
Build an AI system that generates **high-quality Minecraft builds** (including **functional interiors**) from a **text prompt** and **exact dimensions**. It must support:
- Arbitrary build sizes (achieved via **chunked generation**, not single-pass full-volume inference).
- Building on existing terrain and **continuation / extension** of existing builds.
- Output compatible with **Litematica** (`.litematic`).

Constraints:
- Training/inference expected to run in **Google Colab free tier** (as much as possible).
- Users likely have weak PCs; expectation: run model in cloud.
- Server context: Minecraft Java **Forge 1.19.2**.
- “Visual beauty only” is insufficient; must have believable and usable interiors.

User-specified requirements from chat:
- Output format: **Litematica** `.litematic`.
- Primary capability: generate from **text prompt**; also **continue/extend** an existing build (inpainting / completion).
- Build placement: should be able to generate “on terrain” (even if terrain generation itself is not the focus).
- Dimensions: user does **not** want to provide exact sizes; prompt is mostly verbal. System may still accept optional constraints.
- Blocks: must be “building blocks” (not just color matching). No bedrock/barrier/command blocks by default unless user requests.
- Physics/plausibility: no unrealistic floating structures; structural believability.
- Quality target: looks like built by **professional builders**; includes exterior + **interior**, detail, and completeness.
- Diversity: avoid obvious templates/pattern repetition; aim for unique results per prompt.
- Styles: **any** architectural style.
- Size: **any** size; typical reference size mentioned ~14k blocks, but must scale far beyond.
- UX: “would be cool” to see it building progressively; streaming placement is desirable.
- Deployment: each player runs their own Colab session; server-side integration later; admin role controls generation.

Non-goal (for now): implementing the Forge mod. Current focus: dataset strategy.

## Key architectural decision
User explicitly requested **3D AI, not an LLM**.

Clarification:
- To condition on free-form text prompts, a system still needs a **text encoder** (can be frozen; does not have to be an LLM generator).
- The generation itself should be **3D** (voxels/latent tokens), not “text commands”.

Practical 3D approach for Colab:
- **3D VQ-VAE** to compress block volumes into a smaller 3D latent grid.
- A **masked 3D latent model** (Transformer-like or similar) for **inpainting** (best for continuation/repair).
- Optional: latent diffusion/DiT for richer style, but usually slower/harder on Colab.
- “Any size” is achieved via **tiling/chunking with overlap**, plus a global low-res plan if needed.

Interior quality strategy:
- Two-pass generation (recommended):
  1) Shell + room layout + connectivity.
  2) Interior fill (furniture, lighting, storage, etc.).
- Plus post-generation validators:
  - Support/physics plausibility (no large floating slabs).
  - Navigability (doors, stairs, corridors not blocked).
  - Basic lighting sufficiency.
  - Block policy enforcement (no bedrock/barriers by default).

## BuildPaste research (how it works; how to collect a dataset)
### Important: legality/ToS
BuildPaste Terms & Conditions (embedded via websitepolicies) contain prohibited use clause:
- Prohibited uses include: **“spider, crawl, or scrape”**.

Therefore, automated mass dataset extraction from BuildPaste without written permission is likely a ToS violation.

### Technical findings (endpoints & formats)
Even though ToS blocks scraping, the following was reverse-engineered for understanding:

#### 1) Build data download endpoint
Cloud Function:
- `GET https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/get/<BUILD_ID>?version=1.19.2&member=free`

Verified:
- Works with `version=1.19.2`.
- Returns JSON containing:
  - `size`: `[sx, sy, sz]`
  - `blocks`: list of numeric IDs (len = sx*sy*sz)
  - `data`: list of blockstate strings (len = sx*sy*sz), e.g. `"[axis=y,waterlogged=false]"`, `"[distance=1,persistent=true]"`
  - `direction`: upload direction
  - `nbt`: object keyed by block index, value is NBT-like string (tile entities), e.g. chest

Note: tested that “premium build” also returned 200 with `member=free`, but this should not be relied on.

#### 2) Build list / metadata source
The BuildPaste website uses Firebase/Firestore.

Firebase config was extractable from the site JS bundle:
- `projectId: buildpastemod`

Firestore query endpoint:
- `POST https://firestore.googleapis.com/v1/projects/buildpastemod/databases/(default)/documents:runQuery?key=<API_KEY>`

The site queries `collectionId: "builds"` with filters like:
- `private == false`
- `published == true`
- `category == <...>`

Fields observed in documents:
- `name`, `description` (often very short), `category`, `blockCount`, `date`, `premium`, `thumbnail`, `creatoruuid`, etc.

#### 3) Mapping numeric IDs to `minecraft:<block>`
BuildPaste encodes blocks as indices into an internal `blocksArray`.

For **Forge 1.19.2**, the Modrinth BuildPaste jar `BuildPasteMod-1.19.2v1.9.5.1.jar` (project: `buildpaste`) contains:
- `Functions.java` with a very large string `blocksFile = "air\nstone\n..."`
- `blocksArray = blocksFile.split("\n", -1)`

Then:
- `minecraft:` + `blocksArray[id]` gives the base block name.
- Append `data[i]` for blockstates.

Thus, BuildPaste’s JSON can be normalized into canonical Minecraft block + state representation (and later re-exported).

### Suggested dataset pipeline (if permission exists)
(technical outline only; not a recommendation to scrape without permission)

1) Enumerate builds from Firestore by category and ordering.
2) Download build payloads via cloud function for `version=1.19.2`.
3) Normalize blocks:
   - id -> block name via `blocksArray`
   - combine with blockstate strings `data[i]`
   - attach NBT if present
4) Filter:
   - drop trivial builds (e.g. `blockCount < 800`)
   - optionally cap extremely large builds in early training (`blockCount > 300k`)
   - enforce block policy (no bedrock/barrier/command blocks unless explicitly allowed)
5) Deduplicate:
   - hash based on dims + block histograms + low-res projections.
6) Text augmentation:
   - derive tags (palette/materials, floor count, roominess, furniture presence, symmetry heuristics)
   - generate synthetic descriptions from tags.
7) Chunking for 3D training:
   - split into 32³ (or similar) chunks with overlap
   - create masked-inpainting tasks to support continuation/repair.

## Dataset alternatives (since BuildPaste scraping is not acceptable)
User rejected opt-in collection as the primary route and asked for internet alternatives.

Realistic options:

### A) Open-license schematic repositories (preferred legal route)
- GitHub repos containing `.litematic` / `.schem` / `.schematic` with clear licenses (CC0/CC-BY/MIT/etc.).
- Example found: `Greaby/minecraft-circles-schematics` (CC0) — limited to geometric circles (not “pro interiors”), but demonstrates availability.
- In general, quality/quantity varies; must verify per-repo licensing.

### B) Synthetic dataset (clean license, scalable)
- Procedural generation of buildings/interiors + export to `.litematic/.schem`.
- 3D model -> schematic conversion using tools like `ObjToSchematic` (BSD-3), for pretraining shapes.
- This helps pretrain VQ-VAE/compressors, but does not guarantee Minecraft-architectural realism unless the generator is strong.

### C) Partnerships / permissions
- Contact build teams / authors for permission and bulk exports.
- This is the most direct path to “professional” build quality.

## Colab + “watch it build” plan (future, not implemented)
- Model runs in Colab as a temporary service.
- Server mod requests generation, receives streamed placement ops.
- Streaming stages: foundation -> structure -> roof -> facade -> interior.
- Colab is not stable hosting; requires user-run sessions.

## Open questions / next steps
1) Decide legal dataset source strategy (open-license / synthetic / partnerships).
2) Pick file formats for training storage:
   - recommended: chunk-level tensors + metadata (NPZ/Parquet/JSONL+bin).
3) Define block vocabulary for 1.19.2:
   - vanilla blocks only by default
   - handle blockstates and NBT
4) Define objective functions / evaluation:
   - interior usability metrics, support constraints, diversity metrics, duplication checks.
5) Once dataset exists, implement baseline:
   - 3D VQ-VAE on 32³ chunks
   - masked latent model for inpainting/continuation
   - chunked inference with overlap
