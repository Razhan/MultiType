package com.example.multitypecompiler;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.TypeMethod;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

class DelegateInfoParser {

    static void collectDelegateInfo(List<TypeNode> nodes, Map<TypeElement, ExecutableElement> typeMethods) {
    }

    static List<TypeNode> collectDelegateTypeInfo(RoundEnvironment env) {
        List<TypeNode> typeInfo = new LinkedList<>();
        for (Element element : env.getElementsAnnotatedWith(Delegate.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("Annotation Delegate.class should target on a Class");
            }

            Delegate delegateAnnotation = element.getAnnotation(Delegate.class);
            if (delegateAnnotation != null) {
                int subType = getSubType(delegateAnnotation);
                String typeClass = getTypeClassString(delegateAnnotation);

                typeInfo.add(new TypeNode((TypeElement) element, typeClass, subType));
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

    private static int getSubType(Delegate type) {
        return type.DETAIL().SUBTYPE();
    }

    private static String getTypeClassString(Delegate type) {
        try {
            return type.DETAIL().CLASS().toString();
        } catch(MirroredTypeException ex) {
            return ex.getTypeMirror().toString();
        }
    }

}
