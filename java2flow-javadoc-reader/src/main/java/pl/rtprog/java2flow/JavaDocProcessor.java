package pl.rtprog.java2flow;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import pl.rtprog.java2flow.interfaces.ClassJavaDoc;
import pl.rtprog.java2flow.interfaces.JavaDocProvider;

import java.nio.file.Path;

/**
 * Class for reading JavaDoc from source code using library <a href="http://javaparser.org/">JavaParser</a>
 *
 * @author Ryszard Trojnacki
 */
public class JavaDocProcessor implements JavaDocProvider {
    private final Path sourceRoot;

    public JavaDocProcessor(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    private static Class<?> getEnclosingClass(Class<?> clazz) {
        while(clazz.getEnclosingClass()!=null) clazz=clazz.getEnclosingClass();
        return clazz;
    }

    private static String getAuthor(Javadoc doc) {
        if(doc.getBlockTags()==null) return null;
        for(JavadocBlockTag tag: doc.getBlockTags()) {
            if("AUTHOR".equals(tag.getTagName())) {
                return tag.getContent().toText();
            }
        }
        return null;
    }

    private static String getClassName(Node n) {
        if(n instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration cn=(ClassOrInterfaceDeclaration)n;
            if(cn.getFullyQualifiedName().isPresent()) return cn.getFullyQualifiedName().get();
        } else if(n instanceof MethodDeclaration || n instanceof FieldDeclaration) {
            if(n.getParentNode().isPresent()) return getClassName(n.getParentNode().get());
        }
        return null;
    }

    @Override
    public ClassJavaDoc getComments(Class<?> clazz) {
        SourceRoot sr=new SourceRoot(sourceRoot==null?CodeGenerationUtils.mavenModuleRoot(clazz):sourceRoot);
        sr.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        Class<?> root=getEnclosingClass(clazz);
        final String fullyQualifiedName=clazz.getName().replace('$', '.');

        CompilationUnit cu=sr.parse(root.getPackage().getName(), root.getSimpleName()+".java");
        if(cu==null) return null;

        JavaDoc doc=new JavaDoc(clazz);

        for(Comment c: cu.getAllComments()) {
            if(!(c instanceof JavadocComment)) continue;
            JavadocComment jd=(JavadocComment)c;
            if(jd.getCommentedNode().isPresent()) {
                Node n=jd.getCommentedNode().get();

                String owner=getClassName(n);
                if(fullyQualifiedName.equals(owner)) {
                    Javadoc p = jd.parse();

                    if (n instanceof ClassOrInterfaceDeclaration) {
                        doc.setComment(p.getDescription().toText());
                        doc.setAuthor(getAuthor(p));
                    } else if (n instanceof FieldDeclaration) {
                        if(!Java2FlowUtils.isBlank(p.toText())) {
                            FieldDeclaration fd = (FieldDeclaration) n;
                            for (VariableDeclarator d : fd.getVariables()) {
                                doc.add(d.getNameAsString(), p.toText());
                            }
                        }
                    } else if (n instanceof MethodDeclaration) {
                        // TODO: Getter/Setter
                        MethodDeclaration md=(MethodDeclaration)n;
//                        System.out.println("Method: " + ((MethodDeclaration) n).getNameAsString());
                    }
                }
            }
        }
        return doc;
    }
}
