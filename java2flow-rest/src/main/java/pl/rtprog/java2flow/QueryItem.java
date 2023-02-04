package pl.rtprog.java2flow;

import javax.ws.rs.QueryParam;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with information about request query parameter.
 *
 * @author Ryszard Trojnacki
 */
public class QueryItem {
    /**
     * Name of this query parameter
     */
    private final String name;
    /**
     * Type of this query parameter in Java
     */
    private final Type type;

    public QueryItem(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public static List<QueryItem> of(Method method) {
        ArrayList<QueryItem> res=new ArrayList<>();
        for(int i=0;i<method.getParameterCount();++i) {
            QueryParam p=RestUtils.find(method.getParameterAnnotations()[i], QueryParam.class);
            if(p==null) continue;   // not a FormParam
            res.add(new QueryItem(p.value(), method.getGenericParameterTypes()[i]));
        }
        return res;
    }
}
