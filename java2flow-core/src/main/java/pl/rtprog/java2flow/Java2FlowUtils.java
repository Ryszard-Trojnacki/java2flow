package pl.rtprog.java2flow;

import com.fasterxml.jackson.databind.BeanProperty;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

/**
 * Helper functions.
 *
 * @author Ryszard Trojnacki
 */
public class Java2FlowUtils {
    /**
     * Available NotNull annotations in classpath.
     */
    private final static Class<? extends Annotation> notNulls[];

    private static void addIfExists(ArrayList<Class<? extends Annotation>> list, String className) {
        try {
            Class<?> clazz=Class.forName(className);
            if(!clazz.isAnnotation()) return;
            list.add((Class<? extends Annotation>) clazz);
        } catch (ClassNotFoundException e) {
        }
    }

    static {
        ArrayList<Class<? extends Annotation>> nn=new ArrayList<>();
        nn.add(NotNull.class);
        addIfExists(nn, "org.jetbrains.annotations.NotNull");   // This will not work, because it is Class RetentionPolicy
        addIfExists(nn, "javax.validation.constraints.NotNull");
        notNulls=nn.toArray(new Class[0]);
    }

    public static boolean isNotNull(BeanProperty prop) {
        for(Class<? extends Annotation> c: notNulls) {
            if(prop.getAnnotation(c)!=null) return true;
        }
        return false;
    }
}
