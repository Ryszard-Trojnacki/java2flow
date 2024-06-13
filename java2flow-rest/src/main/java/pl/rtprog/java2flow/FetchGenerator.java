package pl.rtprog.java2flow;

import pl.rtprog.java2flow.js.JsGenerator;
import pl.rtprog.java2flow.structs.NamedTypeInfo;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic (single) method generator with types.
 *
 * @author Ryszard Trojnacki
 */
public class FetchGenerator {
    private final static Class<? extends Annotation> FLOW_IGNORE;

    static {
        FLOW_IGNORE=Java2FlowUtils.getIfExits("pl.rtprog.java2flow.FlowIgnore");
    }

    private final JsGenerator o;
    private final Java2Flow types;
    private final String networkFunc;
    private final String networkImport;
    private final String typesFile;
    private final Set<String> usedTypes;

    public FetchGenerator(JsGenerator o, Java2Flow types, String networkFunc, String networkImport, String typesFile) {
        this.o=o;
        this.types=types;
        this.networkFunc=networkFunc;
        this.networkImport=networkImport;
        this.typesFile=typesFile;
        this.usedTypes=new HashSet<>();
    }

    private final ArrayList<RestMethod> methods=new ArrayList<>();

    public void register(Class<?> clazz) {
        if(!clazz.isAnnotationPresent(Path.class) && !clazz.isAnnotationPresent(jakarta.ws.rs.Path.class)) return;
        for(Method m: clazz.getMethods()) {
            if(FLOW_IGNORE!=null && m.isAnnotationPresent(FLOW_IGNORE)) continue;
            try {
                methods.add(RestMethod.of(m));
            }catch (IllegalArgumentException e) {}
        }
    }

    public void addHeader() {
        if(!o.addHeader()) return;
        calcImports();
        o.ln(networkImport);
        if(typesFile!=null && o.isFlow() && !usedTypes.isEmpty()) {
            o.a("import {");
            boolean f=true;
            for(String type: usedTypes) {
                if(f) f=false;
                else o.a(", ");
                o.a(type);
            }
            o.a("} from '").a(typesFile).ln("';");
        }
        o.eol();
    }

    private void addType(String type) {
        if(Java2FlowUtils.isJavaScriptType(type)) return;
        Java2FlowUtils.processGeneric(type, t -> {
            if(!Java2FlowUtils.isJavaScriptType(t)) usedTypes.add(t);
            return t;
        });
    }

    private void calcImports() {
        for(RestMethod m: methods) {
            for(PathFragment f: m.getFragments()) {
                if(f instanceof PathFragment.ParamPathFragment) {
                    addType(types.getJavaScriptType((PathFragment.ParamPathFragment)f));
                }
            }
            if(m.getBody()!=null) addType(types.getJavaScriptType(m.getBody()));
            for(NamedTypeInfo q: m.getQuery()) {
                addType(types.getJavaScriptType(q));
            }
            addType(types.getJavaScriptType(m.getResult()));
        }
    }

    private void appendPath(RestMethod m, boolean flowTypes, boolean jsTypes) {
        if(!m.hasPathVariables()) {
            o.quot(m.getPath());
            return;
        }
        StringBuilder path=new StringBuilder();
        boolean pathEmpty=true;
        boolean first=true;
        o.a("[ ");
        for(PathFragment f: m.getFragments()) {
            if(f instanceof PathFragment.ConstPathFragment) {
                PathFragment.ConstPathFragment cf=(PathFragment.ConstPathFragment)f;
                if(pathEmpty) pathEmpty=false;
                else path.append('/');

                path.append(cf.getValue());
                continue;
            }
            if(first) first=false;
            else o.a(", ");

            if(!pathEmpty) {
                o.quot(path);
                o.a(", ");
                path.setLength(0);
                pathEmpty=true;
            }

            PathFragment.ParamPathFragment pf=(PathFragment.ParamPathFragment)f;
            if(flowTypes) o.a(types.getJavaScriptType(pf));
            else if(jsTypes) o.a(types.getJavaScriptType(pf));
            else o.a(pf.getName());
        }
        if(!pathEmpty) {
            if(!first) o.a(", ");
            o.a('\"').a(path).a('\"');
        }
        o.a(" ]");
    }

    public void exportPathFunction() {
        if(methods.isEmpty()) return;
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
            Set<String> paths=new HashSet<>();
            boolean first=true;
            for (RestMethod m: methods) {
                if(!paths.add(m.getPath())) continue;

                if(first) first=false;
                else o.ln(" | ");
                appendPath(m, true, false);
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
                    o.a(" * @param {").a(types.getJavaScriptType(pf)).a("} ").a(pf.getName()).eol();
                }
                if(m.getBody()!=null) {
                    o.a(" * @param {").a(types.getJavaScriptType(m.getBody())).a("} body").eol();
                }
                o.a(" * @return {Promise<").a(m.getResult()==null?"void":types.getJavaScriptType(m.getResult())).a(">}").eol();
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
                for(NamedTypeInfo q: m.getQuery()) {
                    if(first) first=false;
                    else o.a(", ");
                    o.a(q.getName());
                    if(o.isFlow()) o.a(": ").a(types.getJavaScriptType(q));
                }

                o.a(")");

            }
            if(o.isFlow()) o.a(": Promise<").a(types.getJavaScriptType(m.getResult())).a(">");
            o.ln(" {");
            o.enter().a("return ").a(networkFunc).a("(").eol().enter();
            appendPath(m, false, false);

            o.ln(",").a('\"').a(m.getType()).a("\",");

            boolean query=false;

            if(m.getQuery().length>0) {
                query=true;
                o.eol().a("{ ");
                for(NamedTypeInfo q: m.getQuery()) {
                    o.a(q.getName()).a(", ");
                }
                o.a(" },");
            }

            if(m.getBody()!=null && !query) {
                o.eol().a("null,");
            }

            if(m.getBody()!=null) {
                o.eol().a("body");
            } else {
//                o.a("undefined");
            }

            o.eol().leave().ln(");").leave().ln("}").eol();
        }
    }
}
