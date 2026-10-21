# Upstream software and notices

- Base simulator: [Beta8397/virtual_robot](https://github.com/Beta8397/virtual_robot), source snapshot `168a8a32028f7b1e51a3a767329f193c18b6a911`. Original README is preserved as `UPSTREAM-README.md`. Retain the source headers and `LEGAL/` notices, including FTC/Qualcomm/REV and other attributions. Existing lib JARs are included unchanged.
- Pedro v3 / Ivy: upstream simulator's bundled desktop library. This is its virtual-robot integration, not Android hardware code.
- dyn4j 4.1.4: included upstream; used for planar drivetrain dynamics.
- OpenJFX 21.0.2 Windows x64 modules: downloaded from Maven Central; license notices are included in the module JARs. OpenJFX is GPL v2 with the Classpath Exception. [OpenJFX](https://openjfx.io/)
- Bundled Java runtime/compiler: JetBrains Runtime OpenJDK 21.0.10, Windows x64. Copyrights, GPL v2/Classpath Exception terms and other notices are retained in `runtime/legal`, and release metadata in `runtime/release`. [JetBrains Runtime source](https://github.com/JetBrains/JetBrainsRuntime).

BIOBUZZ additions are the `virtual_robot/biobuzz` package, `BioBuzz` game, `BioBuzzBot` configuration, synthetic Limelight DTO/device adapters, desktop SDK compatibility additions, and import/build/launch tooling. Original upstream applications/configuration were modified locally to load these additions. No changes were pushed to either upstream repository.

The simulator's SDK approximation cannot reproduce every real FTC SDK API or hardware effect. Runtime code executes locally with normal desktop Java permissions.

The v2 visuals use procedural JavaFX geometry and textures; no additional asset dependency is required. Kinematics reference: [WPILib mecanum documentation](https://docs.wpilib.org/en/latest/docs/software/kinematics-and-odometry/mecanum-drive-kinematics.html). Planar engine reference: [dyn4j advanced documentation](https://dyn4j.org/pages/advanced). Custom ball contacts, spin, and flywheel approximations are local simulator models.

## Driver Studio v3 dependencies

- Three.js 0.170.0 (MIT), including OrbitControls and STLLoader. License: web/vendor/THREE-LICENSE. Source: https://github.com/mrdoob/three.js/tree/r170 .
- occt-import-js 0.0.23 (LGPL-2.1), including OpenCascade WebAssembly. License: web/vendor/OCCT-IMPORT-LICENSE.md. Source and build instructions: https://github.com/kovacsv/occt-import-js . The JS/WASM files may be replaced with compatible rebuilt versions; no integrity check prevents replacement. The cube STEP test fixture comes from the same upstream project.
- Gson 2.11.0 (Apache-2.0). License: LEGAL/GSON-LICENSE. Source: https://github.com/google/gson/tree/gson-parent-2.11.0 .
- fflate 0.8.2 (MIT), for local source ZIP imports. License: web/vendor/FFLATE-LICENSE. Source: https://github.com/101arrowz/fflate/tree/v0.8.2 .

Download URLs and SHA-256 hashes are in web/vendor/manifest.json. Procedural textures and audio remain in use; official field geometry and artwork are attributed below.

## Official FIRST BIOBUZZ field CAD

`web/assets/field/` includes tessellated derivatives of FIRST's BIOBUZZ field STEP v26-27.2, retrieved October 20, 2026 from https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step. FIRST and the original designers retain ownership. No separate permissive CAD license is asserted. These attributed derivatives provide the simulator's educational field reference. Source hashes, conversion tolerances, exclusions and coordinates are in the asset manifest and README.

`tools/vendor/meshopt_simplifier.cjs` is meshoptimizer v0.24 by Arseny Kapoulkine and contributors, MIT licensed; license in `tools/vendor/MESHOPT-LICENSE.md`. It is used offline to produce the balanced rendering mesh. Source: https://github.com/zeux/meshoptimizer/tree/v0.24.

The four `apriltag-board-*.png` textures are crops of FIRST/AndyMark AprilTag Production Art V1, from https://ftc-resources.firstinspires.org/ftc/archive/2027/field/apriltag-art. They retain the original design and ownership and are display derivatives, not replacement competition print files. Measurement and cropping details are in `docs/VISION-MODEL.md`.
