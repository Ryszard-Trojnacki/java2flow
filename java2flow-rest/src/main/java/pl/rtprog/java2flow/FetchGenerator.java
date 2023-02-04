package pl.rtprog.java2flow;

import pl.rtprog.java2flow.js.JsGenerator;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Generic (single) method generator with types.
 *
 * @author Ryszard Trojnacki
 */
public class FetchGenerator {
    private final JsGenerator o;
    private final Java2Flow types;
    private final String networkFunc;
    private final String networkImport;
    private final String typesImport;

    public FetchGenerator(JsGenerator o, Java2Flow types, String networkFunc, String networkImport, String typesImport) {
        this.o=o;
        this.types=types;
        this.networkFunc=networkFunc;
        this.networkImport=networkImport;
        this.typesImport=typesImport;
    }

    private final ArrayList<RestMethod> methods=new ArrayList<>();

    public void export(Class<?> clazz) {
        if(!clazz.isAnnotationPresent(Path.class)) return;
        for(Method m: clazz.getMethods()) {
            try {
                methods.add(RestMethod.of(m));
            }catch (IllegalArgumentException e) {}
        }
    }

    /**
     * Generate API call functions with generic fetch method.
     */
    public void exportFunctions() {
        o.addHeader();
        o.ln(networkImport);
        o.ln(typesImport);

        for(RestMethod m: methods) {
            if(o.isJsdoc()) {
                o.ln("/**").ln(" * @function");
                for(PathFragment f: m.getFragments()) {
                    if(!(f instanceof PathFragment.ParamPathFragment)) continue;
                    PathFragment.ParamPathFragment pf=(PathFragment.ParamPathFragment)f;
                    o.a(" * @param {").a(types.getJavaScriptType(pf)).a("} ").a(pf.getName()).eol();
                }
                if(m.getBody()!=null) {
                    o.a(" * @param {").a(types.getJavaScriptType(m.getBody())).a("} body").eol();
                }
                o.a(" * @return {Promise<").a(types.getJavaScriptType(m.getResult())).a(">}").eol();
                o.ln(" */");
            }
            o.a("export function ").a(Java2FlowUtils.uncapitalize(m.getClazz().getSimpleName()))
                    .a(Java2FlowUtils.capitalize(m.getMethod().getName()))
                    .a("(");
            {   // parameters
                boolean first = true;
                for (PathFragment f : m.getFragments()) {
                    if (!(f instanceof PathFragment.ParamPathFragment)) continue;
                    if (first) first = false;
                    else o.a(", ");
                    PathFragment.ParamPathFragment pf = (PathFragment.ParamPathFragment) f;
                    o.a(pf.getName());
                    if (o.isFlow()) o.a(": ").a(types.getJavaScriptType(pf));
                }
                if (m.getBody() != null) {
                    if (first) first = false;
                    else o.a(", ");
                    o.a("body");
                    if (o.isFlow()) o.a(": ").a(types.getJavaScriptType(m.getBody()));
                }
                o.a(")");
            }
            if(o.isFlow()) o.a(": Promise<").a(types.getJavaScriptType(m.getResult())).a(">");
            o.ln(" {");
            o.enter().a("return ").a(networkFunc).a("(").eol().enter();
            if(m.hasPathVariables()) {
                StringBuilder pathBuf=new StringBuilder();
                boolean first=true;

                o.a("[ ");

                for(PathFragment f: m.getFragments()) {
                    if(f instanceof PathFragment.ConstPathFragment) {
                        PathFragment.ConstPathFragment cf=(PathFragment.ConstPathFragment)f;
                        if(pathBuf.length()>0) pathBuf.append('/');
                        pathBuf.append(cf.getValue());
                        continue;
                    }
                    if(first) first=false;
                    else o.a(", ");

                    if(pathBuf.length()>0) {
                        o.a('\"').a(pathBuf).a("\", ");
                        pathBuf.setLength(0);
                    }

                    PathFragment.ParamPathFragment pf=(PathFragment.ParamPathFragment)f;
                    o.a(pf.getName());
                }
                if(pathBuf.length()>0) {
                    if(!first) o.a(", ");
                    o.a('\"').a(pathBuf).a('\"');
                }
                o.a("]");
            } else o.a("\"").a(m.getPath()).a("\"");

            o.ln(",").a('\"').a(m.getType()).ln(",");

            o.ln("null,");

            if(m.getBody()!=null) {
                o.a("body");
            } else o.a("null");

            o.eol().leave().ln(");").leave().ln("}").eol();
        }
    }
}
