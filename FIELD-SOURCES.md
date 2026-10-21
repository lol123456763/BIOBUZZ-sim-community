# Field sources and measurement provenance

Checked October 20, 2026. The visible field now uses the actual FIRST BIOBUZZ STEP CAD v26-27.2, downloaded from the official FIRST field resource page. Physics uses reduced-order collision proxies; loading CAD does not make contact dynamics CAD-exact.

| Source | Used for |
| --- | --- |
| [FIRST 2026-27 playing field resources](https://ftc-resources.firstinspires.org/ftc/archive/2027/field) | Official source index and CAD links |
| [Arena, Section 9, TU04](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/manual-09) | 144in field, 24in tiles, HIVE/flower/ball dimensions and tag IDs |
| [Event field setup guide V1](https://ftc-resources.firstinspires.org/ftc/archive/2027/field/eventfieldguide) | Frame centering, flower positions, initial elements, HIVE calibration |
| [Game details, TU05](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/manual-10) | Scoring context |
| [Official Onshape field CAD](https://cad.onshape.com/documents/a355e772e3d24813de7852ee/w/f106353168f1f92100b81259/e/95d1e1e442b4138cccaf2d73) | Official source; derived tessellated field geometry is bundled offline |

## Published nominal dimensions

These are the original manual-based references, not a claim that every CAD feature equals a rounded nominal dimension. The current collision perimeter uses the CAD rail envelope at +/-1.792859 m (top 0.29575666 m), shared by balls, chassis and distance sensors. FLOWER centers now use the CAD positions (+/-0.5942, +/-1.7283 m, rotated around the field) and capture-top height 0.54366346 m. Support legs and frame feet also use measured CAD proxies. See `docs/REALISM.md` for the remaining approximations.

- Nominal field size: 144 x 144 inches; 6 x 6 tiles. The CAD perimeter used by this simulator differs from that nominal size, as described above.
- Frame envelope: 49.46in wide by 38.95in deep; pivot 43.95in above floor.
- HIVE lane separation: 25.5in; stable tilt 30 degrees.
- CELL face: 20in width, 14in full pentagonal height, 7.61in lower rectangular portion; 12.04in nominal depth; 18.84in gap between cells.
- Upward CELL opening: bottom 53.5in, top 65.6in above floor. Entry plane is inclined 60 degrees from horizontal, inferred from the 30-degree stable tilt.
- FLOWER top opening 4in diameter, 21.5in above floor.
- POLLEN 2.8in diameter; NECTAR 3.6in diameter.
- Red clusters 30-33 (scoring side), 34-37 (audience side); blue 38-41 (audience), 42-45 (scoring).
- Initial red audience CELL is up; initial blue scoring CELL is up; each has 3 alliance nectar.
- Flowers at wall joins shown in setup guide: north x=-24in, east y=24in, south x=24in, west y=-24in. The previous 2.4in estimated inset is superseded by CAD placement in the simulation.
- Preferred tip calibration targets: 8 pollen, or 3 nectar plus 3 pollen. The actual calibration also has necessary dynamic test requirements; those are not a full mechanical model.

## Coordinates

The visible plan uses the field center as (0,0), positive x right/east and positive y toward the scoring side/north. The internal chassis heading is 0 facing north and increases counterclockwise. Pedro's normal path frame uses x/y in inches and heading 0 facing +x, so a north-facing start is 90 degrees. The upstream Pinpoint adapter handles this heading convention. Place the actual simulated robot at the corresponding start location before INIT; changing the reported odometry pose does not teleport the chassis.

Published documents remain copyrighted by FIRST and their respective owners. Local reference PDF copies are supplied for checking dimensions; they are not modified.

## Driver station views (v2)

The red and blue driver eyes sit outside the corresponding side wall within the nominal 54in deep by 97in long alliance area. Default eye height is an assumed 1.60m; sliders allow 1.20-1.90m and movement along the station. These are viewpoints inside the driver area, not a prescribed human location. The camera uses the same field coordinates as the simulation, with a corrected unmirrored projection.

## Actual CAD integration (October 20, 2026)

- Official archive: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step (listed as v26-27.2, September 15, 2026).
- Full provenance, source SHA-256 hashes, all part bounds and assembly names: `web/assets/field/manifest.json`.
- Converter: `tools/convert-field.cjs`; packer: `tools/pack-field.cjs`; display optimization: `tools/simplify-field.cjs`.
- Offline full mesh: `web/assets/field/field-full.json.gz`; optimized balanced mesh: `web/assets/field/field.json.gz`. Both originate from the official STEP, not hand-drawn replacements.
- Source coordinates already match render coordinates (meters, Y-up, tile surface Y=0). No recentering or scale fitting. Physics x/y/z corresponds to CAD X/-Z/Y.
- Red and blue named HIVE assemblies rotate rigidly about their CAD bearing axis at Y=1.11632238 m, Z=0. The opposite stable state is 60 degrees from the source state. Tipping dynamics remain a simulation approximation.
- Source normals and CAD colors retained. Foam texture, roughness, transparency and lighting are estimated optical properties, not measured material data.
- Static CAD pollen/nectar instances are excluded to avoid duplicated game pieces; simulated moving balls represent them. All fixtures and hardware are retained in the full model.
- High quality: original 1 mm / 0.35 radian OpenCascade tessellation, 10 micrometer coordinate rounding. Balanced: meshoptimizer v0.24 simplification, relative error limit 0.0003 per material batch. This is a visualization optimization, not a modification of physical collision data.
- White AprilTag board geometry is present in STEP. Printed overlays now use the official FIRST AprilTag production-art PDF (https://ftc-resources.firstinspires.org/ftc/archive/2027/field/apriltag-art), cropped to its 17 x 5 inch cut lines. Four board textures follow their measured CAD plate positions and HIVE rotation. Printed black tag squares are 3.25in; their horizontal offsets are -6.5, -2.75, +2.75, +6.5in and centers sit 0.25in above the board center.
- The older nominal geometry list above describes the fallback/reduced-order model, not a claim that nominal dimensions override the actual CAD.

Field design and source CAD remain owned by FIRST and their respective designers. See `web/assets/field/README.md` for reproduction and scope.
