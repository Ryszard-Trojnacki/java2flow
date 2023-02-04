package pl.rtprog.java2flow;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Helper method for processing REST API.
 *
 * @author Ryszard Trojnackis
 */
public class RestUtils {

    /**
     * Check if path fragment is a variable declaration, that is <code>{variable_name}</code>.
     * @param fragment path fragment to check
     * @return variable name or null if not a variable name fragment
     */
    public static String isVariable(String fragment) {
        if(fragment==null) return null;
        if(fragment.startsWith("{") && fragment.endsWith("}")) {
            return fragment.substring(1, fragment.length()-1);
        }
        return null;
    }

    /**
     * Helper function that searches for annotation in array.
     * @param annotations annotations to search in
     * @param annotation annotation type to find
     * @return found annotation or null if not found
     * @param <T> annotation type
     */
    public static <T extends Annotation> T find(Annotation[] annotations, Class<T> annotation) {
        if(annotations==null) return null;
        for(int i=0;i<annotations.length;++i) {
            if(annotations[i].annotationType()==annotation) return (T)annotations[i];
        }
        return null;
    }

    /**
     * Functions that returns method HTTP type
     * @param method method to check for type annotations
     * @return HTTP method type ({@link HttpMethod} or null if no valid annotation
     */
    public static String getMethodType(Method method) {
        if(method==null) return null;
        if(method.isAnnotationPresent(GET.class)) return HttpMethod.GET;
        if(method.isAnnotationPresent(POST.class)) return HttpMethod.POST;
        if(method.isAnnotationPresent(HEAD.class)) return HttpMethod.HEAD;
        if(method.isAnnotationPresent(DELETE.class)) return HttpMethod.DELETE;
        if(method.isAnnotationPresent(PUT.class)) return HttpMethod.PUT;
        if(method.isAnnotationPresent(OPTIONS.class)) return HttpMethod.OPTIONS;
        return null;
    }


    /**
     * Functions that tries to find path parameter type for given name
     * @param method method to check for
     * @param name name of parameter
     * @return number of found parameter or -1 if not found
     */
    public static int findPathParam(Method method, String name) {
        for(int i=0;i<method.getParameterCount();++i) {
            PathParam pp=find(method.getParameterAnnotations()[i], PathParam.class);
            if(pp==null) continue;
            if(name.equals(pp.value())) return i;
        }
        return -1;
    }

    /**
     * Helper function that joins two path fragments
     * @param path1 one path fragment
     * @param path2 second path fragment
     * @return combined path
     */
    public static String pathJoin(String path1, String path2) {
        if(path2==null || path2.length()==0) return path1;
        if(path2.startsWith("/")) {
            if(path1.endsWith("/")) return path1+path2.substring(1);
            else return path1+path2;
        } else {
            if(path1.endsWith("/")) return path1+path2;
            else return path1+'/'+path2;
        }
    }

}
