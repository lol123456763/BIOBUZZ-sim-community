package virtual_robot.biobuzz;

import java.nio.file.Path;
import java.util.regex.Pattern;

/** Source layout shared by OnBot Java downloads and Android Studio TeamCode. */
public record JavaSource(Path relativePath, String content) {
    private static final Pattern PACKAGE = Pattern.compile("(?m)^\\s*package\\s+(org\\.firstinspires\\.ftc\\.teamcode(?:\\.[A-Za-z_$][\\w$]*)*)\\s*;");

    public static JavaSource parse(String filename, String text) {
        String name = filename.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        if (!name.matches("[A-Za-z_$][\\w$]*\\.java")) throw new IllegalArgumentException("Expected a Java source filename");
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        var match = PACKAGE.matcher(text);
        if (!match.find()) throw new IllegalArgumentException("Expected package org.firstinspires.ftc.teamcode (or a subpackage) in " + name + ". Download the .java source from OnBot Java; compiled classes and APKs are not supported.");
        return new JavaSource(Path.of(match.group(1).replace('.', '/'), name), text);
    }
}
