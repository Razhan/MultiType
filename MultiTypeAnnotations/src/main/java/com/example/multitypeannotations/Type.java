package com.example.multitypeannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Type {

    Class CLASS() default None.class;

    int SUBTYPE() default -1;

}
