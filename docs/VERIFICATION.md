# Community verification

This version is tested with newly written generic examples, not competition robot source.

- Rebuild.ps1 -Test: physics, camera, mechanisms, motor/profile/input behavior, imported OpMode ownership, @Disabled filtering, helper/abstract/constructor rejection and Java source package validation.
- tests/onbot.cjs: clean startup, OnBot source ZIP import, UTF-8 BOM, helper subpackage, disabled OpMode filtering, failed-import preservation, restart, actual TeleOp motor movement and LinearOpMode execution.
- tests/camera.cjs: smooth driver/audience following and preserved orbit/overhead/chase.
- tests/practice-camera.cjs: free practice camera and guided UI.
- tests/input.cjs: standard/raw synthetic gamepad mapping.

The privacy audit checks the public file set and archive for imported robot sources, compiled team classes, import history, profiles, source manifests and original project history. The sharing ZIP is generated from a filtered tracked file list, never a working-folder ZIP.

Physical hardware and arbitrary third-party/Android APIs are not covered by the generic example tests.
