package pl.rtprog.java2flow;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;

/**
 * Class for reading JavaDoc from source code using library <a href="http://javaparser.org/">JavaParser</a>
 *
 * @author Ryszard Trojnacki
 */
public class JavaDocProcessor {
    public void processFile(File sourceRoot, Class<?> clazz) {
        SourceRoot sr=new SourceRoot(CodeGenerationUtils.mavenModuleRoot(clazz));
        CompilationUnit cu=sr.parse(clazz.getPackage().getName(), clazz.getSimpleName()+".java");
//        new ModifierVisitor<>();
        cu.getAllComments();
    }
}
