package com.skyinu.wardhere;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class ProguardJarFixTask {
    private Project project;

    public ProguardJarFixTask(Project project) {
        this.project = project;

    }

    public void start() {
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                Object appExtension = project.property("android");
                if (appExtension instanceof AppExtension) {
                    AppExtension real = (AppExtension) appExtension;
                    DomainObjectSet<ApplicationVariant> variants = real.getApplicationVariants();
                    configTask(variants);
                }
            }
        });
    }

    public void configTask(DomainObjectSet<ApplicationVariant> variants) {
        variants.iterator().forEachRemaining(new Consumer<ApplicationVariant>() {
            @Override
            public void accept(ApplicationVariant applicationVariant) {
                applicationVariant.getPackageApplicationProvider().get().doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        applicationVariant.getPackageApplicationProvider().get().getJavaResourceFiles().iterator().forEachRemaining(new Consumer<File>() {
                            @Override
                            public void accept(File file) {
                                zipFix(file);
                            }
                        });
                    }
                });
            }
        });
    }

    private void zipFix(File file) {
        project.getLogger().info("start to process file = " + file.getAbsolutePath());
        try {
            JarFile src = new JarFile(file);
            File destFile = new File(file.getParentFile(), file.getName() + ".bak");
            JarOutputStream destOut = new JarOutputStream(new FileOutputStream(destFile));
            src.stream().forEach(new Consumer<JarEntry>() {
                @Override
                public void accept(JarEntry jarEntry) {
                    try {
                        destOut.putNextEntry(jarEntry);
                    } catch (IOException e) {
                        project.getLogger().error(jarEntry.getName() + " " + jarEntry.getSize() + " " + e.getMessage());
                    }
                }
            });
            destOut.flush();
            file.delete();
            destFile.renameTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
