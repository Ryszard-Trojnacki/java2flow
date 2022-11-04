package pl.rtprog.java2flow;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import pl.rtprog.java2flow.interfaces.ClassJavaDoc;
import pl.rtprog.java2flow.interfaces.JavaDocProvider;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class for reading JavaDoc from source code using library <a href="http://javaparser.org/">JavaParser</a>
 *
 * @author Ryszard Trojnacki
 */
public class JavadocProcessor implements JavaDocProvider {
    private final Path sourceRoot;

    public JavadocProcessor(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    private static Class<?> getEnclosingClass(Class<?> clazz) {
        while(clazz.getEnclosingClass()!=null) clazz=clazz.getEnclosingClass();
        return clazz;
    }

    private static String getAuthor(com.github.javaparser.javadoc.Javadoc doc) {
        if(doc.getBlockTags()==null) return null;
        for(JavadocBlockTag tag: doc.getBlockTags()) {
            if(JavadocBlockTag.Type.AUTHOR==tag.getType()) {
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
        if(sourceRoot!=null && !Files.isDirectory(sourceRoot)) return null;
        SourceRoot sr=new SourceRoot(sourceRoot==null?CodeGenerationUtils.mavenModuleRoot(clazz):sourceRoot);
        sr.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        Class<?> root=getEnclosingClass(clazz);
        final String fullyQualifiedName=clazz.getName().replace('$', '.');

        CompilationUnit cu;
        try {
            cu = sr.parse(root.getPackage().getName(), root.getSimpleName() + ".java");
        }catch (ParseProblemException e) {
            return null;
        }
        if(cu==null) return null;

        Javadoc doc=new Javadoc(clazz);

        for(Comment c: cu.getAllComments()) {
            if(!(c instanceof JavadocComment)) continue;
            JavadocComment jd=(JavadocComment)c;
            if(jd.getCommentedNode().isPresent()) {
                Node n=jd.getCommentedNode().get();

                String owner=getClassName(n);
                if(fullyQualifiedName.equals(owner)) {
                    com.github.javaparser.javadoc.Javadoc p = jd.parse();

                    if (n instanceof ClassOrInterfaceDeclaration) {
                        doc.setComment(p.getDescription().toText());
                        doc.setAuthor(getAuthor(p));
                    } else if (n instanceof FieldDeclaration) {
                        if(!Java2FlowUtils.isBlank(p.toText())) {
                            FieldDeclaration fd = (FieldDeclaration) n;
                            for (VariableDeclarator d : fd.getVariables()) {
                                doc.add(d.getNameAsString(), p.toText(), true);
                            }
                        }
                    } else if (n instanceof MethodDeclaration) {
                        MethodDeclaration md=(MethodDeclaration)n;
                        if(md.getComment().isPresent() && md.getComment().get() instanceof JavadocComment) {
                            String comment=p.toText();
                            if(!Java2FlowUtils.isBlank(comment)) {
                                String name = md.getNameAsString();
                                if (md.getType().isVoidType()) {
                                    if (md.getParameters().size() == 1 && name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3))) {
                                        doc.add(Java2FlowUtils.uncapitalize(name.substring(2)), comment, false);
                                    }
                                } else if (md.getParameters().size() == 0) {
                                    if (name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2))) {
                                        doc.add(Java2FlowUtils.uncapitalize(name.substring(2)), comment, false);
                                    } else if (name.length() > 3 && name.startsWith("get") && Character.isUpperCase(name.charAt(3))) {
                                        doc.add(Java2FlowUtils.uncapitalize(name.substring(3)), comment, false);
                                    }
                                }
                            }
                        }
//                        System.out.println("Method: " + name+" -> "+ md.getType()+ " ("+md.getParameters()+")");
                    }
                }
            }
        }
        return doc;
    }
}
