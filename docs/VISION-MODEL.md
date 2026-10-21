# Limelight simulation model

This adapter supports common FTC Limelight SDK calls for locally imported programs. It does not execute camera firmware, Python pipelines, neural networks or AprilTag image decoding.

## Implemented behavior

- Pipeline 0 detects yellow floor POLLEN; pipeline 1 detects the exposed upper CELL tag cluster. Slots 2-9 return empty results.
- Configurable camera forward/right/height offset, yaw/pitch/roll, horizontal FOV, frame rate and processing latency. Positive yaw rotates counterclockwise from robot forward; positive pitch points up. Offsets are meters and angles degrees.
- 640 x 480 square-pixel pinhole projection with aspect-correct vertical FOV; tx positive right, ty positive up. Ball corners are bounding rectangles; percentage area approximates the circular silhouette. Targets outside the image, behind the camera or smaller than 2 pixels are rejected.
- Robot-space target translation is right/forward/up, with camera mount removed. Camera-space translation uses legacy right/down/forward. Target rotations remain unmodeled (zero).
- Field pose is ideal delayed ground truth, available only with detected tags. Field yaw is zero along +x and positive counterclockwise; north-facing chassis is 90 degrees. MegaTag2 requires supplied robot orientation and returns that yaw. This is not the actual MegaTag estimator.
- Capture FPS and polling cadence are separate. Pipeline changes arrive asynchronously after capture, processing and polling. Staleness is time since receipt; targeting latency is configured processing delay in milliseconds. Frozen/paused results keep aging, while reset/restart clears queued frames.
- `BioBuzzBot.visionDiagnostics()` reports actual delivered pipeline, validity, angles, area, IDs, ball corners, receipt age and timing settings.

## Official CAD measurements

Tag board positions and normals come from official AndyMark STEP parts `am-5888-red1/red2/blue1/blue2`. Upper board center height is 1.261519155 m (49.666 in), field y is +/-0.327341117 m, and lane x centers are -0.323637681 m and +0.324062319 m. These replace the prior guessed 51.94-inch height and CELL-center depth. The underside plane normal is (0, side*0.5, -sqrt(3)/2). Backface rejection, projected area and edge-on resolution checks use that normal and the configured camera position.

STEP coordinates map to field(x,y,z)=(CAD x,-CAD z,CAD y). Centers and normals were checked from mesh bounds and triangle normals. The opposite stable HIVE state mirrors the upper board about the central pivot.

Printed geometry is measured from [FIRST AprilTag Production Art V1](https://ftc-resources.firstinspires.org/ftc/archive/2027/field/apriltag-art), not guessed. Each PDF page is 18 x 6 inches with a 17 x 5 inch board cutline. Each tag image is 292.5 PDF points wide and has 10 modules including a one-module white quiet border; the black 8-module square is 234 points = 3.25 inches. Horizontal tag-center offsets from board center are -6.5, -2.75, +2.75 and +6.5 inches. Their centers are .25 inch above board center in the board plane. ID order is left-to-right viewed from the printed face; scoring-side order is reversed in field x. These replace the previous guessed 3-inch tags and equally spaced centers.

Four board textures in `web/assets/field/apriltag-board-1.png` through `-4.png` are rasterized directly from the production art at 72 pixels/inch, cropped to its board cutline without rescaling. Page order is red scoring (30-33), red audience (34-37), blue audience (38-41), blue scoring (42-45). Artwork remains FIRST/AndyMark material; these are simulator display derivatives, not replacement official print files. Only exposed upper clusters are modeled; lower tags and other markers are absent, and tipping suppresses detections.

## Remaining limitations

No CAD/robot/ball occlusion ray tracing, lens distortion, exposure, blur, lighting response, image noise, packet loss, pose ambiguity or calibration uncertainty is modeled. Ground-truth localization and approximate sphere areas are not hardware-equivalent measurements. Defaults (85-degree horizontal FOV, 30 FPS, 60 ms delay and mounting) are editable assumptions, not measured Limelight specifications. Configure the mount for the targets you want to observe; high HIVE tags require an upward view from typical robot heights.

## Using pipelines

Select pipeline 1 for AprilTags and pipeline 0 for yellow floor targets. Your program must start the camera, verify result freshness and pipeline, and inspect fiducial IDs before using a result for tag alignment. No competition alignment or autonomous implementation is bundled.

## References and verification

The [official FTC API guide](https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming) documents asynchronous pipeline changes, target area percentages, orientation input and staleness. The [official localization guide](https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-robot-localization) explains mount configuration and real pose estimation.

`virtual_robot.biobuzz.VisionTests` covers projection, mounts, latency, polling, pipeline transitions, reset/restart/freeze. One test waits 210 ms to verify wall-time staleness during pause. These are simulator tests, not camera hardware validation.

Production-art PDF SHA-256: `be9451596b2a08eda8d22041ae4451feb3946b100fe4ed4d64c5ff24e694cf32`.
