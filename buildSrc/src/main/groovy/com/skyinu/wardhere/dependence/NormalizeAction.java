package com.skyinu.wardhere.dependence;

import net.lingala.zip4j.ZipFile;
import java.io.File;
import javax.inject.Inject;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

abstract class NormalizeAction implements TransformAction<NormalizeTransformParameters> {
  NormalizeTransformParameters parameters;

  @Inject
  public NormalizeAction(NormalizeTransformParameters parameters) {
    this.parameters = parameters;
  }

  @Override public NormalizeTransformParameters getParameters() {
    return parameters;
  }

  @PathSensitive(PathSensitivity.NAME_ONLY)
  @InputArtifact
  public abstract Provider<FileSystemLocation> getInputArtifact();

  @Override public void transform(TransformOutputs transformOutputs) {
    File out = transformOutputs.file(getInputArtifact());
    try {
      ZipFile zipFile = new ZipFile(out);
      zipFile.extractFile("com/google/gson/Gson.class",
          getParameters().getBuildDir());
      zipFile.removeFile("com/google/gson/Gson.class");
      System.out.println("location is " + getInputArtifact().get().getAsFile().getAbsolutePath());
      System.out.println("succeed");
      System.out.println("output location is " + out.getAbsolutePath());
    } catch (Exception ignored) {
    }
  }
}
