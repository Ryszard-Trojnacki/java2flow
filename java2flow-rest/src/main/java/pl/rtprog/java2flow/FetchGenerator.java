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
    private final String typesFile;

    public FetchGenerator(JsGenerator o, Java2Flow types, String networkFunc, String networkImport, String typesFile) {
        this.o=o;
        this.types=types;
        this.networkFunc=networkFunc;
        this.networkImport=networkImport;
        this.typesFile=typesFile;
    }

    private final ArrayList<RestMethod> methods=new ArrayList<>();

    public void register(Class<?> clazz) {
        if(!clazz.isAnnotationPresent(Path.class)) return;
        for(Method m: clazz.getMethods()) {
            try {
                methods.add(RestMethod.of(m));
            }catch (IllegalArgumentException e) {}
        }
    }

    private String ft(String type) {
        if(typesFile==null || type==null || Java2FlowUtils.isJavaScriptType(type)) return type;
        return "types."+type;
    }

    private String jt(String type) {
        if(typesFile==null || type==null || Java2FlowUtils.isJavaScriptType(type)) return type;
        return "import('"+typesFile+"')."+type;
    }

    public void addHeader() {
        if(!o.addHeader()) return;
        o.ln(networkImport);
        if(typesFile!=null && o.isFlow()) {
            o.a("import * as types from '").a(typesFile).ln("';");
        }
        o.eol();
    }

    public void exportPathFunction() {
        addHeader();
//        if(o.isJsdoc()) {
//            o.ln("/**")
//                    .ln(" * @typedef {")
//                    .enter();
//            for(RestMethod m: methods) {
//
//            }
//        }
        if(o.isFlow()) {
            o.ln("export type NetworkPathType = ").enter();
            boolean first=true;
            for (RestMethod m: methods) {
                if(first) first=false;
                else o.ln(" | ");

                if(!m.hasPathVariables()) o.a('\"').a(m.getPath()).a('\"');
                else {
                    StringBuilder pathBuf=new StringBuilder();
                    boolean ff=true;
                    o.a("[ ");

                    for(PathFragment f: m.getFragments()) {
                        if(f instanceof PathFragment.ConstPathFragment) {
                            PathFragment.ConstPathFragment cf=(PathFragment.ConstPathFragment)f;
                            if(pathBuf.length()>0) pathBuf.append('/');
                            pathBuf.append(cf.getValue());
                            continue;
                        }
                        if(ff) ff=false;
                        else o.a(", ");

                        if(pathBuf.length()>0) {
                            o.a('\"').a(pathBuf).a("\", ");
                            pathBuf.setLength(0);
                        }

                        PathFragment.ParamPathFragment pf=(PathFragment.ParamPathFragment)f;
                        o.a(ft(types.getJavaScriptType(pf)));
                    }
                    if(pathBuf.length()>0) {
                        if(!ff) o.a(", ");
                        o.a('\"').a(pathBuf).a('\"');
                    }
                    o.a(" ]");
                }
            }
            o.ln(";");
            o.leave();
        }
        o.eol();
    }

    /**
     * Generate API call functions with generic fetch method.
     */
    public void exportFunctions() {
        addHeader();

        for(RestMethod m: methods) {
            if(o.isJsdoc()) {
                o.ln("/**").ln(" * @function");
                for(PathFragment f: m.getFragments()) {
                    if(!(f instanceof PathFragment.ParamPathFragment)) continue;
                    PathFragment.ParamPathFragment pf=(PathFragment.ParamPathFragment)f;
                    o.a(" * @param {").a(jt(types.getJavaScriptType(pf))).a("} ").a(pf.getName()).eol();
                }
                if(m.getBody()!=null) {
                    o.a(" * @param {").a(jt(types.getJavaScriptType(m.getBody()))).a("} body").eol();
                }
                o.a(" * @return {Promise<").a(jt(types.getJavaScriptType(m.getResult()))).a(">}").eol();
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
                    if (o.isFlow()) o.a(": ").a(ft(types.getJavaScriptType(pf)));
                }
                if (m.getBody() != null) {
                    if (first) first = false;
                    else o.a(", ");
                    o.a("body");
                    if (o.isFlow()) o.a(": ").a(ft(types.getJavaScriptType(m.getBody())));
                }
                o.a(")");
            }
            if(o.isFlow()) o.a(": Promise<").a(ft(types.getJavaScriptType(m.getResult()))).a(">");
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
