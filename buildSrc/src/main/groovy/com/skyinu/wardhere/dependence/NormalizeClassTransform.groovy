package com.skyinu.wardhere.dependence;

import org.gradle.api.Project;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute

import java.util.function.BiConsumer

class NormalizeClassTransform {
  private Project project

  NormalizeClassTransform(Project project) {
    this.project = project
  }

  void proceed() {
    //step one, declare attribute
    Attribute<Boolean> normalize = Attribute.of("normalize", Boolean.class);
    project.getDependencies().getAttributesSchema().attribute(normalize);
    //step two 	make all JAR files are not normalized
    project.getDependencies()
        .getArtifactTypes()
        .getAsMap()
        .forEach(new BiConsumer<String, ArtifactTypeDefinition>() {
          @Override
          void accept(String name, ArtifactTypeDefinition artifactTypeDefinition) {
            if (name.equals(ArtifactTypeDefinition.JAR_TYPE)) {
              artifactTypeDefinition.getAttributes().attribute(normalize, false)
            }
          }
        })
    //step three Request normalize=true on all resolvable configurations
    project.afterEvaluate { project ->
      project.getConfigurations().all { files ->
        if (files.isCanBeResolved()) {
          files.getAttributes().attribute(normalize, true)
        }
      }
    }
    //step four
    project.getDependencies()
        .registerTransform(NormalizeAction.class) { normalizeTransformParametersTransformSpec ->
          normalizeTransformParametersTransformSpec.getFrom().attribute(normalize, false)
          normalizeTransformParametersTransformSpec.getTo().attribute(normalize, true)
          normalizeTransformParametersTransformSpec.parameters.buildDir = project.buildDir.absolutePath
        }
  }
}
