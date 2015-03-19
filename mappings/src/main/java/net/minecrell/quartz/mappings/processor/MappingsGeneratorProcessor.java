/*
 * Quartz
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Based on Sponge and SpongeAPI, licensed under the MIT License (MIT).
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.minecrell.quartz.mappings.processor;

import static com.google.common.base.Preconditions.checkArgument;
import static net.minecrell.quartz.mappings.processor.util.Elements.getDescriptor;
import static net.minecrell.quartz.mappings.processor.util.Elements.getInternalName;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecrell.quartz.mappings.Accessible;
import net.minecrell.quartz.mappings.Constructor;
import net.minecrell.quartz.mappings.Mapping;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({
        "net.minecrell.quartz.mappings.Accessible",
        "net.minecrell.quartz.mappings.Mapping",
        "net.minecrell.quartz.mappings.Constructor"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("baseJar")
public class MappingsGeneratorProcessor extends AbstractProcessor {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAPPINGS_TYPE = new TypeToken<Map<String, MappingInfo>>(){}.getType();

    private Path baseJar;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        /*Map<String, String> options = processingEnv.getOptions();
        checkArgument(options.containsKey("baseJar"), "Missing baseJar argument");
        this.baseJar = Paths.get(options.get("baseJar"));
        checkArgument(Files.exists(baseJar), "Base JAR does not exist");*/
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        List<TypeElement> mappingClasses = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(Mapping.class)) {
            if (element instanceof TypeElement) {
                mappingClasses.add((TypeElement) element);
            }
        }

        if (mappingClasses.isEmpty()) {
            return true;
        }

        try {
            FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "mappings.json");

            Map<String, MappingInfo> mappings;
            try (Reader reader = file.openReader(false)) {
                mappings = GSON.fromJson(reader, MAPPINGS_TYPE);
            } catch (IOException ignored) {
                mappings = new HashMap<>();
            }

            ClassMappings classMappings = readClassMappings(mappingClasses);

            // We need to remap the descriptors of the fields and methods, use ASM for convenience
            Remapper unmapper = classMappings.getUnmapper();

            for (TypeElement mappingClass : mappingClasses) {
                Mapping annotation = mappingClass.getAnnotation(Mapping.class);
                MappingInfo mapping = new MappingInfo(Strings.emptyToNull(annotation.value()));

                Accessible accessible = mappingClass.getAnnotation(Accessible.class);
                if (accessible != null) {
                    mapping.getAccess().put("", readAccess(mappingClass));
                }

                for (Element element : mappingClass.getEnclosedElements()) {
                    Constructor constructor = element.getAnnotation(Constructor.class);
                    if (constructor != null) {
                        if (element.getKind() != ElementKind.METHOD) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Constructor can be only used for methods", element);
                            continue;
                        }

                        String name = element.getSimpleName().toString();
                        if (!name.equals("create")) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Constructor methods should be called \"create\"",
                                    element);
                            continue;
                        }

                        ExecutableElement creator = (ExecutableElement) element;

                        if (!mappingClass.asType().equals(creator.getReturnType())) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Constructor method should return instance of itself",
                                    element);
                            continue;
                        }

                        mapping.getConstructors().add(getDescriptor((ExecutableElement) element));
                        continue;
                    }

                    annotation = element.getAnnotation(Mapping.class);
                    if (annotation == null) {
                        continue;
                    }

                    accessible = element.getAnnotation(Accessible.class);

                    String mappedName = annotation.value();
                    checkArgument(!mappedName.isEmpty(), "Mapping detection is not supported yet");

                    switch (element.getKind()) {
                        case METHOD:
                            ExecutableElement method = (ExecutableElement) element;
                            String methodName = method.getSimpleName().toString();
                            String methodDesc = getDescriptor(method);
                            mapping.getMethods().put(mappedName + unmapper.mapMethodDesc(methodDesc), methodName);

                            if (accessible != null) {
                                mapping.getAccess().put(methodName + methodDesc, readAccess(method));
                            }

                            break;
                        case FIELD:
                        case ENUM_CONSTANT:
                            VariableElement field = (VariableElement) element;
                            String fieldName = field.getSimpleName().toString();
                            mapping.getFields().put(mappedName + ':' + unmapper.mapDesc(getDescriptor(field)), fieldName);

                            if (accessible != null) {
                                mapping.getAccess().put(fieldName, readAccess(field));
                            }

                            break;
                        default:
                    }
                }

                mappings.put(getInternalName(mappingClass), mapping);
            }

            // Generate JSON output
            file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "mappings.json");
            try (Writer writer = file.openWriter()) {
                GSON.toJson(mappings, writer);
            }

            return true;
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Failed to create mappings.json", e);
        }
    }

    private static ClassMappings readClassMappings(List<TypeElement> mappingClasses) {
        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();

        for (TypeElement element : mappingClasses) {
            String mapping = element.getAnnotation(Mapping.class).value();
            if (!mapping.isEmpty()) {
                classes.put(mapping, getInternalName(element));
            }
        }

        return new ClassMappings(classes.build());
    }

    private static class MappingInfo {

        private final String name;
        private Map<String, String> methods;
        private Map<String, String> fields;
        private Map<String, AccessInfo> access;
        private Set<String> constructors;

        private MappingInfo(String name) {
            this.name = name;
        }

        public Map<String, String> getMethods() {
            if (methods == null) {
                this.methods = new HashMap<>();
            }

            return methods;
        }

        public Map<String, String> getFields() {
            if (fields == null) {
                this.fields = new HashMap<>();
            }

            return fields;
        }

        public Map<String, AccessInfo> getAccess() {
            if (access == null) {
                this.access = new HashMap<>();
            }

            return access;
        }

        public Set<String> getConstructors() {
            if (constructors == null) {
                this.constructors = new HashSet<>();
            }

            return constructors;
        }
    }

    private static AccessInfo readAccess(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        boolean isFinal = modifiers.contains(Modifier.FINAL);

        for (Modifier modifier : element.getModifiers()) {
            switch (modifier) {
                case PUBLIC:
                    return new AccessInfo(AccessModifier.PUBLIC, isFinal);
                case PROTECTED:
                    return new AccessInfo(AccessModifier.PROTECTED, isFinal);
                case PRIVATE:
                    return new AccessInfo(AccessModifier.PRIVATE, isFinal);
                default:
            }
        }

        return new AccessInfo(AccessModifier.PACKAGE_LOCAL, isFinal);
    }

    private static class AccessInfo {

        private final AccessModifier modifier;
        private final boolean isFinal;

        private AccessInfo(AccessModifier modifier, boolean isFinal) {
            this.modifier = modifier;
            this.isFinal = isFinal;
        }
    }

}
