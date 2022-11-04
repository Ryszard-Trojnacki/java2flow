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
    private final static Class<? extends Annotation>[] notNulls;

    @SuppressWarnings("unchecked")
    private static void addIfExists(ArrayList<Class<? extends Annotation>> list, String className) {
        try {
            Class<?> clazz=Class.forName(className);
            if(!clazz.isAnnotation()) return;
            list.add((Class<? extends Annotation>) clazz);
        } catch (ClassNotFoundException ignored) {
        }
    }

    static {
        ArrayList<Class<? extends Annotation>> nn=new ArrayList<>();
        nn.add(NotNull.class);
//        addIfExists(nn, "org.jetbrains.annotations.NotNull");   // This will not work, because it is Class RetentionPolicy
        addIfExists(nn, "javax.validation.constraints.NotNull");
        addIfExists(nn, "io.ebean.annotation.NotNull");
        notNulls=nn.toArray(new Class[0]);
    }

    public static boolean isNotNull(BeanProperty prop) {
        for(Class<? extends Annotation> c: notNulls) {
            if(prop.getAnnotation(c)!=null) return true;
        }
        return false;
    }

    /**
     * Helper function that returns true if string is empty (is null or length is 0).
     * @param str string to test
     * @return true if string is empty (null or length is 0)
     */
    public static boolean isEmpty(String str) {
        return str==null || str.length()==0;
    }

    /**
     * Helper function that returns true if string is blank (is null or trimmed length is 0).
     * @param str string to test
     * @return true if string is blank (null or trimmed length is 0)
     */
    public static boolean isBlank(String str) {
        return str==null || str.trim().length()==0;
    }

    /**
     * Method that appends to output buffer message.
     * Each line if appended with given prefix.
     * @param out output buffer
     * @param prefix prefix to append to each line
     * @param msg message to append
     */
    public static void formatOutput(StringBuilder out, String prefix, String msg) {
        String[] lines=msg.trim().split("\n");
        for(String l: lines) {
            out.append(prefix).append(l).append('\n');
        }
    }

    /**
     * Uncapitalize input string
     * @param in string to be uncapitalized
     * @return uncapitalized string
     */
    public static String uncapitalize(String in) {
        if(in==null || in.length()==0) return in;
        return Character.toLowerCase(in.charAt(0))+in.substring(1);
    }
}
