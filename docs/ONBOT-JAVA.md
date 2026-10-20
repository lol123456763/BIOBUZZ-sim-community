# OnBot Java source support

OnBot Java programs are FTC Java OpModes. This simulator accepts downloaded .java files and Java-source ZIPs; it does not require Gradle or an Android Studio project.

1. Download your source from the OnBot Java editor, including helper files.
2. In Programs, choose Java files, Source folder, or OnBot Java / TeamCode source ZIP.
3. Review the selection and compile. Restart the Java runner after success.
4. Choose an enabled program in Drive, initialize, then start.

Both iterative OpMode and LinearOpMode are supported through the desktop SDK approximation. @Disabled programs and helper classes never appear as runnable choices. Java package paths are reconstructed automatically, including helper subpackages; UTF-8 byte-order marks are accepted.

The generic examples under web/examples/onbot are newly written simulator examples and can be uploaded to OnBot Java as ordinary source. Match hardware configuration names before running on a real robot; the simulator's estimated constants are not robot tuning.

Scope: source compatibility for the FTC APIs and hardware implemented by this desktop simulator. Unsupported Android classes, SDK methods, hardware, external dependencies and Blocks programs are not automatically converted. Error messages preserve the old working source/build. No robot deployment is performed.

Official reference: [FIRST OnBot Java tutorial](https://ftc-docs.firstinspires.org/en/latest/programming_resources/onbot_java/OnBot-Java-Tutorial.html).
