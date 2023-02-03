package pl.rtprog.java2flow;

import java.lang.annotation.Annotation;

/**
 * Helper method for processing REST API.
 *
 * @author Ryszard Trojnackis
 */
public class JavaRest2FlowUtils {

    public static String isVariable(String pathPart) {
        if(pathPart.startsWith("{") && pathPart.endsWith("}")) {
            return pathPart.substring(1, pathPart.length()-1);
        }
        return null;
    }

    public static <T extends Annotation> T find(Annotation[] annotations, Class<T> annotation) {
        if(annotations==null) return null;
        for(int i=0;i<annotations.length;++i) {
            if(annotations[i].annotationType()==annotation) return (T)annotations[i];
        }
        return null;
    }

}
