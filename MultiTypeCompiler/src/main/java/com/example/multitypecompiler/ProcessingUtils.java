package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.DelegateAdapter;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.None;
import com.example.multitypeannotations.Type;
import com.example.multitypeannotations.TypeMethod;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

class ProcessingUtils {

    static List<TypeNode> collectDelegateInfo(RoundEnvironment env, Elements elementUtils) {
        List<TypeNode> typeInfo = new LinkedList<>();
        for (Element element : env.getElementsAnnotatedWith(Delegate.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("Annotation Delegate.class should target on a Class");
            }

            Delegate delegateAnnotation = element.getAnnotation(Delegate.class);
            if (delegateAnnotation != null) {
                List<Pair<ClassName, Integer>> typeClass = getSupportTypes((TypeElement) element, elementUtils, delegateAnnotation);
                typeInfo.add(new TypeNode((TypeElement) element, typeClass));
            }
        }

        checkDelegateNode(typeInfo);
        return typeInfo;
    }

    static Map<TypeElement, ExecutableElement> collectTypeMethodInfo(RoundEnvironment env) {
        Map<TypeElement, ExecutableElement> typeInfo = new HashMap<>();
        for (Element element : env.getElementsAnnotatedWith(TypeMethod.class)) {
            if (!(element instanceof ExecutableElement)) {
                throw new IllegalArgumentException("Annotation TypeMethod.class should target on a Method");
            }

            TypeElement className = (TypeElement) element.getEnclosingElement();
            typeInfo.put(className, (ExecutableElement) element);
        }

        return typeInfo;
    }

    static Map<TypeElement, Integer> collectDelegateLayoutInfo(RoundEnvironment env) {
        Map<TypeElement, Integer> layoutInfo = new HashMap<>();
        for (Element element : env.getElementsAnnotatedWith(DelegateLayout.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("Annotation DelegateLayout.class should target on a Class");
            }

            DelegateLayout layoutAnnotation = element.getAnnotation(DelegateLayout.class);
            if (layoutAnnotation != null) {
                layoutInfo.put((TypeElement) element, layoutAnnotation.LAYOUT());
            }
        }

        return layoutInfo;
    }

    static List<TypeElement> collectAdapterInfo(RoundEnvironment env) {
        List<TypeElement> adapterInfo = new LinkedList<>();

        for (Element element : env.getElementsAnnotatedWith(DelegateAdapter.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("DelegateAdapter Delegate.class should target on a Class");
            }

            adapterInfo.add((TypeElement) element);

        }

        return adapterInfo;
    }

    static List<? extends TypeMirror> getTypeArguments(TypeElement element) {
        TypeElement typeElement = element;

        while (true) {
            TypeMirror superClass = typeElement.getSuperclass();
            if (superClass.getKind() == TypeKind.NONE) {
                return null;
            }

            List<? extends TypeMirror> typeMirrors = ((DeclaredType) superClass).getTypeArguments();

            if (typeMirrors == null || typeMirrors.isEmpty()) {
                typeElement = (TypeElement) ((DeclaredType) superClass).asElement();
            } else {
                return typeMirrors;
            }
        }
    }

    private static void checkDelegateNode(List<TypeNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        Set<String> set = new HashSet<>();
        for (TypeNode node : nodes) {
            if (node.supportTypes == null || node.supportTypes.isEmpty()) {
                continue;
            }

            for (Pair<ClassName, Integer> pair : node.supportTypes) {
                String typeInfoString = pair.first.toString() + String.valueOf(pair.second);
                if (set.contains(typeInfoString)) {
                    throw new IllegalArgumentException(String.format("%s with specific subtype %d Can Only correspond to One Delegate",
                            pair.first.toString(), pair.second));
                }

                set.add(typeInfoString);
            }
        }
    }

    private static List<Pair<ClassName, Integer>> getSupportTypes(TypeElement element, Elements elementUtils, Delegate type) {
        List<Pair<ClassName, Integer>> supportTypes = new ArrayList<>();
        Type[] typeArray = type.DETAIL();

        if (typeArray.length <= 0) {
            return supportTypes;
        }

        for (Type typeDetail : typeArray) {
            ClassName typeClassName;
            try {
                typeClassName = ClassName.get(typeDetail.CLASS());
            } catch (MirroredTypeException ex) {
                typeClassName = ClassName.get(elementUtils.getTypeElement(ex.getTypeMirror().toString()));
            }

            if (typeClassName.toString().equals(None.class.getName())) {
                List<? extends TypeMirror> typeMirrors = getTypeArguments(element);
                if (typeMirrors == null || typeMirrors.isEmpty()) {
                    throw new IllegalArgumentException(String.format("Can not find corresponding data type for %s",
                            element.asType().toString()));
                }

                typeClassName = ClassName.get(elementUtils.getTypeElement(typeMirrors.get(0).toString()));
            }

            int subType = typeDetail.SUBTYPE();

            supportTypes.add(Pair.create(typeClassName, subType));
        }

        return supportTypes;
    }

}
