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
                public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//                    System.out.println("visitTypeAnnotation: "+typePath);
                    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
                }

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                    System.out.println("Visit: "+name+ " "+signature);
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
//                    System.out.println("VisitField: "+name+ " "+descriptor+" "+ value);
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
