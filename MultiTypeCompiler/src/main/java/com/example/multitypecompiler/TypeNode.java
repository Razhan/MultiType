package com.example.multitypecompiler;

import javax.lang.model.element.TypeElement;

public class TypeNode {

    public TypeElement element;
    public String typeString;
    public int subType;

    public TypeNode(TypeElement element, String typeString, int subType) {
        this.element = element;
        this.typeString = typeString;
        this.subType = subType;
    }
}
