package interpreter;

import java.util.*;

public class InterfaceDefinition {
    private String name;
    private List<String> methodNames = new ArrayList<>();

    public InterfaceDefinition(String name) { this.name = name; }

    public String getName() { return name; }
    public void addMethod(String methodName) { methodNames.add(methodName); }
    public List<String> getMethodNames() { return methodNames; }

    public void validate(ClassDefinition classDef) {
        for (String method : methodNames) {
            if (!classDef.hasMethod(method)) {
                throw new RuntimeException(
                    "Class '" + classDef.getName() + "' must implement method '" +
                    method + "' from interface '" + name + "'"
                );
            }
        }
    }
}