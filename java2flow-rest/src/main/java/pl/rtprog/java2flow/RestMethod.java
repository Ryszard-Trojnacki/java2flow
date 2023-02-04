package pl.rtprog.java2flow;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
    private final Type body;

    /**
     * Result type of API call.
     */
    private final Type result;

    /**
     * Path split into fragments
     */
    private final PathItem[] fragments;


    private RestMethod(Class<?> clazz, Method method, String type, String path, Type body, Type result, PathItem[] fragments) {
        this.clazz = clazz;
        this.method = method;
        this.type = type;
        this.path = path;
        this.body = body;
        this.result = result;
        this.fragments = fragments;
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
        PathItem[] fragments=new PathItem[strFragments.length];
        for(int i=0;i<strFragments.length;++i) {
            String str=strFragments[i];
            String name=RestUtils.isVariable(str);
            if(name==null) {    // not a variable path fragment
                fragments[i]=PathItem.of(str);
                continue;
            }
            int pi=RestUtils.findPathParam(method, name);
            if(pi==-1) throw new IllegalArgumentException("Missing @PathParam for '"+name+"' in method "+method.getName());
            fragments[i]=PathItem.of(name, method.getGenericParameterTypes()[pi]);
        }

        return new RestMethod(
                owner, method, type, path,
                null,
                method.getGenericReturnType(),
                fragments
        );

    }

}
