package pl.rtprog.java2flow;

import pl.rtprog.java2flow.interfaces.ClassJavaDoc;
import pl.rtprog.java2flow.interfaces.FieldJavaDoc;

import java.util.HashMap;
import java.util.Map;

/**
 * Class with parsed JavaDoc information.
 *
 * @author Ryszard Trojnacki
 */
public class Javadoc implements ClassJavaDoc {
    private final Class<?> about;

    private String author;

    private String comment;

    private Map<String, String> fields;

    public Javadoc(Class<?> about) {
        this.about = about;
        this.fields=new HashMap<>();
    }

    @Override
    public Class<?> getAbout() {
        return about;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public FieldJavaDoc get(String field) {
        String fc=fields.get(field);
        if(fc==null) return null;
        return new FieldJavaDoc() {
            @Override
            public String getAbout() {
                return field;
            }

            @Override
            public String getComment() {
                return fc;
            }
        };
    }

    public void add(String field, String comment, boolean override) {
        if(!override && fields.containsKey(field)) return;
        fields.put(field, comment);
    }
}
