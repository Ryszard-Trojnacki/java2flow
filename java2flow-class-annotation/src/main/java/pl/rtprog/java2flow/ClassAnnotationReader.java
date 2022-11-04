package pl.rtprog.java2flow;

import org.objectweb.asm.*;
import pl.rtprog.java2flow.interfaces.ClassAnnotations;
import pl.rtprog.java2flow.interfaces.JavaAnnotationProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

/**
 * Class for reading annotation with class retention policy ({@link java.lang.annotation.RetentionPolicy#CLASS}
 * using ASM library.
 *
 * @author Ryszard Trojnacki
 */
public class ClassAnnotationReader implements JavaAnnotationProvider {
    private final static String[] NOT_NULL=new String[] {
            "Lorg/jetbrains/annotations/NotNull;"
    };

    private static boolean contains(String[] values, String item) {
        for(String i: values) {
            if(i.equals(item)) return true;
        }
        return false;
    }

    private static class ClassAnnotationsImpl implements ClassAnnotations {
        private final HashSet<String> notNull=new HashSet<>();

        @Override
        public boolean isNotNull(String field) {
            return notNull.contains(field);
        }

        public void setNotNull(String field) {
            notNull.add(field);
        }
    }

    private final ClassLoader loader;

    public ClassAnnotationReader(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public ClassAnnotations get(Class<?> clazz) {
        try {
//            System.out.println("ClassAnnotations for: "+clazz);
            String pathName=clazz.getName().replace('.', '/')+".class";
            final ClassAnnotationsImpl res=new ClassAnnotationsImpl();
            ClassReader cr = new ClassReader(Objects.requireNonNull(loader != null ? loader.getResourceAsStream(pathName) : ClassLoader.getSystemResourceAsStream(pathName)));
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    String field;
                    if(name.length()>2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2))) {
                        field=Java2FlowUtils.uncapitalize(name.substring(2));
                    } else if(name.length()>3 && Character.isUpperCase(name.charAt(3))) {
                        if(name.startsWith("get")) {
                            if(!descriptor.startsWith("()") || descriptor.equals("()V")) return null;
//                        } else if(name.startsWith("set")) {   // Only getters
//                            if(!descriptor.equals(")V")) return null;
                        }
                        field=Java2FlowUtils.uncapitalize(name.substring(3));
                    } else return null;

                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            if(contains(NOT_NULL, descriptor)) res.setNotNull(field);
                            return null;
                        }
                    };
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    return new FieldVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            if(contains(NOT_NULL, descriptor)) res.setNotNull(name);
                            return null;
                        }
                    };
                }

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    return null;
                }
            };
            cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
            return res;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
