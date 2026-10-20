# Field sources and measurement provenance

Checked October 18, 2026. The field is built from nominal public drawings, not imported Onshape collision meshes. The official CAD remains the authoritative geometry.

| Source | Used for |
| --- | --- |
| [FIRST 2026-27 playing field resources](https://ftc-resources.firstinspires.org/ftc/archive/2027/field) | Official source index and CAD links |
| [Arena, Section 9, TU04](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/manual-09) | 144in field, 24in tiles, HIVE/flower/ball dimensions and tag IDs |
| [Event field setup guide V1](https://ftc-resources.firstinspires.org/ftc/archive/2027/field/eventfieldguide) | Frame centering, flower positions, initial elements, HIVE calibration |
| [Game details, TU05](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/manual-10) | Scoring context |
| [Official Onshape field CAD](https://cad.onshape.com/documents/a355e772e3d24813de7852ee/w/f106353168f1f92100b81259/e/95d1e1e442b4138cccaf2d73) | Reference link; exact CAD meshes are not bundled |

## Published nominal geometry represented

- Field inside perimeter: 144 x 144 inches; 6 x 6 tiles.
- Frame envelope: 49.46in wide by 38.95in deep; pivot 43.95in above floor.
- HIVE lane separation: 25.5in; stable tilt 30 degrees.
- CELL face: 20in width, 14in full pentagonal height, 7.61in lower rectangular portion; 12.04in nominal depth; 18.84in gap between cells.
- Upward CELL opening: bottom 53.5in, top 65.6in above floor. Entry plane is inclined 60 degrees from horizontal, inferred from the 30-degree stable tilt.
- FLOWER top opening 4in diameter, 21.5in above floor.
- POLLEN 2.8in diameter; NECTAR 3.6in diameter.
- Red clusters 30-33 (scoring side), 34-37 (audience side); blue 38-41 (audience), 42-45 (scoring).
- Initial red audience CELL is up; initial blue scoring CELL is up; each has 3 alliance nectar.
- Flowers at wall joins shown in setup guide: north x=-24in, east y=24in, south x=24in, west y=-24in. Inset of 2.4in is a simplified placement estimate.
- Preferred tip calibration targets: 8 pollen, or 3 nectar plus 3 pollen. The actual calibration also has necessary dynamic test requirements; those are not a full mechanical model.

## Coordinates

The visible plan uses the field center as (0,0), positive x right/east and positive y toward the scoring side/north. The internal chassis heading is 0 facing north and increases counterclockwise. Pedro's normal path frame uses x/y in inches and heading 0 facing +x, so a north-facing start is 90 degrees. The upstream Pinpoint adapter handles this heading convention. Place the actual simulated robot at the corresponding start location before INIT; changing the reported odometry pose does not teleport the chassis.

Published documents remain copyrighted by FIRST and their respective owners. Local reference PDF copies are supplied for checking dimensions; they are not modified.

## Driver station views (v2)

The red and blue driver eyes sit outside the corresponding side wall within the nominal 54in deep by 97in long alliance area. Default eye height is an assumed 1.60m; sliders allow 1.20-1.90m and movement along the station. These are viewpoints inside the driver area, not a prescribed human location. The camera uses the same field coordinates as the simulation, with a corrected unmirrored projection.
