# Simulation fidelity and calibration

The simulator is a development and driver-practice aid. Real field geometry does not make an uncalibrated robot model a digital twin. Measured robot and game-piece data are still needed before using predicted trajectories or stopping distances to make hardware decisions.

## Physical model

- SI units internally; gravity is 9.80665 m/s^2. Ball flight uses RK4 with quadratic aerodynamic drag and an empirical, capped Magnus lift model. Substeps are at most 1/480 second, and longer caller frames retain their complete elapsed time.
- Projectiles inherit both chassis translation and the tangential velocity of their offset launch point. Launch speed remains a calibrated wheel-speed multiplier, with a separate flywheel energy-loss estimate.
- Floor contact uses Coulomb sliding friction and solid-sphere rotational inertia (I = 2mr^2/5), consistent with the existing bounce model. Sliding friction changes translation and spin until the contact point stops slipping. Rolling resistance then integrates distance through the exact stop time, without reversing velocity.
- Restitution and impact friction describe ball bounces. Pair contacts use impulse exchange. Walls only reverse velocity when the ball is moving into the wall; projection of existing overlap does not bounce a separating ball back inward.
- The drivetrain is a planar dyn4j rigid body. Motor torque follows a voltage/speed curve with a rated-torque cap. External reduction changes both wheel torque/speed and motor encoder feedback.
- Translation and yaw share a conservative friction-ellipsoid budget. Forward/strafe coefficients may differ. This assumes equal static wheel loading; simultaneous full translation and full yaw no longer each receive the entire traction budget.

The conservative perimeter collision prism uses the official rail envelope at +/-1.792859 m, with top height 0.29575666 m. Both chassis and balls use that boundary; the glass itself is 2.26 mm farther out, so this remains a deliberately conservative contact approximation. The nominal 144-inch pixel conversion is retained only for the legacy 2D view/other games. Frame-foot chassis obstacles use CAD bounding rectangles.

The support-leg collision centerlines now taper in both horizontal axes, fitted to the official v26-27.2 A-Frame Leg mesh, rather than staying at a constant x coordinate. A Top Bar capsule is included. FLOWER centers and capture-top height follow the CAD placement. These are simplified capsule/capture proxies, not full mesh collision. The CAD coordinate transform is (x, y, z) -> simulation (x, -z, y); no scale fitting is applied.

## What remains approximate

- The HIVE mouth, lips, support contacts, FLOWER capture and tipping are reduced-order shapes and rule-based state transitions. Rendering an official CAD mesh does **not** turn every mesh triangle into a rigid-body collision surface. Decorative CAD detail has no contact response.
- Balls use solid-sphere inertia, not a measured deformable foam or shell model. Restitution, rolling/sliding friction, aerodynamic lift/drag, spin decay, wheel slip and shot efficiency are estimates. No deformation, wear, seam orientation or material-specific contact curve is simulated.
- Chassis motion is planar: no suspension, pitch/roll, wheel lift or load transfer. Wheel encoders derive from chassis motion and do not independently measure spinning/slipping wheels. Ball impacts against the chassis do not impart equal-and-opposite momentum to the much heavier robot.
- Battery voltage is a setpoint, not a circuit with current-dependent sag, motor heating or controller current limits. The torque cap is a model bound, not a measured controller specification. Coast and brake parameters require measured deceleration data.
- Robot pose is supplied once per caller frame; ball substeps do not extrapolate a moving chassis during that frame. Very large elapsed-time updates therefore retain elapsed time but are not a substitute for regular physics updates.
- Intake and scoring captures are geometric/dwell tests; mechanisms do not deform or jam. HIVE tipping is a calibrated load threshold and timed release, not a solved hinge/counterweight mechanism.

## Useful calibration measurements

Measure loaded robot mass and yaw inertia, wheel diameter under load, actual gearing, free speed at measured battery voltage, forward/strafe acceleration and stopping distance in BRAKE/FLOAT, then projectile mass/diameter, exit velocity versus shooter speed, launch angle/height, spin and bounce/rolling behavior. Fit estimates to these measurements and validate with separate trials rather than reusing the fitting data.

## Regression coverage

`python build_sim.py --test` compiles the complete simulator and runs the physics checks. Tests include vacuum trajectories, drag direction, intake/gate behavior, HIVE transitions, slide-to-roll velocity and no-slip condition, analytic stopping distance, preserved elapsed time and frame batching, rotating muzzle velocity, combined traction demand, reduction torque/speed/power consistency, and braking versus coasting. These checks establish mathematical consistency; they do not establish agreement with a measured physical robot.
