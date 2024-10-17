package pl.rtprog.java2flow.structs;

import java.util.Objects;

/**
 * Structure representing an import statement.
 *
 * @author Ryszard Trojnacki
 */
public class Import {
    /**
     * Typename in JavaScript code
     */
    private final String name;

    /**
     * Name of the import in the external code
     */
    private final String importName;

    public Import(String name, String importName) {
        this.name = name;
        this.importName = importName;
    }

    /**
     * Check if this is a default import.
     * @return true if this is a default import
     */
    public boolean isDefault() {
        return importName==null;
    }

    /**
     * Check if this is an import of the same name.
     * @return true if this is an import of the same name
     */
    public boolean isSameName() {
        return "".equals(importName) || name.equals(importName);
    }

    public String getName() {
        return name;
    }

    public String getImportName() {
        return importName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Import anImport = (Import) o;
        return Objects.equals(name, anImport.name) && Objects.equals(importName, anImport.importName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, importName);
    }

    @Override
    public String toString() {
        return "Import{" +
                "name='" + name + '\'' +
                ", importName='" + importName + '\'' +
                '}';
    }
}
