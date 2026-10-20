package virtual_robot.biobuzz;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

/** Shared eligibility rule for the Java selector and Studio API. */
public final class TeamOpModes {
    private TeamOpModes() {}

    public static boolean isRunnable(Class<?> type, Path importedSources) {
        if (!OpMode.class.isAssignableFrom(type)
                || type.isAnnotationPresent(Disabled.class)
                || !(type.isAnnotationPresent(TeleOp.class) || type.isAnnotationPresent(Autonomous.class))
                || !Modifier.isPublic(type.getModifiers())
                || Modifier.isAbstract(type.getModifiers())) return false;
        try {
            type.getConstructor();
        } catch (NoSuchMethodException e) {
            return false;
        }
        // Demos and tuning tools share the teamcode namespace, so package alone
        // cannot establish ownership. Only imported source files belong to the team.
        Class<?> sourceType = type;
        while (sourceType.getEnclosingClass() != null) sourceType = sourceType.getEnclosingClass();
        return Files.isRegularFile(importedSources.resolve(sourceType.getName().replace('.', '/') + ".java"));
    }
}
