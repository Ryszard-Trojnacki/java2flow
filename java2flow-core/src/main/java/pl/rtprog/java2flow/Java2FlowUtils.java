package pl.rtprog.java2flow;

import com.fasterxml.jackson.databind.BeanProperty;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.function.Function;

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
     * Helper function that returns true if string is not blank (not null and contains not only blank characters).
     * @param str string to test
     * @return true if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return str!=null && str.trim().length()>0;
    }

    /**
     * Method that appends to output buffer message.
     * Each line if appended with given prefix.
     * @param out output buffer
     * @param prefix prefix to append to each line
     * @param msg message to append
     * @param prefixFirst add (prepend) prefix to first line
     */
    public static void formatOutput(StringBuilder out, String prefix, String msg, boolean prefixFirst) {
        String[] lines=msg.trim().split("\n");
        boolean first=true;
        for(String l: lines) {
            if(prefixFirst || !first) out.append(prefix);
            if(first) first=false;
            out.append(l).append('\n');
        }
    }

    /**
     * Call to {@link #formatOutput(StringBuilder, String, String, boolean)} with <code>prefixFirst=true</code>
     * @see #formatOutput(StringBuilder, String, String, boolean)
     * @param out output buffer
     * @param prefix prefix to append to each line
     * @param msg message to append
     */
    public static void formatOutput(StringBuilder out, String prefix, String msg) {
        formatOutput(out, prefix, msg, true);
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

    public static String capitalize(String in) {
        if(in==null || in.length()==0) return in;
        return Character.toUpperCase(in.charAt(0))+in.substring(1);
    }

    /**
     * Function that converts multiline string, to format <code> * _line\n</code>
     * @param sb builder to append JSDoc to
     * @param comment multiline comment
     */
    public static void formatComment(StringBuilder sb, String comment) {
        if(isBlank(comment)) return;
        String[] lines=comment.trim().split("\n");
        for(String l: lines) {
            sb.append(" * ").append(l).append("\n");
        }
    }

    /**
     * Wrapper around {@link #formatComment(StringBuilder, String)}.
     * @see #formatComment(StringBuilder, String)
     * @param comment multiline comment
     * @return comment in JSDoc comment format
     */
    public static String formatComment(String comment) {
        StringBuilder sb=new StringBuilder();
        formatComment(sb, comment);
        return sb.toString();
    }

    public static boolean isJavaScriptType(String type) {
        return "string".equals(type) || "number".equals(type) || "Object".equals(type) ||
                "boolean".equals(type) || "symbol".equals(type) || "object".equals(type) ||
                "bigint".equals(type) || "BigInt".equals(type) || "Number".equals(type) ||
                "String".equals(type) || "Date".equals(type) || "Boolean".equals(type) ||
                "void".equals(type) || "Array".equals(type);
    }

    /**
     * Helper function that's process each type in generic type declaration.
     * @param type type to process
     * @param processor fragment convert function
     * @return converted type
     */
    public static String processGeneric(String type, Function<String, String> processor) {
        if(type==null || type.length()==0) return type;
        int pos=0;
        StringBuilder sb=new StringBuilder(type.length());
        for(int i=0;i<type.length();++i) {
            char c=type.charAt(i);
            if(c==',' || c=='<' || c=='>' || c==' ') {
                if(pos<i) sb.append(processor.apply(type.substring(pos, i)));
                sb.append(c);
                pos=i+1;
            }
        }
        if(pos<type.length()) sb.append(processor.apply(type.substring(pos)));
        return sb.toString();
    }
}
