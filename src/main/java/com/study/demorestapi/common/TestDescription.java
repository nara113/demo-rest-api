package com.study.demorestapi.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// junit 4에서 사용 (5는 지원함)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface TestDescription {

    String value();
}
