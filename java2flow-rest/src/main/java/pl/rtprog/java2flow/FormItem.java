package pl.rtprog.java2flow;

import javax.ws.rs.FormParam;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with information about form parameter.
 *
 * @author Ryszard Trojnacki
 */
public class FormItem {
    /**
     * Name of this form parameter
     */
    private final String name;
    /**
     * Java type of this form parameter.
     */
    private final Type type;

    public FormItem(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    /**
     * Method finds parameters with {@link FormParam}.
     * @param method method for which find {@link FormParam}
     * @return list of found {@link FormParam} in {@link FormItem} form.
     */
    public static List<FormItem> of(Method method) {
        ArrayList<FormItem> res=new ArrayList<>();
        for(int i=0;i<method.getParameterCount();++i) {
            FormParam p=RestUtils.find(method.getParameterAnnotations()[i], FormParam.class);
            if(p==null) continue;   // not a FormParam
            res.add(new FormItem(p.value(), method.getGenericParameterTypes()[i]));
        }
        return res;
    }
}
