package com.skyinu.wardhere;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;


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
        project.getLogger().error("start to process file = " + file.getAbsolutePath());
        try {
            File destFile = new File(file.getParentFile(), file.getName() + ".bak");
            File destFolder = new File(file.getParentFile(), file.getName() + "_bak");
            ensureFolderEmpty(destFolder);
            ZipUtils.unZip(file, destFolder.getAbsolutePath());
            ZipFile destZip = new ZipFile(destFile.getAbsolutePath());
            for (File item : destFolder.listFiles()) {
                if (item.isDirectory()) {
                    destZip.addFolder(item.getAbsolutePath(), new ZipParameters());
                } else {
                    destZip.addFile(item, new ZipParameters());
                }
            }
            boolean deleteSrcResult = file.delete();
            FileUtils.deleteDirectory(destFolder);
            boolean result = destFile.renameTo(file);
            project.getLogger().error("process end with " + deleteSrcResult
                    + " and " + result);
        } catch (ZipException | IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureFolderEmpty(File destFolder) {
        if (destFolder.exists()) {
            destFolder.delete();
        }
        destFolder.mkdir();
    }
}