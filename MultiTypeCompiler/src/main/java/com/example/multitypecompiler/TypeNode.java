package com.example.multitypecompiler;

import com.squareup.javapoet.ClassName;

import java.util.List;

import javax.lang.model.element.TypeElement;

public class TypeNode {

    public TypeElement element;
    public List<Pair<ClassName, Integer>> supportTypes;

    public TypeNode(TypeElement element, List<Pair<ClassName, Integer>> supportTypes) {
        this.element = element;
        this.supportTypes = supportTypes;
    }
}
