package com.example.multitypecompiler;

public class ConstructorException extends RuntimeException {
    public ConstructorException() {
        super("you must provider a non arguments public constructor method");
    }
}
