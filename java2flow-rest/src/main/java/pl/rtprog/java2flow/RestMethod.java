package pl.rtprog.java2flow;

import pl.rtprog.java2flow.structs.JavaTypeInfo;
import pl.rtprog.java2flow.structs.NamedTypeInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

/**
 * Class that holds information about REST API call.
 * It holds information for code generators.
 *
 * @author Ryszard Trojnacki
 */
public class RestMethod {
    /**
     * Class in which this method/call is declared
     */
    private final Class<?> clazz;

    /**
     * Method which implements this API call.
     */
    private final Method method;

    /**
     * HTTP call type. One of constants of {@link HttpMethod}
     */
    private final String type;

    /**
     * Full java path, including variables <code>{id}</code>.
     **/
    private final String path;

    /**
     * Body type or null if no body.
     */
    private final JavaTypeInfo body;

    /**
     * Result type of API call.
     */
    private final JavaTypeInfo result;

    /**
     * Path split into fragments
     */
    private final PathFragment[] fragments;

    private final boolean pathVariables;

    /**
     * Query parameters
     */
    private final NamedTypeInfo[] query;


    /**
     * Private constructor with all values.
     */
    private RestMethod(Class<?> clazz, Method method, String type, String path, NamedTypeInfo[] query, JavaTypeInfo body, JavaTypeInfo result, PathFragment[] fragments) {
        this.clazz = clazz;
        this.method = method;
        this.type = type;
        this.path = path;
        this.body = body;
        this.result = result;
        this.fragments = fragments;
        this.query=query;
        this.pathVariables= fragments!=null && Arrays.stream(fragments).anyMatch(f -> f instanceof PathFragment.ParamPathFragment);
    }

    /**
     * Getter for {@link #clazz}.
     */
    public Class<?> getClazz() {
        return clazz;
    }

    public Method getMethod() {
        return method;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public JavaTypeInfo getBody() {
        return body;
    }

    public JavaTypeInfo getResult() {
        return result;
    }

    public PathFragment[] getFragments() {
        return fragments;
    }

    public boolean hasPathVariables() {
        return pathVariables;
    }

    public NamedTypeInfo[] getQuery() {
        return query;
    }

    /**
     * Checks if other method has the same path.
     * @param other other method to check
     * @return true if other method has the same path
     */
    public boolean pathEquals(RestMethod other) {
        if(other==null) return false;
        return path.equals(other.path);

    }

    private static PathFragment[] computerFragments(Method method, String path) {
        String[] strFragments = path.split("/");
        ArrayList<PathFragment> fragments = new ArrayList<>(strFragments.length+1);
//        if(path.startsWith("/")) fragments.add(PathFragment.ROOT);

        for (int i = 0; i < strFragments.length; ++i) {
            String str = strFragments[i];
            String name = RestUtils.isVariable(str);
            if (name == null) {    // not a variable path fragment
                fragments.add(PathFragment.of(str));
                continue;
            }
            int pi = RestUtils.findPathParam(method, name);
            if (pi == -1) throw new IllegalArgumentException("Missing @PathParam for '" + name + "' in method " + method.getName());
            fragments.add(PathFragment.of(
                    method.getParameterTypes()[pi],
                    method.getGenericParameterTypes()[pi],
                    method.getAnnotatedParameterTypes()[pi],
                    method.getParameterAnnotations()[pi],
                    name
            ));
        }
        return fragments.toArray(new PathFragment[0]);
    }

    public static RestMethod of(Method method) throws IllegalArgumentException {
        Class<?> owner=method.getDeclaringClass();
        Path classPath=owner.getAnnotation(Path.class);
        if(classPath==null) throw new IllegalArgumentException("Missing @Path annotation at class "+owner.getName());
        Path methodPath=method.getAnnotation(Path.class);
        String type= RestUtils.getMethodType(method);
        if(type==null) throw new IllegalArgumentException("Missing method type (@GET, @POST, @PUT, @HEAD, @DELETE, @OPTION) annotation at method "+method.getName());
        String path=classPath.value()+(methodPath!=null?"/"+methodPath.value():"");
        PathFragment[] fragments=computerFragments(method,path);

        JavaTypeInfo returnType=JavaTypeInfo.returnOf(method);

        if(CompletionStage.class.isAssignableFrom(method.getReturnType())) {
            Type rt=method.getGenericReturnType();
            if(rt instanceof ParameterizedType) {
                ParameterizedType pt=(ParameterizedType)rt;
                if(pt.getActualTypeArguments()[0] instanceof Class) {
                    returnType = new JavaTypeInfo(
                            (Class<?>) pt.getActualTypeArguments()[0],
                            pt.getActualTypeArguments()[0],
                            method.getAnnotatedReturnType(),
                            method.getAnnotations()
                    );
                }
            }
        }

        ArrayList<NamedTypeInfo> query=new ArrayList<>();

        JavaTypeInfo body=null;

        for(int i=0;i<method.getParameterCount();++i) {
            Annotation[] pa=method.getParameterAnnotations()[i];
            QueryParam q=RestUtils.find(pa, QueryParam.class);
            if(q!=null) {
                query.add(NamedTypeInfo.forParameter(method, q.value(), i));
                continue;
            }

            if(RestUtils.find(pa, Context.class)!=null ||
                    RestUtils.find(pa, QueryParam.class)!=null ||
                    RestUtils.find(pa, PathParam.class)!=null) continue;
            body=new JavaTypeInfo(
                    method.getParameterTypes()[i],
                    method.getGenericParameterTypes()[i],
                    method.getAnnotatedParameterTypes()[i],
                    method.getParameterAnnotations()[i]
            );
            break;
        }

        return new RestMethod(
                owner, method, type, path,
                query.toArray(new NamedTypeInfo[0]),
                body,
                returnType,
                fragments
        );

    }

}
