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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateLayoutInfo;
import static com.example.multitypecompiler.DelegateInfoParser.collectDelegateTypeInfo;
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
        List<TypeNode> typeInfo = collectDelegateTypeInfo(env);
        Map<TypeElement, ExecutableElement> typeMethods = collectTypeMethodInfo(env);
        Map<TypeElement, Integer> layouts = collectDelegateLayoutInfo(env);

        if (typeInfo == null || typeInfo.isEmpty()) {
            return false;
        }

        TypeSpec.Builder classBuilder = generateDelegateIndexClass();
        TypeSpec.Builder infoBuilder = generateDelegateInfoClass();

        classBuilder.addMethod(setAdapterMethod().build());
        classBuilder.addMethod(singletonMethod().build());
        classBuilder.addMethod(setDelegateInfoMethod(typeInfo, typeMethods, layouts).build());

        boolean succeeded = createInfoFile(annotations, env, packageName, infoBuilder);
        succeeded &= createInfoFile(annotations, env, packageName, classBuilder);

        return succeeded;
    }

    private TypeSpec.Builder generateDelegateIndexClass() {
        ClassName generatedFullName = ClassName.get(packageName, delegateIndexClassName);
        ClassName adapterFullName = ClassName.get(adapterPackage, adapterString);
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        ClassName list = ClassName.get("java.util", "List");
        TypeName delegateInfo = ParameterizedTypeName.get(list, DelegateInfoFullName);

        return TypeSpec.classBuilder(generatedFullName)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(adapterFullName, "adapter", Modifier.PRIVATE)
                    .addField(generatedFullName, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                    .addField(delegateInfo, "delegateInfoList", Modifier.PRIVATE)
                    .addAnnotation(Keep.class)
                    .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .build());
    }

    private TypeSpec.Builder generateDelegateInfoClass() {
        ClassName generatedFullName = ClassName.get(packageName, delegateInfoClassName);

        return TypeSpec.classBuilder(generatedFullName)
                .addModifiers(Modifier.PUBLIC)
                .addField(int.class, "index", Modifier.PUBLIC)
                .addField(Class.class, "delegateClass", Modifier.PUBLIC)
                .addField(String.class, "delegateClassString", Modifier.PUBLIC)
                .addField(String.class, "typeClassString", Modifier.PUBLIC)
                .addField(int.class, "subtype", Modifier.PUBLIC)
                .addField(Method.class, "subTypeMethod")
                .addField(int.class, "LayoutRes", Modifier.PUBLIC)
                .addAnnotation(Keep.class)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(int.class, "index")
                        .addParameter(Class.class, "clazz")
                        .addParameter(String.class, "typeName")
                        .addParameter(int.class, "subType")
                        .addParameter(Method.class, "method")
                        .addParameter(int.class, "resId")
                        .addStatement("this.$N = $N", "index", "index")
                        .addStatement("this.$N = $N", "delegateClass", "clazz")
                        .addStatement("this.$N = $N.getName()", "delegateClassString", "clazz")
                        .addStatement("this.$N = $N", "typeClassString", "typeName")
                        .addStatement("this.$N = $N", "subtype", "subtype")
                        .addStatement("this.$N = $N", "subTypeMethod", "method")
                        .addStatement("this.$N = $N", "LayoutRes", "resId")
                        .build());
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
                .addCode("if ($N == null) {\n", "instance")
                .addCode("$>synchronized ($T.class) {\n", generatedFullName)
                .addCode("$>if ($N == null) {\n", "instance")
                .addStatement("$>$N = new $T()", "instance", generatedFullName)
                .addCode("$<}\n$<}\n$<}\n\n")
                .addStatement("return $N", "instance");
    }

    private MethodSpec.Builder setDelegateInfoMethod(List<TypeNode> nodes, Map<TypeElement, ExecutableElement> methods,
                                                     Map<TypeElement, Integer> layouts) {
        ClassName DelegateInfoFullName = ClassName.get(packageName, delegateInfoClassName);

        MethodSpec.Builder setDelegateInfoBuilder = MethodSpec.methodBuilder("setDelegateInfo")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);

        if (nodes == null || nodes.isEmpty()) {
            return setDelegateInfoBuilder;
        }

        setDelegateInfoBuilder.addStatement("$N = new $T<>()", "delegateInfoList", ArrayList.class)
                .addCode("\n");

        for (int i = 0; i < nodes.size(); i++) {
            int resId = -1;
            if (layouts != null && layouts.containsKey(nodes.get(i).element)) {
                resId = layouts.get(nodes.get(i).element);
            }


//            String methodName = null;
//            if (methods != null && methods.containsKey(nodes.get(i).element)) {
//                methodName = methods.get(nodes.get(i).element).asType().toString();
//            }
//
//            setDelegateInfoBuilder.beginControlFlow("try {")
//                    .addStatement("$T $N = $N.getDeclaredMethod($N)", Method.class, "method", "itemClass", "methodName")

            if (nodes.get(i).typeString.equals(None.class.getName())) {
                setDelegateInfoBuilder.beginControlFlow("try")
                        .addStatement("$T $N = $T.class.getGenericSuperclass()", Type.class, "superclass", ClassName.get(nodes.get(i).element))
                        .addStatement("$T $N = ($T) (($T) $N).getActualTypeArguments()[$L]",
                                Class.class, "typeClass", Class.class, ParameterizedType.class, "superclass", 0)
                        .addCode("$<} catch ($T $N) {", Exception.class, "ex")
                        .addCode("\n")
                        .addStatement("$>$N.printStackTrace()", "ex")
                        .addCode("$<}")
                        .addCode("\n")
                        .addCode("\n");
            }

            setDelegateInfoBuilder.addStatement("$N.add(new $T($L, $T.class,\n $S, $L, $N, $L))",
                    "delegateInfoList",
                    DelegateInfoFullName,
                    i,
                    ClassName.get(nodes.get(i).element),
                    nodes.get(i).typeString,
                    nodes.get(i).subType,
                    "null",
                    resId
            );

            setDelegateInfoBuilder.addCode("\n");
        }


        return setDelegateInfoBuilder;
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
