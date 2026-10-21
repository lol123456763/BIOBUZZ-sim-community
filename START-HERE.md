# BIOBUZZ Community Simulator — Windows

1. Extract the entire download. Double-click **Start Simulator.cmd**.
2. The bundled Java compiler builds the simulator, then opens http://127.0.0.1:8765.
3. Open **Programs**. Choose OnBot Java source files, a source ZIP, an extracted source folder, or Android Studio TeamCode.
4. Include all helper classes. Review the selected files, choose **Import & compile selected code**, then **Restart Java runner**.
5. In **Drive**, choose a session and program, press **Initialize**, then **Start**. Click the field to use keyboard driving.

The clean distribution has no imported robot code. **Try generic example programs** loads newly written shareable examples into the source list; compile and restart to use them.

## OnBot Java

Download .java source (or a source ZIP) from OnBot Java on your Control Hub/Robot Controller. Import those files here; no Android Studio project or Gradle files are required. Files use package org.firstinspires.ftc.teamcode or a subpackage. Helper files can be in arbitrary source folders: their Java package determines their installed location.

The desktop **Import Robot Code.cmd** also accepts an extracted source folder. From PowerShell, Import-Code.ps1 -Source accepts a folder, .java file, or .zip. A failed compilation preserves the previous installed sources and runnable build.

This runs exported source on a desktop, not the OnBot editor itself. Unsupported APIs and Android-only libraries produce compiler errors. It does not deploy code or connect to your robot.

## Hardware names

The included mecanum robot exposes front_left_motor, front_right_motor, back_left_motor and back_right_motor, with camelCase aliases frontLeftMotor, frontRightMotor, backLeftMotor and backRightMotor. Mechanisms expose shooter, intake, windmill and gate. Synthetic devices include limelight and pinpoint.

Match your source hardware names to these simulated names. The example uses the underscored motor names. Real hardware settings and measured constants still need independent testing on the robot.

## Controls and cameras

Keyboard: WASD/arrows drive; Q/E turn. The generic example uses gamepad 1 to drive and gamepad 2's right trigger for a demonstration motor. In Gamepads, choose the keyboard mechanism destination if needed. Your imported code determines what inputs do.

Practice starts with a free camera: drag to orbit, right-drag/Shift-drag to pan, scroll to zoom. **Find robot** recenters the view. **Camera & sound** provides movement buttons and exact position inputs. Match sessions use your alliance station and track the robot; returning to practice restores its previous camera pose.

**Emergency stop** or Escape stops the program. Hide or reopen the panel with **Controls** or H. Practice has no timer; match sessions last 30 or 120 seconds.

## Profiles and robot models

Robot setup stores editable estimates and profiles. Apply settings while stopped. Robot model imports STEP/STL and estimates dimensions and masses for review. These are simplified outer-footprint dynamics, not articulated CAD collision or calibrated material measurements.

Imported CAD stays in this browser's local storage. Profiles stay under the ignored profiles folder.

## Sharing

Share only the public clean repository or a ZIP made by **Export-Share.ps1**. Do not ZIP your live working folder: it can contain local robot source, compiled classes, import backups and profiles. Those local folders are intentionally excluded from Git.

Keep competition repositories private. See docs/PRIVACY.md. This public repository begins with a fresh history and does not include any historical competition code.

## Practical limits

One configurable robot, simplified field contacts and provisional scoring. No opponent simulation or complete referee scoring. Physics, synthetic vision, motor properties and mechanism dimensions require calibration. Physical controllers and each team's actual CAD/hardware still need validation.

## Official field and camera diagnostics

The field uses FIRST's BIOBUZZ v26-27.2 CAD and official printed AprilTag artwork. Balanced and Performance use optimized CAD; High preserves the original tessellation. Graphics settings do not change physics. See FIELD-SOURCES.md for provenance and docs/REALISM.md for collision/calibration limits.

For a camera demonstration, load the generic examples in Programs, compile and restart. Select Shared Vision. While stopped, set camera pitch to 40 degrees in Robot setup > Camera & alliance and apply. Initialize and Start, then open Drive > Limelight diagnostics. At the default starting position this shows the high HIVE tags. This angle is a demo setting, not a real camera calibration. The example only reads detections and telemetry; it never drives or aims the robot.

Pipeline 1 models AprilTags; pipeline 0 models yellow floor targets. Set camera mount, FOV, frame rate and latency to match your hardware. Results are geometric estimates, not actual image processing. See docs/VISION-MODEL.md.

The download starts without imported programs, saved profiles, compiled robot code or import history. Use Export-Share.ps1 for redistribution; do not ZIP a live working folder.
