package com.annotation;

import java.lang.annotation.*;
import java.lang.reflect.*;

public class Test1 {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<?> clazz = Class.forName("com.annotation.Person");
        // 反射出所有此類的註解
        getAnnotation(clazz.getAnnotations());

        // 反射出此類所有屬性的註解
        for (Field field : clazz.getDeclaredFields()) {
            getAnnotation(field.getAnnotations());
        }

        // 反射出此類的所有方法
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            declaredMethod.setAccessible(true);
            boolean aStatic = Modifier.isStatic(declaredMethod.getModifiers());
            if (!aStatic) {
                Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                Object o = declaredConstructor.newInstance();
                declaredMethod.invoke(o);
            } else {
                declaredMethod.invoke(null);
            }
        }

    }

    private static void getAnnotation(Annotation[] annotations) throws InvocationTargetException, IllegalAccessException {
        //  獲取屬性上所有的註解
        for (Annotation annotation : annotations) {
            //  獲取註解類型
            Class<? extends Annotation> aClass = annotation.annotationType();
            for (Method declaredMethod : aClass.getDeclaredMethods()) {
                // invoke 需要接收的是實例
                Object invoke = declaredMethod.invoke(annotation);
                System.out.println("method name : " + declaredMethod.getName() + " method value : " + invoke);
            }
        }
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotationClass {
    String className();
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface MyFieldAnnotation {
    String columnName();
    int columnLength();
    String columnType();
}



@MyAnnotationClass(className = "Person")
class Person {
    @MyFieldAnnotation(columnName = "id", columnLength = 10, columnType = "number")
    private Integer id;
    @MyFieldAnnotation(columnName = "name", columnLength = 20, columnType = "nvarchar2")
    private String name;
    @MyFieldAnnotation(columnName = "age", columnLength = 10, columnType = "number")
    private Integer age;

    private static void hello() {
        System.out.println("static hello ~");
    }

    private void sayHello() {
        System.out.println("non static hello ~");
    }

}
