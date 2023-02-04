package pl.rtprog.java2flow;

import pl.rtprog.java2flow.js.JsGenerator;

import java.util.List;

/**
 * Generic (single) method generator with types.
 *
 * @author Ryszard Trojnacki
 */
public class FetchGenerator {
    private JsGenerator out=new JsGenerator();

    public static void generate(Java2Flow gen, List<RestMethod> methods, boolean flow, boolean jsdoc) {
        JsGenerator o=new JsGenerator();

        if(flow) o.append("//@flow\n\n");

        if(jsdoc) {
            o.append("/**\n * @typedef {");
            boolean first=true;
            for(RestMethod m: methods) {
                if(first) first=false;
                else o.append("|\n");

                if(!m.hasPathVariables()) {
                    o.append('"').append(m.getPath()).append('"');
                } else {
                    o.append('[');
                    for(int i=0;i<m.getFragments().length;++i) {
                        PathItem pi=m.getFragments()[i];
                        if(i>0) o.append(", ");

                        if(pi instanceof PathItem.ConstPathItem) o.append('"').append(((PathItem.ConstPathItem)pi).getValue()).append('"');
                        else o.append(gen.getJavaScriptType((PathItem.ParamPathItem)pi));
                    }
                    o.append(']');
                }
            }
            o.append("} NetworkPaths\n");
            o.append(" */");
        }
        if(flow) {
            o.append("export type NetworkPaths = \n");
            boolean first=true;
            for(RestMethod m: methods) {
                if(first) first=false;
                else o.append("|\n");

                if(!m.hasPathVariables()) {
                    o.append('"').append(m.getPath()).append('"');
                } else {
                    o.append('[');
                    for(int i=0;i<m.getFragments().length;++i) {
                        PathItem pi=m.getFragments()[i];
                        if(i>0) o.append(", ");

                        if(pi instanceof PathItem.ConstPathItem) o.append('"').append(((PathItem.ConstPathItem)pi).getValue()).append('"');
                        else o.append(gen.getJavaScriptType((PathItem.ParamPathItem)pi));
                    }
                    o.append(']');
                }
            }
            o.append(";\n\n");
        }

        if(jsdoc) {
            o.append("/**\n");
            o.append(" * @param {NetworkPaths} path\n");
            o.append(" */\n");
        }

        o.append("export default function networkCall(");

        o.append("path");
        if(flow) o.append(": string|NetworkPaths");

        o.append(") {\n");
        o.append("  ");
        o.append("};\n");
    }
}
