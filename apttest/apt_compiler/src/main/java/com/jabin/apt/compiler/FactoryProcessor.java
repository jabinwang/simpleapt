package com.jabin.apt.compiler;

import com.google.auto.service.AutoService;
import com.jabin.apt.annotation.Factory;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {


    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, Map<String, String>> groupMap = new HashMap<>();
        messager.printMessage(Diagnostic.Kind.NOTE, "process" + roundEnv.getElementsAnnotatedWith(Factory.class).size());
        for (TypeElement e :
                annotations) {
            messager.printMessage(Diagnostic.Kind.NOTE, "annoattion " + e.getQualifiedName());
        }
        for (Element ele :
                roundEnv.getElementsAnnotatedWith(Factory.class)) {
            if (ele.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "must annotatin in class");
            }
            TypeElement typeElement = (TypeElement) ele;
            Factory factory = typeElement.getAnnotation(Factory.class);
            String id = factory.id();
            messager.printMessage(Diagnostic.Kind.NOTE, "id " + id);

            if (id.length() == 0) {
                messager.printMessage(Diagnostic.Kind.ERROR, "id must be not null");
            }
            String qualifiedSuperClassName = null;
            try {
                Class<?> type = factory.type();
                qualifiedSuperClassName = type.getCanonicalName();
            } catch (MirroredTypeException e) {
                DeclaredType classTypeMirror = (DeclaredType) e.getTypeMirror();
                TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
                qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            }

            messager.printMessage(Diagnostic.Kind.NOTE, "qualifiedSuperClassName" + qualifiedSuperClassName);
            Map<String, String> map = groupMap.get(qualifiedSuperClassName);
            if (map == null) {
                map = new HashMap<>();
                groupMap.put(qualifiedSuperClassName, map);
            }
            map.put(id, typeElement.getQualifiedName().toString());
        }

        generateCode(groupMap);
        return true;
    }

    private void generateCode(Map<String, Map<String, String>> map) {
        for (Map.Entry<String, Map<String, String>> entry :
                map.entrySet()) {
            //super class
            TypeElement typeElement = elementUtils.getTypeElement(entry.getKey());
            messager.printMessage(Diagnostic.Kind.NOTE, "typeElement " + typeElement.getQualifiedName());
            String factoryClassName = typeElement.getSimpleName() + "Factory";
            PackageElement packageElement = elementUtils.getPackageOf(typeElement);
            String packageName = packageElement.getQualifiedName().toString();

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(String.class, "id")
                    .returns(TypeName.get(typeElement.asType()));

            methodBuilder.beginControlFlow("if (id == null)")
                    .addStatement("throw new IllegalArgumentException($S)", "id is null")
                    .endControlFlow();

            for (Map.Entry<String, String> e :
                    entry.getValue().entrySet()) {
                methodBuilder.beginControlFlow("if ($S.equals(id))", e.getKey())
                        .addStatement("return new $L()", e.getValue())
                        .endControlFlow();
            }

            methodBuilder.addStatement("throw new IllegalArgumentException($S+id)", "unknow id = ");

            TypeSpec typeSpec = TypeSpec.classBuilder(factoryClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())
                    .build();

            try {
                JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
