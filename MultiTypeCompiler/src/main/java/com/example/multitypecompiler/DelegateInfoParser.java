package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Type;
import com.example.multitypeannotations.TypeMethod;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;

class DelegateInfoParser {

    static Map<ClassName, Map<Integer, TypeElement>> collectTypeInfo(List<TypeNode> nodes, Map<TypeElement, ExecutableElement> typeMethods) {
        Map<ClassName, Map<Integer, TypeElement>> res = new HashMap<>();
        for (TypeNode node : nodes) {
            Map<Integer, TypeElement> info;
            for (Pair<ClassName, Integer> supportType : node.supportTypes) {
                if (res.containsKey(supportType.first)) {
                    info = res.get(supportType.first);
                    if (info == null) {
                        info = new HashMap<>();
                    }

                    info.put(supportType.second, node.element);
                } else {
                    info = new HashMap<>();
                    info.put(supportType.second, node.element);
                }

                res.put(supportType.first, info);
            }
        }

        return res;
    }

    static List<TypeNode> collectDelegateTypeInfo(RoundEnvironment env, Elements elements) {
        List<TypeNode> typeInfo = new LinkedList<>();
        for (Element element : env.getElementsAnnotatedWith(Delegate.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("Annotation Delegate.class should target on a Class");
            }

            Delegate delegateAnnotation = element.getAnnotation(Delegate.class);
            if (delegateAnnotation != null) {
                List<Pair<ClassName, Integer>> typeClass = getSupportTypes(elements, delegateAnnotation);
                typeInfo.add(new TypeNode((TypeElement) element, typeClass));
            }
        }

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

    private static List<Pair<ClassName, Integer>> getSupportTypes(Elements elements, Delegate type) {
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
                typeClassName = ClassName.get(elements.getTypeElement(ex.getTypeMirror().toString()));
            }

            int subType = typeDetail.SUBTYPE();

            supportTypes.add(Pair.create(typeClassName, subType));
        }

        return supportTypes;
    }

}
