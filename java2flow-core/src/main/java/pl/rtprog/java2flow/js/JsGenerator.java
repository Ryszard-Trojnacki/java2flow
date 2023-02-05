package pl.rtprog.java2flow.js;

import java.io.IOException;

/**
 * Helper class for generating JavaScript code.
 */
public class JsGenerator {
    private final Appendable sb;
    /**
     * Line buffer
     */
    private final StringBuilder line=new StringBuilder(128);

    private final String identText="\t";

    private final boolean flow;
    private final boolean jsdoc;

    private boolean header=false;

    /**
     * Current ident for code/comments
     */
    private int ident=0;


    public JsGenerator(Appendable sb, int ident, boolean flow, boolean jsdoc) {
        this.sb = sb;
        this.ident = ident;
        this.flow=flow;
        this.jsdoc=jsdoc;
    }


    public boolean isFlow() {
        return flow;
    }

    public boolean isJsdoc() {
        return jsdoc;
    }

    private void flushLine(CharSequence line) {
        try {
            for(int i=0;i<ident;++i) sb.append(identText);
            sb.append(line);
            sb.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void append(CharSequence value) {
        if(value==null || value.length()==0) return;
        int st=0;

        for(int i=0;i<value.length();++i) {
            if(value.charAt(i)=='\n') {
                if(line.length()==0) flushLine(value.subSequence(st, i));
                else {
                    line.append(value.subSequence(st, i));
                    flushLine(line);
                    line.setLength(0);
                }
                st=i+1;
            }
        }
        if(st==0) line.append(value);
        else if(st<value.length()) line.append(value.subSequence(st, value.length()));
    }

    public JsGenerator enter() {
        ++ident;
        return this;
    }

    public JsGenerator leave() {
        --ident;
        return this;
    }

    public JsGenerator a(CharSequence value) {
        append(value);
        return this;
    }

    public JsGenerator a(char c) {
        if(c=='\n') {
            flushLine(line);
            line.setLength(0);
        } else {
            line.append(c);
        }
        return this;
    }

    public JsGenerator a(int value) {
        append(String.valueOf(value));
        return this;
    }

    public JsGenerator ln(String value) {
        append(value);
        flushLine(line);
        line.setLength(0);
        return this;
    }

    public void close() {
        if(line.length()>0) {
            flushLine(line);
            line.setLength(0);
        }
    }

    public JsGenerator eol() {
        flushLine(line);
        line.setLength(0);
        return this;
    }

    public JsGenerator flow(String v) {
        if(flow) append(v);
        return this;
    }

    public JsGenerator jsdoc(String v) {
        if(jsdoc) append(v);
        return this;
    }

    public boolean addHeader() {
        if(header) return false;
        header=true;
        if(flow) ln("//@flow").eol();
        return true;
    }

}
