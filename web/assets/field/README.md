# Official field asset

`field.json.gz` is a tessellated derivative of the official FIRST BIOBUZZ STEP CAD, version v26-27.2, downloaded from FIRST on October 20, 2026. FIRST and the original designers retain ownership of the field design. No separate permissive redistribution license is asserted. This asset is included as a field reference for this educational simulator, with attribution and a reproducible source manifest.

Source index: https://ftc-resources.firstinspires.org/ftc/archive/2027/field
STEP archive: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step

`manifest.json` records hashes, conversion tolerances, full part names, original bounds, mesh counts and assembly membership. Values are meters. The official STEP is already Y-up with floor at Y=0; no centering or scale-to-fit was applied. Render X = CAD X, render Y = CAD Y, render Z = CAD Z; physics (x,y,z) = (CAD X,-CAD Z,CAD Y).

The two named HIVE assemblies retain their official geometry and rotate about the CAD bearing axis at Y=1.11632238 m, Z=0. Their opposite stable state is a 60 degree rigid rotation. Transition timing is simulated; mechanical damping and collisions are not a validated CAD rigid-body solver.

POLLEN and NECTAR instances in the static CAD export are excluded because the simulator renders its moving game pieces. All other assembly mesh parts are retained, batched by material and assembly. Source surface normals are retained; positions are rounded to 10 micrometers for compact storage after 1 mm tessellation. CAD colors are retained; material optical properties (metalness, roughness and plastic transparency) are presentation estimates. STEP carries blank tag backing plates, not AprilTag pixel art.

Rebuild from repository root:

1. Download the source ZIP to `artifacts/field/official-field.zip` and extract its STEP to `artifacts/field/source/field-cad-step.step`.
2. `node tools/convert-field.cjs artifacts/field/source/field-cad-step.step > artifacts/field/mesh-info.json` (OpenCascade conversion may take several minutes).
3. `node tools/pack-field.cjs`.

The conversion uses the pinned, already vendored occt-import-js engine. Raw downloaded data and conversion intermediates are intentionally not part of the browser bundle.

For both display detail levels, after packing copy `field.json.gz` to `field-full.json.gz`, then run `node tools/simplify-field.cjs`. Balanced/Low use the optimized mesh; High uses the original tessellation. The simplifier targets 18% triangles with a maximum relative geometric error of 0.0003 per material batch; topology may prevent reaching that target. The optimized field still derives from actual CAD, and never changes physical collision parameters.

## Official AprilTag artwork

`apriltag-board-1.png` through `apriltag-board-4.png` are cropped from FIRST's production-art PDF at https://ftc-resources.firstinspires.org/ftc/archive/2027/field/apriltag-art. The 18 x 6 inch PDF page has a 17 x 5 inch board cut line, cropped at (36,36)–(1260,396) points. Board order is red scoring 30–33, red audience 34–37, blue audience 38–41, blue scoring 42–45. The renderer places these on the corresponding measured STEP plates with a 0.8 mm surface offset to avoid depth interference; this offset is display-only. Printed tags use actual unequal horizontal spacing and 3.25 inch black squares. Artwork remains owned by FIRST and its respective creators.
