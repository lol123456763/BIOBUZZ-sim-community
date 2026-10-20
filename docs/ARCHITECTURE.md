# Local simulator architecture

- `Launch.ps1` rebuilds changed Java sources when needed, then starts the existing JavaFX/FTC runner. No Node, Python, Android Studio or internet connection is required to run the app.
- `StudioServer` serves `web/` on `127.0.0.1:8765`. The browser renderer communicates only with this local process. Mutations require the per-launch session token and same-origin requests. Java source uploads are compiled in a staging directory before replacing a successful import.
- `StudioInput` is an immutable-frame mailbox consumed by the existing gamepad helper. It carries two FTC-shaped gamepads and clears stale packets after 250 ms. The browser sends its newest state when the previous request completes.
- `DynamicMecanumBase` integrates 5 ms force/contact substeps. `DriveMotorModel` supplies configurable torque-speed behavior; code-selected zero-power behavior controls brake/coast torque. The plan display is capped at about 30 Hz and its trail has a size limit.
- `Physics` handles 3D ball trajectories/contact approximations. Java is the authoritative physics state. Browser extrapolation is capped at 35 ms and affects display only.
- `FieldView` renders PBR materials, directional shadows and procedural textures. Graphics frame caps do not change physics step size.
- `CadRig` reads STL and uses an isolated worker with OpenCascade for STEP. User-imported CAD is stored in the browser's IndexedDB, separately from the Git repository. Named robot profiles are local JSON under ignored `profiles/`.

## Dependencies and references

Pinned binaries and browser modules are bundled; `web/vendor/manifest.json` records download URLs and SHA-256 hashes. Preserve the adjacent license files when redistributing.

- [Three.js renderer](https://threejs.org/docs/pages/WebGLRenderer): rendering, lighting and shadows.
- [Gamepad API](https://developer.mozilla.org/en-US/docs/Web/API/Gamepad_API) and [mapping semantics](https://developer.mozilla.org/en-US/docs/Web/API/Gamepad/mapping): browser-provided standard vs raw devices. Physical F310 mode/driver variants still need manual verification.
- [occt-import-js](https://github.com/kovacsv/occt-import-js): STEP tessellation and named assembly meshes. Motor/connection inference is application logic, not a capability supplied by STEP geometry.
- [WPILib mecanum documentation](https://docs.wpilib.org/en/latest/docs/software/kinematics-and-odometry/mecanum-drive-kinematics.html) and [dyn4j](https://dyn4j.org/pages/advanced): kinematic conventions and planar dynamics references.

Mass, grip, torque, feed rate, bounce, drag and lift parameters need calibration against the physical robot. The public field dimensions come from FIELD-SOURCES.md; no exact field CAD collision mesh or referee system is implied.
