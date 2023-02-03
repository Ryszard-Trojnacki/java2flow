package pl.rtprog.java2flow;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Class for generating Flow types of JAX-RS.
 *
 * @author Ryszard Trojnacki
 */
public class JavaRest2Flow {
    protected final StringBuilder out=new StringBuilder(1024);

    /**
     * Generate Flow output (types)
     */
    protected final boolean flow;

    /**
     * Generate JSDoc output (types)
     */
    protected final boolean jsdoc;

    public JavaRest2Flow(boolean flow, boolean jsdoc) {
        this.flow=flow;
        this.jsdoc=jsdoc;
        if(!flow && !jsdoc) throw new IllegalArgumentException("At last one generator has to be enabled");
    }


    private void processMethod(String prefix, Method method) {
        Path methodPath=method.getAnnotation(Path.class);
        if(methodPath==null) return;
        String[] parts=methodPath.value().split("/");
        HashMap<String, Type> params=new HashMap<>();
        for(int i=0;i<method.getParameterCount();++i) {
            Type pt=method.getGenericParameterTypes()[i];
            PathParam pp=JavaRest2FlowUtils.find(method.getParameterAnnotations()[i], PathParam.class);

        }

        for(String p: parts) {
            String v=JavaRest2FlowUtils.isVariable(p);

        }
    }

    public void export(Class<?> restClass) {
        Path path=restClass.getAnnotation(Path.class);
        if(path==null) return;
        for(Method m: restClass.getMethods()) {
            Path methodPath=m.getAnnotation(Path.class);
            if(methodPath==null) continue;
            String[] parts=methodPath.value().split("/");


            String fullPath="/"+path.value()+"/"+methodPath.value();

            if(m.isAnnotationPresent(GET.class)) {

            } else if(m.isAnnotationPresent(POST.class)) {

            } else if(m.isAnnotationPresent(PUT.class)) {

            } else if(m.isAnnotationPresent(HEAD.class)) {

            } else if(m.isAnnotationPresent(DELETE.class)) {

            }
        }
    }
}
