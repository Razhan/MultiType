package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.Keep;
import com.example.multitypeannotations.None;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateLayoutInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateTypeInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectTypeInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectTypeMethodInfo;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class MultiTypeProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtil;

    private String packageName = "com.example.ran.multitype";
    private String delegateIndexClassName = "AdapterTypeIndex";
    private String delegateInfoClassName = "DelegateInfo";

    private String adapterPackage = "com.example.multitypelib";
    private String adapterString = "AbsDelegationAdapter";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtil = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        List<TypeNode> typeInfo = collectDelegateTypeInfo(env, elementUtil);
        Map<TypeElement, ExecutableElement> typeMethods = collectTypeMethodInfo(env);
        Map<TypeElement, Integer> layouts = collectDelegateLayoutInfo(env);
        Map<ClassName, Map<Integer, TypeElement>> typeMap = collectTypeInfo(typeInfo, typeMethods);

        if (typeInfo == null || typeInfo.isEmpty()) {
            return false;
        }

        TypeSpec.Builder classBuilder = generateDelegateIndexClass();
        TypeSpec.Builder infoBuilder = generateDelegateInfoClass();

        classBuilder.addMethod(setAdapterMethod().build());
        classBuilder.addMethod(singletonMethod().build());
        classBuilder.addMethod(setDelegateInfoMethod(typeInfo, layouts).build());
        classBuilder.addMethod(setDelegateInfoMethod2(typeMap).build());

        classBuilder.addMethod(setTypeMethodInfoMethod(typeMethods).build());

        classBuilder.addMethod(getTypeMethod(typeInfo).build());
        classBuilder.addMethod(getDelegateMethod().build());

        boolean succeeded = createInfoFile(annotations, env, packageName, infoBuilder);
        succeeded &= createInfoFile(annotations, env, packageName, classBuilder);

        return succeeded;
    }

    private TypeSpec.Builder generateDelegateIndexClass() {
        ClassName generatedFullName = ClassName.get(packageName, delegateIndexClassName);
        ClassName adapterFullName = ClassName.get(adapterPackage, adapterString);
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        ClassName list = ClassName.get("java.util", "List");
        ClassName map = ClassName.get("java.util", "Map");

        TypeName delegateInfo = ParameterizedTypeName.get(list, DelegateInfoFullName);
        TypeName typeMethodInfo = ParameterizedTypeName.get(map, ClassName.get(Class.class), ClassName.get(Method.class));
        TypeName typeInfo = ParameterizedTypeName.get(map, ClassName.get(Class.class),
                ParameterizedTypeName.get(map, ClassName.get(Integer.class), ClassName.get(Class.class)));

        return TypeSpec.classBuilder(generatedFullName)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(adapterFullName, "adapter", Modifier.PRIVATE)
                    .addField(generatedFullName, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                    .addField(delegateInfo, "delegateInfoList", Modifier.PRIVATE)
                    .addField(typeMethodInfo, "typeMethodInfo", Modifier.PRIVATE)
                    .addField(typeInfo, "typeInfo", Modifier.PRIVATE)
                    .addAnnotation(Keep.class)
                    .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .build());
    }

    private TypeSpec.Builder generateDelegateInfoClass() {
        String delegateString = "AdapterDelegate";

        ClassName generatedFullName = ClassName.get(packageName, delegateInfoClassName);
        ClassName adapterFullName = ClassName.get(adapterPackage, adapterString);
        ClassName delegateClassName = ClassName.get(adapterPackage, delegateString);

        return TypeSpec.classBuilder(generatedFullName)
                .addModifiers(Modifier.PUBLIC)
                .addField(int.class, "index", Modifier.PUBLIC)
                .addField(int.class, "subtype", Modifier.PUBLIC)
                .addField(Class.class, "typeClass")
                .addField(delegateClassName, "delegate")
                .addAnnotation(Keep.class)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(int.class, "index")
                        .addParameter(adapterFullName, "adapter")
                        .addParameter(Class.class, "clazz")
                        .addParameter(Class.class, "typeClass")
                        .addParameter(int.class, "subType")
                        .addParameter(int.class, "resId")
                        .addStatement("this.$N = $N", "index", "index")
                        .addStatement("this.$N = $N", "subtype", "subtype")
                        .addStatement("this.$N = $N", "typeClass", "typeClass")
                        .addCode("\n")
                        .beginControlFlow("try")
                        .addStatement("$T $N = $N.getDeclaredConstructor($T.class, $T.class)", Constructor.class, "constructor", "clazz", adapterFullName, int.class)
                        .addStatement("this.$N = ($T) $N.newInstance($N, $L)", "delegate", delegateClassName, "constructor", "adapter", "resId")
                        .addCode("$<} catch ($T $N) {", Exception.class, "ex")
                        .addCode("\n")
                        .addStatement("$>$N.printStackTrace()", "ex")
                        .endControlFlow()
                        .build()
                );
    }

    private MethodSpec.Builder setAdapterMethod() {
        ClassName adapterClass = ClassName.get(adapterPackage, adapterString);
        return MethodSpec
                .methodBuilder("setAdapter")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(adapterClass, "adapter")
                .addStatement("this.$N = $N", "adapter", "adapter")
                .returns(void.class);
    }

    private MethodSpec.Builder singletonMethod() {
        ClassName generatedFullName = ClassName.get(packageName, delegateIndexClassName);
        return MethodSpec
                .methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(packageName, delegateIndexClassName))
                .beginControlFlow("if ($N == $S)", "instance", null)
                .beginControlFlow("synchronized ($T.class)", generatedFullName)
                .beginControlFlow("if ($N == $S)", "instance", null)
                .addStatement("$N = new $T()", "instance", generatedFullName)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addCode("\n")
                .addStatement("return $N", "instance");
    }

    private MethodSpec.Builder setDelegateInfoMethod(List<TypeNode> nodes, Map<TypeElement, Integer> layouts) {
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        MethodSpec.Builder setDelegateInfoBuilder = MethodSpec.methodBuilder("setDelegateInfo")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);

        if (nodes == null || nodes.isEmpty()) {
            return setDelegateInfoBuilder;
        }

        setDelegateInfoBuilder.addStatement("$N = new $T<>()", "delegateInfoList", ArrayList.class)
                .addCode("\n");

        int index = 0;
        for (int i = 0; i < nodes.size(); i++) {
            List<Pair<ClassName, Integer>> supportTypes = nodes.get(i).supportTypes;
            for (Pair<ClassName, Integer> typeInfo : supportTypes) {
                int resId = -1;
                if (layouts != null && layouts.containsKey(nodes.get(i).element)) {
                    resId = layouts.get(nodes.get(i).element);
                }

                if (typeInfo.first.toString().equals(None.class.getName())) {
                    String typeClassName = "typeClass" + String.valueOf(i);
                    setDelegateInfoBuilder.addStatement("$T $N = $S", Class.class, typeClassName, null)
                            .beginControlFlow("try")
                            .addStatement("$T $N = $T.class.getGenericSuperclass()", Type.class, "superclass", ClassName.get(nodes.get(i).element))
                            .addStatement("$N = ($T) (($T) $N).getActualTypeArguments()[$L]", typeClassName, Class.class, ParameterizedType.class, "superclass", 0)
                            .addCode("\n")
                            .beginControlFlow("if ($N == $S)", typeClassName, null)
                            .addStatement("throw new $T($S)", IllegalArgumentException.class, "1111112")
                            .endControlFlow()
                            .addCode("$<} catch ($T $N) {", Exception.class, "ex")
                            .addCode("\n")
                            .addStatement("$>$N.printStackTrace()", "ex")
                            .endControlFlow()
                            .addStatement("$N.add(new $T($L, $N, $T.class, $N, $L, $L))",
                                    "delegateInfoList",
                                    DelegateInfoFullName,
                                    index++,
                                    "adapter",
                                    ClassName.get(nodes.get(i).element),
                                    typeClassName,
                                    typeInfo.second,
                                    resId
                            );
                } else {
                    setDelegateInfoBuilder.addStatement("$N.add(new $T($L, $N, $T.class, $T.class, $L, $L))",
                            "delegateInfoList",
                            DelegateInfoFullName,
                            index++,
                            "adapter",
                            ClassName.get(nodes.get(i).element),
                            typeInfo.first,
                            typeInfo.second,
                            resId
                    );
                }
            }

            if (i != nodes.size() - 1) {
                setDelegateInfoBuilder.addCode("\n");
            }
        }

        return setDelegateInfoBuilder;
    }


    private MethodSpec.Builder setDelegateInfoMethod2(Map<ClassName, Map<Integer, TypeElement>> typeMap) {
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        MethodSpec.Builder setDelegateInfoBuilder = MethodSpec.methodBuilder("setDelegateInfo2")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);

        if (typeMap == null || typeMap.size() <= 0) {
            return setDelegateInfoBuilder;
        }

        setDelegateInfoBuilder.addStatement("$N = new $T<>()", "typeInfo", HashMap.class)
                .addCode("\n");

        for (Map.Entry<ClassName, Map<Integer, TypeElement>> entry : typeMap.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }



            for (Map.Entry<Integer, TypeElement> subEntry : entry.getValue().entrySet()) {

            }
        }

        return setDelegateInfoBuilder;
    }

    private MethodSpec.Builder getDelegateMethod() {
        String delegateString = "AdapterDelegate";
        ClassName delegateClassName = ClassName.get(adapterPackage, delegateString);
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        MethodSpec.Builder getDelegateMethodBuilder = MethodSpec
                .methodBuilder("getDelegateByViewType")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "viewType")
                .returns(delegateClassName)
                .beginControlFlow("if ($N == $S || $N.isEmpty())", "delegateInfoList", null, "delegateInfoList")
                .addStatement("throw new $T($T.format($S, $L))", IllegalArgumentException.class, String.class, "Can not Find Adapter Delegate for Type %d", "viewType")
                .endControlFlow()
                .addCode("\n")
                .addStatement("$T $N = $S", DelegateInfoFullName, "delegateInfo", null)
                .addStatement("$N = $N.get($L)", "delegateInfo", "delegateInfoList", "viewType")
                .beginControlFlow("if ($N == $S)", "delegateInfo", null)
                .addStatement("throw new $T($T.format($S, $L))", IllegalArgumentException.class, String.class, "Can not Find Adapter Delegate for Type %d", "viewType")
                .endControlFlow()
                .addCode("\n")
                .addStatement("return $N.$N", "delegateInfo", "delegate");

        return getDelegateMethodBuilder;
    }

    private MethodSpec.Builder setTypeMethodInfoMethod(Map<TypeElement, ExecutableElement> typeMethods) {
        MethodSpec.Builder setTypeMethodInfoBuilder = MethodSpec.methodBuilder("setTypeMethodInfo")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addStatement("$N = new $T<>()", "typeMethodInfo", HashMap.class);

        if (typeMethods == null || typeMethods.isEmpty()) {
            return setTypeMethodInfoBuilder;
        }

        setTypeMethodInfoBuilder.addCode("\n")
            .beginControlFlow("try");

        int i = 0;
        for (Map.Entry<TypeElement, ExecutableElement> entry : typeMethods.entrySet()) {
            String methodName = entry .getValue().getSimpleName().toString();
            String methodVariable = "method" + String.valueOf(i);

            if (!entry.getValue().getReturnType().toString().equals(int.class.getName())) {
                throw new IllegalArgumentException("Method with TypeMethod.class Annotation should Only return int type");
            }

            if (entry.getValue().getParameters() != null && entry.getValue().getParameters().size() > 0) {
                throw new IllegalArgumentException("Method with TypeMethod.class Annotation should has No Parameter");
            }

            setTypeMethodInfoBuilder.addStatement("$T $N = $T.class.getDeclaredMethod($S)", Method.class, methodVariable, ClassName.get(entry.getKey()), methodName)
                    .addStatement("$N.put($T.class, $N)", "typeMethodInfo", ClassName.get(entry.getKey()), methodVariable);
            i++;
        }

        setTypeMethodInfoBuilder.addStatement("$<} catch ($T $N) {", NoSuchMethodException.class, "ex")
                .addStatement("$>$N.printStackTrace()", "ex")
                .endControlFlow();

        return setTypeMethodInfoBuilder;
    }

    private MethodSpec.Builder getTypeMethod(List<TypeNode> typeInfo) {
        MethodSpec.Builder getTypeMethodBuilder = MethodSpec
                .methodBuilder("getItemViewType")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "item")
                .returns(void.class)
//                .addStatement("$T itemClass = $N.getClass().getName()", String.class, "item")
//                .addStatement("$T $N = $N($N)", Class.class, "itemClass", "getMultiTypeClass", "item")
                .addCode("\n");

        return getTypeMethodBuilder;
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
