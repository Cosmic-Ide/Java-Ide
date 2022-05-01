package com.pranav.lib_android.task.java;

import android.content.Context;
import android.content.SharedPreferences;

import com.pranav.lib_android.exception.CompilationFailedException;
import com.pranav.lib_android.interfaces.*;
import com.pranav.lib_android.util.ConcurrentUtil;
import com.pranav.lib_android.util.FileUtil;
import com.sun.tools.javac.Main;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.file.JavacFileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.StandardLocation;

public class JavacTask extends Task {

    private final StringBuilder errs = new StringBuilder();
    private final StringBuilder warnings = new StringBuilder();
    private final SharedPreferences prefs;

    public JavacTask(Builder builder) {
        prefs =
                builder.getContext()
                        .getSharedPreferences("compiler_settings", Context.MODE_PRIVATE);
    }

    @Override
    public String getTaskName() {
        return "Javac Task";
    }

    @Override
    public void doFullTask() throws Exception {

        final File output = new File(FileUtil.getBinDir(), "classes");
        final String version = prefs.getString("javaVersion", "7.0");

        ConcurrentUtil.execute(
                () -> {
                    DiagnosticListener<JavaFileObject> diagnosticCollector =
                            diagnostic -> {
                                switch (diagnostic.getKind()) {
                                    case ERROR:
                                        getLogger().error(new DiagnosticWrapper(diagnostic));
                                        break;
                                    case WARNING:
                                        getLogger().warning(new DiagnosticWrapper(diagnostic));
                                }
                            };

                    List<JavaFileObject> javaFileObjects = new ArrayList<>();
                    List<File> javaFiles = getSourceFiles(new File(FileUtil.getJavaDir()));
                    for (File file : javaFiles) {
                        javaFileObjects.add(new SourceFileObject(file.toPath()));
                    }

                    JavacTool tool = JavacTool.create();

                    JavacFileManager standardJavaFileManager =
                            tool.getStandardFileManager(
                                    diagnosticCollector,
                                    Locale.getDefault(),
                                    Charset.defaultCharset());
                    try {
                        standardJavaFileManager.setLocation(
                                StandardLocation.CLASS_OUTPUT, Collections.singletonList(output));
                        standardJavaFileManager.setLocation(
                                StandardLocation.PLATFORM_CLASS_PATH,
                                getPlatformClasspath(version));
                        standardJavaFileManager.setLocation(
                                StandardLocation.CLASS_PATH, getClassPath());
                        standardJavaFileManager.setLocation(
                                StandardLocation.SOURCE_PATH, javaFiles);
                    } catch (IOException e) {
                        throw new CompilationFailedException(e);
                    }

                    final ArrayList<String> args = new ArrayList<>();

                    args.add("-g");
                    args.add("-source");
                    args.add(version);
                    args.add("-target");
                    args.add(version);

                    args.add("-proc:none");

                    JavacTask task =
                            tool.getTask(
                                    null,
                                    standardJavaFileManager,
                                    diagnosticCollector,
                                    args,
                                    null,
                                    javaFileObjects);

                    if (!task.call()) {
                        throw new CompilationFailedException(errs.toString());
                    }
                    Main.compile(args.toArray(new String[0]), writer);
                });
        String errors = errs.toString();

        if (!errors.isEmpty()) {
            throw new CompilationFailedException(errors);
        }
    }

    public ArrayList<File> getSourceFiles(File path) {
        ArrayList<File> sourceFiles = new ArrayList<>();
        File[] files = path.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    sourceFiles.add(file);
                }
            } else {
                sourceFiles.addAll(getSourceFiles(file));
            }
        }
        return sourceFiles;
    }

    public List<File> getClasspath() {
        List<File> classpath = new ArrayList<>();
        final StringBuilder path = new StringBuilder();
        final String clspath = prefs.getString("classpath", "");
        if (!clspath.isEmpty() && classpath.length() > 0) {
            path.append(":");
            cpath.append(clspath);
        }

        for (String clas : path.toString().split(":")) {
            classpath.add(new File(clas));
        }
        return classpath;
    }

    public List<File> getPlatformClasspath(String version) {
        List<File> classpath = new ArrayList<>();
        classpath.add(new File(FileUtil.getClasspathDir(), "android.jar"));
        if (version.equals("8.0")) {
            classpath.add(new File(FileUtil.getClasspathDir(), "core-lambda-stubs.jar"));
        }
        return classpath;
    }
}
