package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.DelegateAdapter;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Keep;
import com.example.multitypeannotations.TypeMethod;
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
import java.util.Arrays;
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
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.example.multitypecompiler.ProcessingUtils.collectAdapterInfo;
import static com.example.multitypecompiler.ProcessingUtils.collectDelegateInfo;
import static com.example.multitypecompiler.ProcessingUtils.collectDelegateLayoutInfo;
import static com.example.multitypecompiler.ProcessingUtils.collectTypeMethodInfo;
import static com.example.multitypecompiler.ProcessingUtils.getTypeArguments;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
@SupportedOptions("eventBusIndex")
public class MultipleTypeProcessor extends AbstractProcessor {

    private static final String OPTION_MULTIPLE_TYPE_INDEX = "multipleTypeIndex";

    private Filer filer;
    private Messager messager;
    private Elements elementUtil;

    private String packageName;

    private ClassName listNameClass;
    private ClassName mapNameClass;

    private ClassName adapterNameClass;
    private ClassName delegateNameClass;

    private ClassName delegateInfoNameClass;
    private ClassName delegateIndexNameClass;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtil = processingEnv.getElementUtils();
    }

    private void initClassData(String index) {
        int lastPeriod = index.lastIndexOf('.');
        packageName = lastPeriod != -1 ? index.substring(0, lastPeriod) : null;

        listNameClass = ClassName.get(List.class);
        mapNameClass = ClassName.get(Map.class);

        adapterNameClass = ClassName.bestGuess(NameStore.ADAPTER_CLASS);
        delegateNameClass = ClassName.bestGuess(NameStore.DELEGATE_CLASS);

        delegateInfoNameClass = ClassName.get(packageName, NameStore.DELEGATE_INFO);
        delegateIndexNameClass = ClassName.get(packageName, NameStore.DELEGATE_INDEX);
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

        try {
            String index = processingEnv.getOptions().get(OPTION_MULTIPLE_TYPE_INDEX);
            if (index == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "No option " + OPTION_MULTIPLE_TYPE_INDEX +
                        " passed to annotation processor");
                return false;
            }

            initClassData(index);

            List<TypeNode> typeInfo = collectDelegateInfo(env, elementUtil);
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

            createFile(packageName, infoBuilder);
            createFile(packageName, classBuilder);
            for (Map.Entry<String, TypeSpec.Builder> entry : managerBuilderMap.entrySet()) {
                createFile(entry.getKey(), entry.getValue());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in MultipleTypeProcessor: " + e);
        }

        return true;
    }

    private TypeSpec.Builder generateDelegateIndexClass() {
        TypeName delegateInfo = ParameterizedTypeName.get(listNameClass, delegateInfoNameClass);
        TypeName typeMethodInfo = ParameterizedTypeName.get(mapNameClass, ClassName.get(Class.class), ClassName.get(Method.class));
        TypeName typeInfo = ParameterizedTypeName.get(mapNameClass, ClassName.get(Class.class),
                ParameterizedTypeName.get(mapNameClass, ClassName.get(Integer.class), ClassName.get(Integer.class)));

        return TypeSpec.classBuilder(delegateIndexNameClass)
                .addModifiers(Modifier.PUBLIC)
                .addField(adapterNameClass, "adapter", Modifier.PRIVATE)
                .addField(delegateIndexNameClass, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
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
        return TypeSpec.classBuilder(delegateInfoNameClass)
                .addModifiers(Modifier.PUBLIC)
                .addField(int.class, "index", Modifier.PUBLIC)
                .addField(int.class, "subType", Modifier.PUBLIC)
                .addField(Class.class, "typeClass")
                .addField(delegateNameClass, "delegate")
                .addAnnotation(Keep.class)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(int.class, "index")
                        .addParameter(adapterNameClass, "adapter")
                        .addParameter(Class.class, "clazz")
                        .addParameter(Class.class, "typeClass")
                        .addParameter(int.class, "subType")
                        .addParameter(int.class, "resId")
                        .addStatement("this.$N = $N", "index", "index")
                        .addStatement("this.$N = $N", "subType", "subType")
                        .addStatement("this.$N = $N", "typeClass", "typeClass")
                        .addCode("\n")
                        .beginControlFlow("try")
                        .addStatement("$T $N = $N.getDeclaredConstructor($T.class, $T.class)", Constructor.class, "constructor", "clazz", adapterNameClass, int.class)
                        .addStatement("this.$N = ($T) $N.newInstance($N, $L)", "delegate", delegateNameClass, "constructor", "adapter", "resId")
                        .addCode("$<} catch ($T $N) {", Exception.class, "ex")
                        .addCode("\n")
                        .addStatement("$>$N.printStackTrace()", "ex")
                        .endControlFlow()
                        .build()
                );
    }

    private Map<String, TypeSpec.Builder> generateManagerClass(List<TypeElement> adapterInfo) {
        ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");

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
            TypeName itemListType = ParameterizedTypeName.get(listNameClass, typeClassName);

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(managerClassName)
                    .superclass(ParameterizedTypeName.get(ClassName.bestGuess(NameStore.LIST_MANAGER_CLASS), typeClassName))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(MethodSpec.methodBuilder("setAdapter")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void.class)
                            .addAnnotation(Override.class)
                            .addParameter(adapterNameClass, "adapter")
                            .addStatement("$T.getInstance().setAdapter($N)", delegateIndexNameClass, "adapter")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("getItemViewType")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(int.class)
                            .addAnnotation(Override.class)
                            .addParameter(ParameterSpec.builder(itemListType, "items")
                                    .addAnnotation(AnnotationSpec.builder(NonNull).build())
                                    .build())
                            .addParameter(int.class, "position")
                            .addStatement("return $T.getInstance().getItemViewType($N.get($N))", delegateIndexNameClass, "items", "position")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("getDelegateForViewType")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ParameterizedTypeName.get(ClassName.bestGuess(NameStore.LIST_DELEGATE_CLASS), typeClassName))
                            .addAnnotation(Override.class)
                            .addAnnotation(NonNull)
                            .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                    .addMember("value", "$S","unchecked").build())
                            .addParameter(int.class, "viewType")
                            .addStatement("return ($T) $T.getInstance().getDelegateByViewType($N)",
                                    ParameterizedTypeName.get(ClassName.bestGuess(NameStore.LIST_DELEGATE_CLASS), typeClassName), delegateIndexNameClass, "viewType")
                            .build());

            managerMap.put(packageName, classBuilder);
        }

        return managerMap;
    }

    private MethodSpec.Builder setAdapterMethod() {
        return MethodSpec
                .methodBuilder("setAdapter")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(adapterNameClass, "adapter")
                .addStatement("this.$N = $N", "adapter", "adapter")
                .returns(void.class);
    }

    private MethodSpec.Builder singletonMethod() {
        return MethodSpec
                .methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(delegateIndexNameClass)
                .beginControlFlow("if ($N == $S)", "instance", null)
                .beginControlFlow("synchronized ($T.class)", delegateIndexNameClass)
                .beginControlFlow("if ($N == $S)", "instance", null)
                .addStatement("$N = new $T()", "instance", delegateIndexNameClass)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addCode("\n")
                .addStatement("return $N", "instance");
    }

    private MethodSpec.Builder setDelegateInfoMethod(List<TypeNode> nodes, Map<TypeElement, Integer> layouts) {
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
                        delegateInfoNameClass,
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
        TypeName typeInfo = ParameterizedTypeName.get(mapNameClass, ClassName.get(Integer.class), ClassName.get(Integer.class));

        MethodSpec.Builder setDelegateInfoBuilder = MethodSpec.methodBuilder("setTypeInfo")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);

        setDelegateInfoBuilder.beginControlFlow("if ($N == null || $N.isEmpty())", "delegateInfoList", "delegateInfoList")
                .addStatement("return")
                .endControlFlow()
                .addCode("\n")
                .addStatement("$N = new $T<>()", "typeInfo", HashMap.class)
                .addCode("\n")
                .beginControlFlow("for ($T $N : $N)", delegateInfoNameClass, "delegateInfo", "delegateInfoList")
                .addStatement("$T $N = $N.get($N.$N)", typeInfo , "info", "typeInfo", "delegateInfo", "typeClass")
                .addCode("\n")
                .beginControlFlow("if ($N == $S)", "info", null)
                .addStatement("$N = new $T<>()", "info", HashMap.class)
                .endControlFlow()
                .addCode("\n")
                .addStatement("$N.put($N.$N, $N.$N)", "info", "delegateInfo", "subType", "delegateInfo", "index")
                .addStatement("$N.put($N.$N, $N)", "typeInfo", "delegateInfo", "typeClass", "info")
                .endControlFlow();

        return setDelegateInfoBuilder;
    }

    private MethodSpec.Builder getDelegateMethod() {
        return MethodSpec
                .methodBuilder("getDelegateByViewType")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "viewType")
                .returns(delegateNameClass)
                .beginControlFlow("if ($N == $S || $N.isEmpty())", "delegateInfoList", null, "delegateInfoList")
                .addStatement("throw new $T($T.format($S, $L))", IllegalArgumentException.class, String.class, "Can not Find Adapter Delegate for Type %d", "viewType")
                .endControlFlow()
                .addCode("\n")
                .addStatement("$T $N = $N.get($L)", delegateInfoNameClass, "delegateInfo", "delegateInfoList", "viewType")
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
        TypeName typeInfo = ParameterizedTypeName.get(mapNameClass, ClassName.get(Integer.class), ClassName.get(Integer.class));

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
                .addStatement("$T $N = $N.get($N)", typeInfo, "delegateMap", "typeInfo", "itemClass")
                .addCode("\n")
                .beginControlFlow("if ($N == null || $N.isEmpty())", "delegateMap", "delegateMap")
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

    private void createFile(String generatedPackageName, TypeSpec.Builder classBuilder) {
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
        return new TreeSet<>(Arrays.asList(
                Delegate.class.getCanonicalName(),
                DelegateLayout.class.getCanonicalName(),
                DelegateAdapter.class.getCanonicalName(),
                TypeMethod.class.getCanonicalName(),
                Keep.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

}
