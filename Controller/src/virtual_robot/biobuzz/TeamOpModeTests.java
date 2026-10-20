package virtual_robot.biobuzz;

import com.qualcomm.robotcore.eventloop.opmode.*;
import java.nio.file.*;
import org.reflections.Reflections;

public final class TeamOpModeTests {
    public static class Helper {}
    public static class Unannotated extends OpMode { public void init() {} public void loop() {} }
    @TeleOp public static class Tele extends Unannotated {}
    @Autonomous public static class Auto extends Unannotated {}
    @Disabled @TeleOp public static class DisabledTele extends Unannotated {}
    @Disabled @Autonomous public static class DisabledAuto extends Unannotated {}
    @TeleOp public abstract static class AbstractTele extends Unannotated {}
    @TeleOp public static class NoDefaultConstructor extends Unannotated { public NoDefaultConstructor(int n) {} }
    @TeleOp private static class PrivateTele extends Unannotated {}
    @TeleOp public static class AnnotatedHelper {}

    private static void check(boolean pass, String message) {
        if (!pass) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory(Path.of("build"), "opmode-test-");
        Path source = root.resolve(TeamOpModeTests.class.getName().replace('.', '/') + ".java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "// Ownership fixture; annotations are read from compiled classes.");
        check(TeamOpModes.isRunnable(Tele.class, root), "Imported TeleOp allowed");
        check(TeamOpModes.isRunnable(Auto.class, root), "Imported autonomous allowed");
        for (Class<?> type : new Class<?>[]{DisabledTele.class, DisabledAuto.class, Helper.class,
                Unannotated.class, AbstractTele.class, NoDefaultConstructor.class, PrivateTele.class, AnnotatedHelper.class}) {
            check(!TeamOpModes.isRunnable(type, root), "Must reject " + type.getName());
        }
        Files.delete(source);
        check(!TeamOpModes.isRunnable(Tele.class, root), "Missing imported source never falls back to demos");

        Path imported = Path.of("ImportedTeamCode");
        var candidates = new Reflections("org.firstinspires.ftc.teamcode").getSubTypesOf(OpMode.class);
        var runnable = candidates.stream().filter(c -> TeamOpModes.isRunnable(c, imported))
                .map(Class::getName).sorted().toList();
        check(runnable.stream().noneMatch(n -> n.contains("StudioDriveProbe")), "Bundled diagnostic excluded");
        for (Class<?> type : candidates) check(!TeamOpModes.isRunnable(type, root), "Empty import excludes every program");
        System.out.println("PASS: imported TeleOp/autonomous, @Disabled, helper/abstract/constructor filters, empty import, bundled demos/tuners");
        System.out.println("Runnable team programs (" + runnable.size() + "): " + runnable);
        check(JavaSource.parse("download/MyOp.java", "\uFEFFpackage org.firstinspires.ftc.teamcode; class MyOp {}").relativePath().toString().replace('\\','/').endsWith("teamcode/MyOp.java"), "OnBot source with BOM and arbitrary download path");
        check(JavaSource.parse("Helper.java", "package org.firstinspires.ftc.teamcode.helpers; class Helper {}").relativePath().toString().replace('\\','/').contains("teamcode/helpers/"), "OnBot helper package preserved");
        try { JavaSource.parse("Bad.java", "package org.firstinspires.ftc.teamcodeevil; class Bad {}"); throw new AssertionError("Package boundary"); } catch (IllegalArgumentException expected) {}
        System.out.println("PASS: OnBot Java source layouts, UTF-8 BOM and package boundary validation");
    }
}
