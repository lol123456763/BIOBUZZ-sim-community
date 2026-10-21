# BIOBUZZ Community Simulator

A local Windows FTC simulator for feedback, practice, and Java OpMode experiments. Supports source exported from OnBot Java and Android Studio.

Double-click **Start Simulator.cmd** after extracting the entire repository. Open **Programs**, choose Java files or a source ZIP, compile, then restart the Java runner. **Try generic example programs** provides sample driving, LinearOpMode, read-only AprilTag camera and helper code.

The repository starts with no imported robot programs. It contains no team competition TeleOp, autonomous, subsystem implementation, or old project history. Imported code stays local in the ignored ImportedTeamCode folder. Builds, import backups, profiles and source ZIPs are also ignored.

- Guided program selection and run controls.
- Free practice camera; smooth driver-station tracking for matches.
- Two gamepads and keyboard controls.
- Configurable mecanum dynamics, mechanisms, field balls and camera estimates.
- STEP/STL robot models and local profiles.
- FIRST's actual BIOBUZZ field CAD and printed AprilTag artwork, bundled offline with source provenance.
- CAD-aligned collision proxies, improved projectile/drivetrain dynamics and configurable Limelight projection/timing.
- Live camera diagnostics and optimized graphics, with original CAD tessellation available in High quality.

See [START-HERE.md](START-HERE.md), [OnBot Java support](docs/ONBOT-JAVA.md), [privacy and sharing](docs/PRIVACY.md), and [verification](docs/VERIFICATION.md).

This is a desktop approximation of FTC hardware, not the OnBot Java editor or a Control Hub runtime. Android-only APIs, unsupported hardware, Blocks programs and arbitrary external libraries may need adapters. Hardware names must match the simulated configuration.

Based on [Beta8397/virtual_robot](https://github.com/Beta8397/virtual_robot). Preserve [upstream notices](THIRD-PARTY.md) when redistributing.
