package pl.rtprog.java2flow;

import pl.rtprog.java2flow.structs.NamedTypeInfo;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        if(method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(jakarta.ws.rs.GET.class)) return HttpMethod.GET;
        if(method.isAnnotationPresent(POST.class) || method.isAnnotationPresent(jakarta.ws.rs.POST.class)) return HttpMethod.POST;
        if(method.isAnnotationPresent(HEAD.class) || method.isAnnotationPresent(jakarta.ws.rs.HEAD.class)) return HttpMethod.HEAD;
        if(method.isAnnotationPresent(DELETE.class) || method.isAnnotationPresent(jakarta.ws.rs.DELETE.class)) return HttpMethod.DELETE;
        if(method.isAnnotationPresent(PUT.class) || method.isAnnotationPresent(jakarta.ws.rs.PUT.class)) return HttpMethod.PUT;
        if(method.isAnnotationPresent(OPTIONS.class) || method.isAnnotationPresent(jakarta.ws.rs.OPTIONS.class)) return HttpMethod.OPTIONS;
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
            if(pp!=null) {
                if (name.equals(pp.value())) return i;
            } else {
                jakarta.ws.rs.PathParam jpp=find(method.getParameterAnnotations()[i], jakarta.ws.rs.PathParam.class);
                if(jpp!=null) {
                    if (name.equals(jpp.value())) return i;
                }
            }

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

    /**
     * Method finds parameters with {@link FormParam}.
     * @param method method for which find {@link FormParam}
     * @return list of found {@link FormParam} in {@link NamedTypeInfo} form.
     */
    public static List<NamedTypeInfo> getFormParams(Method method) {
        ArrayList<NamedTypeInfo> res=new ArrayList<>();
        for(int i=0;i<method.getParameterCount();++i) {
            FormParam p=RestUtils.find(method.getParameterAnnotations()[i], FormParam.class);
            if(p!=null) {
                res.add(NamedTypeInfo.forParameter(method, p.value(), i));
            } else {
                jakarta.ws.rs.FormParam jp=RestUtils.find(method.getParameterAnnotations()[i], jakarta.ws.rs.FormParam.class);
                if(jp!=null) {
                    res.add(NamedTypeInfo.forParameter(method, jp.value(), i));
                }

            }
        }
        return res;
    }

    public static List<NamedTypeInfo> getQueryParams(Method method) {
        ArrayList<NamedTypeInfo> res=new ArrayList<>();
        for(int i=0;i<method.getParameterCount();++i) {
            QueryParam p=RestUtils.find(method.getParameterAnnotations()[i], QueryParam.class);
            if(p!=null) {
                res.add(NamedTypeInfo.forParameter(method, p.value(), i));
            } else {
                jakarta.ws.rs.QueryParam jp=RestUtils.find(method.getParameterAnnotations()[i], jakarta.ws.rs.QueryParam.class);
                if(jp!=null) {
                    res.add(NamedTypeInfo.forParameter(method, jp.value(), i));
                }
            }
        }
        return res;
    }

}
