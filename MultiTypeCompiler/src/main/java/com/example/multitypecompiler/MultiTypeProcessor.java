package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.Keep;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateLayoutInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateTypeInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectTypeMethodInfo;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class MultiTypeProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private String generatedPackageName = "com.example.ran.multitype";
    private String generatedClassName = "AdapterTypeIndex";
    private String adapterPackage = "com.example.multitypelib";
    private String adapterString = "AbsDelegationAdapter";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        List<TypeNode> delegateTypeInfos = collectDelegateTypeInfo(env);
        Map<TypeElement, ExecutableElement> typeMethods = collectTypeMethodInfo(env);
        Map<TypeElement, Integer> delegateLayouts = collectDelegateLayoutInfo(env);


        if (delegateTypeInfos == null || delegateTypeInfos.isEmpty()) {
            return false;
        }

        TypeSpec.Builder classBuilder = generateClass();

        classBuilder.addMethod(generateSetAdapterMethod().build());
        classBuilder.addMethod(generateSingletonMethod().build());
        classBuilder.addMethod(generateGetItemTypeMethod(typeMethods).build());
        classBuilder.addMethod(generateGetImplicitDelegatesMethod(delegateTypeInfos).build());
        classBuilder.addMethod(generateGetItemTypeFunctionMethod(typeMethods).build());
        classBuilder.addMethod(generateGetTypeMethod(delegateTypeInfos, typeMethods).build());
        classBuilder.addMethod(generateGetDelegateMethod(delegateTypeInfos).build());
        classBuilder.addMethod(generateGetDelegateLayoutMethod(delegateLayouts).build());

        return !createInfoFile(annotations, env, generatedPackageName, classBuilder);
    }

    private TypeSpec.Builder generateClass() {
        ClassName generatedFullName = ClassName.get(generatedPackageName, generatedClassName);
        ClassName adapterFullName = ClassName.get(adapterPackage, adapterString);

        return TypeSpec.classBuilder(generatedFullName)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(adapterFullName, "adapter", Modifier.PRIVATE)
                    .addField(generatedFullName, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                    .addAnnotation(Keep.class)
                    .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .build());
    }

    private MethodSpec.Builder generateSetAdapterMethod() {
        ClassName adapterClass = ClassName.get(adapterPackage, adapterString);
        return MethodSpec
                .methodBuilder("setAdapter")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(adapterClass, "adapter")
                .addStatement("this.$N = $N", "adapter", "adapter")
                .returns(void.class);
    }

    private MethodSpec.Builder generateSingletonMethod() {
    ClassName generatedFullName = ClassName.get(generatedPackageName, generatedClassName);

    MethodSpec.Builder singletonMethodBuilder = MethodSpec
            .methodBuilder("getInstance")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(generatedFullName)
            .addCode("if ($N == null) {\n", "instance")
            .addCode("$>synchronized ($T.class) {\n", generatedFullName)
            .addCode("$>if ($N == null) {\n", "instance")
            .addStatement("$>$N = new $T()", "instance", generatedFullName)
            .addCode("$<}\n$<}\n$<}\n\n")
            .addStatement("return $N", "instance");


    return singletonMethodBuilder;
}

    private MethodSpec.Builder generateGetDelegateMethod(List<TypeNode> typeInfo) {
        String delegateString = "AdapterDelegate";
        ClassName delegateClassName = ClassName.get(adapterPackage, delegateString);
        ClassName adapterFullName = ClassName.get(adapterPackage, adapterString);

        MethodSpec.Builder getDelegateMethodBuilder = MethodSpec
                    .methodBuilder("getDelegateByViewType")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "viewType")
                    .returns(delegateClassName)
                    .addStatement("$T classType = null", Class.class)
                    .beginControlFlow("switch ($L)", "viewType");

        for (int i = 0; i < typeInfo.size(); i++) {
            ClassName delegateClass = ClassName.get(typeInfo.get(i).element);

            getDelegateMethodBuilder.addCode("case $L:\n", i)
                    .addStatement("$>$N = $T.class", "classType", delegateClass)
                    .addStatement("break")
                    .addCode("$<");
        }

        getDelegateMethodBuilder.addCode("default:\n")
                    .addStatement("$>break")
                    .addCode("$<")
                    .endControlFlow()
                    .beginControlFlow("\ntry")
                    .addStatement("$T $N = $N.getDeclaredConstructor($T.class, $T.class)",
                            Constructor.class, "constructor", "classType", adapterFullName, int.class)
                    .addStatement("$T layoutId = $N($N)", int.class, "getDelegateLayout", "classType")
                    .addStatement("return ($T)$N.newInstance($N, $N)", delegateClassName, "constructor",
                            "adapter", "layoutId")
                    .addCode("$<} catch ($T $N) {\n", Exception.class, "ex")
                    .addStatement("$>$N.printStackTrace()", "ex")
                    .endControlFlow()
                    .addCode("\n")
                    .addStatement("return null");

        return getDelegateMethodBuilder;
    }

    private MethodSpec.Builder generateGetTypeMethod(List<TypeNode> typeInfo, Map<TypeElement, ExecutableElement> typeMethods) {
        MethodSpec.Builder getTypeMethodBuilder = MethodSpec
                .methodBuilder("getItemViewType")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "item")
                .returns(int.class)
                .addStatement("$T clazzString = $N.getClass().getName()", String.class, "item")
                .addStatement("$T $N = $N($N)", Class.class, "itemClass", "getItemType", "item")
                .addCode("\n")
                .addStatement("$T $N = $L", int.class, "subType", -1)
                .beginControlFlow("if ($N != null)", "itemClass")
                .beginControlFlow("try")
                .addStatement("$T $N = $N($N)", String.class, "methodName", "getItemTypeFunction", "itemClass")
                .addStatement("$T $N = $N.getDeclaredMethod($N)", Method.class, "method", "itemClass", "methodName")
                .addStatement("$N = ($T)$N.invoke($N)", "subType", int.class, "method", "item")
                .addCode("$<} catch ($T $N) {\n", NoSuchMethodException.class, "ex")
                .addStatement("$>$N.printStackTrace()", "ex")
                .addCode("$<} catch ($T $N) {\n", Exception.class, "ex")
                .addStatement("$>$N.printStackTrace()", "ex")
                .endControlFlow()
                .endControlFlow()
                .addCode("\n")
                .addStatement("$N += $S + String.valueOf($L)", "clazzString", "$", "subType")
                .beginControlFlow("switch ($N)", "clazzString");

        for (int i = 0; i < typeInfo.size(); i++) {
            getTypeMethodBuilder.addCode("case $S:\n", typeInfo.get(i).typeString + "$" + String.valueOf(typeInfo.get(i).subType))
                    .addCode("$>return $L;\n", i)
                    .addCode("$<");
        }

        getTypeMethodBuilder.addCode("default:\n$>return $L;\n", -1)
                            .addCode("$<")
                            .endControlFlow();

        return getTypeMethodBuilder;
    }

    private MethodSpec.Builder generateGetItemTypeMethod(Map<TypeElement, ExecutableElement> typeMethods) {
        MethodSpec.Builder getItemTypeMethodBuilder = MethodSpec
                .methodBuilder("getItemType")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Object.class, "item")
                .returns(Class.class)
                .addStatement("$T clazzString = $N.getClass().getName()", String.class, "item")
                .beginControlFlow("switch ($N)", "clazzString");

        for (Map.Entry<TypeElement, ExecutableElement> entry : typeMethods.entrySet()) {
            getItemTypeMethodBuilder.addCode("case $S:\n", entry.getKey().asType().toString())
                    .addCode("$>return $T.class;\n", ClassName.get(entry.getKey()))
                    .addCode("$<$<");
        }

        getItemTypeMethodBuilder.addCode("$>default:\n$>return null;\n")
                .addCode("$<")
                .endControlFlow();

        return getItemTypeMethodBuilder;
    }

    private MethodSpec.Builder generateGetItemTypeFunctionMethod(Map<TypeElement, ExecutableElement> typeMethods) {
        MethodSpec.Builder getItemTypeFunctionMethodBuilder = MethodSpec
                .methodBuilder("getItemTypeFunction")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Class.class, "clazz")
                .returns(String.class)
                .beginControlFlow("switch ($N.getName())", "clazz");

        for (Map.Entry<TypeElement, ExecutableElement> entry : typeMethods.entrySet()) {
            getItemTypeFunctionMethodBuilder.addCode("case $S:\n", entry.getKey().asType().toString())
                    .addCode("$>return $S;\n", entry.getValue().getSimpleName())
                    .addCode("$<");
        }

        getItemTypeFunctionMethodBuilder.addCode("default:\n$>return null;\n")
                .addCode("$<")
                .endControlFlow();

        return getItemTypeFunctionMethodBuilder;
    }

    private MethodSpec.Builder generateGetDelegateLayoutMethod(Map<TypeElement, Integer> layouts) {
        MethodSpec.Builder getDelegateLayoutMethodBuilder = MethodSpec
                .methodBuilder("getDelegateLayout")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Class.class, "delegateClass")
                .returns(int.class)
                .beginControlFlow("switch ($N.getName())", "delegateClass");

        for (Map.Entry<TypeElement, Integer> entry : layouts.entrySet()) {
            getDelegateLayoutMethodBuilder.addCode("case $S:\n", entry.getKey().asType().toString())
                    .addCode("$>return $L;\n", entry.getValue())
                    .addCode("$<");
        }

        getDelegateLayoutMethodBuilder.addCode("default:\n$>return $L;\n", -1)
                .addCode("$<")
                .endControlFlow();

        return getDelegateLayoutMethodBuilder;
    }

    private MethodSpec.Builder generateGetImplicitDelegatesMethod(List<TypeNode> delegateTypeInfos) {
        String delegateString = "AdapterDelegate";

        ClassName list = ClassName.get("java.util", "List");
        ClassName delegateClassName = ClassName.get(adapterPackage, delegateString);
        TypeName listOfDelegates = ParameterizedTypeName.get(list, delegateClassName);

        MethodSpec.Builder getImplicitDelegatesMethodBuilder = MethodSpec
                .methodBuilder("getImplicitDelegates")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(List.class, "delegateClass")
                .returns(listOfDelegates);

        return getImplicitDelegatesMethodBuilder;
    }

    private boolean createInfoFile(Set<? extends TypeElement> annotations, RoundEnvironment env,
                                   String generatedPackageName, TypeSpec.Builder classBuilder) {
        if (env.processingOver()) {
            if (!annotations.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Unexpected processing state: annotations still available after processing over");
                return true;
            }
        }

        if (annotations.isEmpty()) {
            return true;
        }

        try {
            JavaFile.builder(generatedPackageName,
                    classBuilder.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(ERROR, e.toString());
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new TreeSet<>(Collections.singletonList(
                Delegate.class.getCanonicalName()));
    }

}
