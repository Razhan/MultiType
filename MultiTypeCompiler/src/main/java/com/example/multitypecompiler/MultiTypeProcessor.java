package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.Keep;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.example.multitypecompiler.DelegateInfoParser.collectAdapterInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateLayoutInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateTypeInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectTypeMethodInfo;
import static com.example.multitypecompiler.DelegateInfoParser.getTypeArguments;
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
    private String delegateString = "AdapterDelegate";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtil = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver()) {
            if (!annotations.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Unexpected processing state: annotations still available after processing over");
                return false;
            }
        }

        if (annotations.isEmpty()) {
            return false;
        }

        List<TypeNode> typeInfo = collectDelegateTypeInfo(env, elementUtil);
        Map<TypeElement, ExecutableElement> typeMethods = collectTypeMethodInfo(env);
        Map<TypeElement, Integer> layouts = collectDelegateLayoutInfo(env);
        List<TypeElement> adapterInfo = collectAdapterInfo(env);

        if (typeInfo.isEmpty()) {
            return false;
        }

        TypeSpec.Builder classBuilder = generateDelegateIndexClass();
        TypeSpec.Builder infoBuilder = generateDelegateInfoClass();
        Map<String, TypeSpec.Builder> managerBuilderMap = generateManagerClass(adapterInfo);

        classBuilder.addMethod(setAdapterMethod().build());
        classBuilder.addMethod(singletonMethod().build());
        classBuilder.addMethod(setDelegateInfoMethod(typeInfo, layouts).build());
        classBuilder.addMethod(setTypeInfoMethod().build());

        classBuilder.addMethod(setTypeMethodInfoMethod(typeMethods).build());

        classBuilder.addMethod(getTypeMethod().build());
        classBuilder.addMethod(getDelegateMethod().build());

        createInfoFile(packageName, infoBuilder);
        createInfoFile(packageName, classBuilder);
        for (Map.Entry<String, TypeSpec.Builder> entry : managerBuilderMap.entrySet()) {
            createInfoFile(entry.getKey(), entry.getValue());
        }

        return true;
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
                ParameterizedTypeName.get(map, ClassName.get(Integer.class), ClassName.get(Integer.class)));

        return TypeSpec.classBuilder(generatedFullName)
                .addModifiers(Modifier.PUBLIC)
                .addField(adapterFullName, "adapter", Modifier.PRIVATE)
                .addField(generatedFullName, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                .addField(delegateInfo, "delegateInfoList", Modifier.PRIVATE)
                .addField(typeMethodInfo, "typeMethodInfo", Modifier.PRIVATE)
                .addField(typeInfo, "typeInfo", Modifier.PRIVATE)
                .addAnnotation(Keep.class)
                .addMethod(MethodSpec.constructorBuilder()
                        .addStatement("$N()", "setDelegateInfo")
                        .addStatement("$N()", "setTypeMethodInfo")
                        .addStatement("$N()", "setTypeInfo")
                        .addModifiers(Modifier.PRIVATE)
                        .build());
    }

    private TypeSpec.Builder generateDelegateInfoClass() {
        ClassName generatedFullName = ClassName.get(packageName, delegateInfoClassName);
        ClassName adapterFullName = ClassName.get(adapterPackage, adapterString);
        ClassName delegateClassName = ClassName.get(adapterPackage, delegateString);

        return TypeSpec.classBuilder(generatedFullName)
                .addModifiers(Modifier.PUBLIC)
                .addField(int.class, "index", Modifier.PUBLIC)
                .addField(int.class, "subType", Modifier.PUBLIC)
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
                        .addStatement("this.$N = $N", "subType", "subType")
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

    private Map<String, TypeSpec.Builder> generateManagerClass(List<TypeElement> adapterInfo) {
        ClassName AbsManager = ClassName.get("com.example.multitypelib.listadapter", "AbsListDelegatesManager");
        ClassName AbsAdapter = ClassName.get("com.example.multitypelib", "AbsDelegationAdapter");
        ClassName AbsDelegate = ClassName.get("com.example.multitypelib.listadapter", "AbsListDelegate");

        ClassName generatedFullName = ClassName.get(packageName, delegateIndexClassName);
        ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");
        ClassName list = ClassName.get("java.util", "List");

        Map<String, TypeSpec.Builder> managerMap = new HashMap<>();

        if (adapterInfo == null || adapterInfo.isEmpty()) {
            return managerMap;
        }

        for (TypeElement adapterClass : adapterInfo) {
            String packageName = ClassName.get(adapterClass).packageName();
            String adapterClassName = ClassName.get(adapterClass).simpleName();
            String managerClassName = adapterClassName + "Manager";

            List<? extends TypeMirror> typeMirrors = getTypeArguments(adapterClass);
            if (typeMirrors == null || typeMirrors.isEmpty()) {
                throw new IllegalArgumentException("adapter对应类型没有正确设置");
            }

            ClassName typeClassName = ClassName.get(elementUtil.getTypeElement(typeMirrors.get(0).toString()));
            TypeName itemListType = ParameterizedTypeName.get(list, typeClassName);

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(managerClassName)
                    .superclass(ParameterizedTypeName.get(AbsManager, typeClassName))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(MethodSpec.methodBuilder("setAdapter")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void.class)
                            .addAnnotation(Override.class)
                            .addParameter(AbsAdapter, "adapter")
                            .addStatement("$T.getInstance().setAdapter($N)", generatedFullName, "adapter")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("getItemViewType")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(int.class)
                            .addAnnotation(Override.class)
                            .addParameter(ParameterSpec.builder(itemListType, "items")
                                    .addAnnotation(AnnotationSpec.builder(NonNull).build())
                                    .build())
                            .addParameter(int.class, "position")
                            .addStatement("return $T.getInstance().getItemViewType($N.get($N))", generatedFullName, "items", "position")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("getDelegateForViewType")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ParameterizedTypeName.get(AbsDelegate, typeClassName))
                            .addAnnotation(Override.class)
                            .addAnnotation(NonNull)
                            .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                    .addMember("value", "$S","unchecked").build())
                            .addParameter(int.class, "viewType")
                            .addStatement("return ($T) $T.getInstance().getDelegateByViewType($N)",
                                    ParameterizedTypeName.get(AbsDelegate, typeClassName), generatedFullName, "viewType")
                            .build());

            managerMap.put(packageName, classBuilder);
        }

        return managerMap;
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

            if (i != nodes.size() - 1) {
                setDelegateInfoBuilder.addCode("\n");
            }
        }

        return setDelegateInfoBuilder;
    }

    private MethodSpec.Builder setTypeInfoMethod() {
        ClassName map = ClassName.get("java.util", "Map");
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);
        TypeName typeInfo = ParameterizedTypeName.get(map, ClassName.get(Integer.class), ClassName.get(Integer.class));

        MethodSpec.Builder setDelegateInfoBuilder = MethodSpec.methodBuilder("setTypeInfo")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);

        setDelegateInfoBuilder.beginControlFlow("if ($N == null || $N.isEmpty())", "delegateInfoList", "delegateInfoList")
                .addStatement("return")
                .endControlFlow()
                .addCode("\n")
                .addStatement("$N = new $T<>()", "typeInfo", HashMap.class)
                .addCode("\n")
                .beginControlFlow("for ($T $N : $N)", DelegateInfoFullName, "delegateInfo", "delegateInfoList")
                .addStatement("$T $N", typeInfo, "info")
                .addCode("\n")
                .beginControlFlow("if ($N.get($N.$N) == $S)", "typeInfo", "delegateInfo", "typeClass", null)
                .addStatement("$N = new $T<>()", "info", HashMap.class)
                .addCode("$<} else {")
                .addCode("\n")
                .addStatement("$>$N = $N.get($N.$N)", "info", "typeInfo", "delegateInfo", "typeClass")
                .endControlFlow()
                .addCode("\n")
                .addStatement("$N.put($N.$N, $N.$N)", "info", "delegateInfo", "subType", "delegateInfo", "index")
                .addStatement("$N.put($N.$N, $N)", "typeInfo", "delegateInfo", "typeClass", "info")
                .endControlFlow();

        return setDelegateInfoBuilder;
    }

    private MethodSpec.Builder getDelegateMethod() {
        ClassName delegateClassName = ClassName.get(adapterPackage, delegateString);
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        return MethodSpec
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
            String methodName = entry.getValue().getSimpleName().toString();
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

    private MethodSpec.Builder getTypeMethod() {
        ClassName map = ClassName.get("java.util", "Map");
        TypeName typeInfo = ParameterizedTypeName.get(map, ClassName.get(Integer.class), ClassName.get(Integer.class));

        return MethodSpec
                .methodBuilder("getItemViewType")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "item")
                .returns(int.class)
                .beginControlFlow("if ($N == null || $N.isEmpty())", "typeInfo", "typeInfo")
                .addStatement("return $L", -1)
                .endControlFlow()
                .addCode("\n")
                .addStatement("$T itemClass = $N.getClass()", Class.class, "item")
                .addCode("\n")
                .addStatement("$T $N = $N.get($N)", typeInfo, "delegateMap", "typeInfo", "itemClass")
                .addCode("\n")
                .beginControlFlow("if ($N.get($N) == null || $N.get($N).isEmpty())", "typeInfo", "itemClass", "typeInfo", "itemClass")
                .addStatement("return $L", -1)
                .endControlFlow()
                .addCode("\n")
                .beginControlFlow("if ($N.size() == $L && $N.get($L) != $S)", "delegateMap", 1, "delegateMap", -1, null)
                .addStatement("return $N.get($L)", "delegateMap", -1)
                .endControlFlow()
                .addCode("\n")
                .addStatement("$T $N = $N.get($N)", Method.class, "typeMethod", "typeMethodInfo", "itemClass")
                .beginControlFlow("if ($N == null)", "typeMethod")
                .addStatement("throw new $T($S)", IllegalArgumentException.class, "没找到方法")
                .endControlFlow()
                .addCode("\n")
                .beginControlFlow("try")
                .addStatement("$T $N = ($T) $N.invoke($N)", int.class, "subType", int.class, "typeMethod", "item")
                .addStatement("return $N.get($N)", "delegateMap", "subType")
                .addCode("$<} catch ($T $N) {", Exception.class, "ex")
                .addCode("\n")
                .addStatement("$>throw new $T($S)", IllegalArgumentException.class, "执行子类型方法异常")
                .endControlFlow();
    }

    private void createInfoFile(String generatedPackageName, TypeSpec.Builder classBuilder) {
        try {
            JavaFile.builder(generatedPackageName,
                    classBuilder.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(ERROR, e.toString());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new TreeSet<>(Collections.singletonList(
                Delegate.class.getCanonicalName()));
    }

}
