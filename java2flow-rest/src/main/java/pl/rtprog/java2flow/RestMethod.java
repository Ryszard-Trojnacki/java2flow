package pl.rtprog.java2flow;

import pl.rtprog.java2flow.structs.JavaTypeInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

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


    private RestMethod(Class<?> clazz, Method method, String type, String path, JavaTypeInfo body, JavaTypeInfo result, PathFragment[] fragments) {
        this.clazz = clazz;
        this.method = method;
        this.type = type;
        this.path = path;
        this.body = body;
        this.result = result;
        this.fragments = fragments;
        this.pathVariables= fragments!=null && Arrays.stream(fragments).anyMatch(f -> f instanceof PathFragment.ParamPathFragment);
    }

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

    public static RestMethod of(Method method) throws IllegalArgumentException {
        Class<?> owner=method.getDeclaringClass();
        Path classPath=owner.getAnnotation(Path.class);
        if(classPath==null) throw new IllegalArgumentException("Missing @Path annotation at class "+owner.getName());
        Path methodPath=method.getAnnotation(Path.class);
        String type= RestUtils.getMethodType(method);
        if(type==null) throw new IllegalArgumentException("Missing method type (@GET, @POST, @PUT, @HEAD, @DELETE, @OPTION) annotation at method "+method.getName());
        String path=classPath.value()+(methodPath!=null?"/"+methodPath.value():"");
        String[] strFragments=path.split("/");
        PathFragment[] fragments=new PathFragment[strFragments.length];
        for(int i=0;i<strFragments.length;++i) {
            String str=strFragments[i];
            String name=RestUtils.isVariable(str);
            if(name==null) {    // not a variable path fragment
                fragments[i]= PathFragment.of(str);
                continue;
            }
            int pi=RestUtils.findPathParam(method, name);
            if(pi==-1) throw new IllegalArgumentException("Missing @PathParam for '"+name+"' in method "+method.getName());
            fragments[i]= PathFragment.of(
                    method.getParameterTypes()[pi],
                    method.getGenericParameterTypes()[pi],
                    method.getAnnotatedParameterTypes()[pi],
                    method.getParameterAnnotations()[pi],
                    name
            );
        }

        return new RestMethod(
                owner, method, type, path,
                null,
                JavaTypeInfo.returnOf(method),
                fragments
        );

    }

}
