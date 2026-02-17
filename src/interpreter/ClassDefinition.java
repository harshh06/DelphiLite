package interpreter;

import grammar.delphiParser;
import java.util.*;

public class ClassDefinition {
    private String name;
    private Map<String, String> fields = new LinkedHashMap<>();
    private Map<String, String> fieldVisibility = new HashMap<>();
    private Map<String, String> methodVisibility = new HashMap<>();

    public ClassDefinition(String name) { this.name = name; }

    public String getName() { return name; }

    public Map<String, String> getFields() { return fields; }

    public void addField(String fieldName, String typeName, String visibility) {
        fields.put(fieldName, typeName);
        fieldVisibility.put(fieldName, visibility);
    }

    public String getFieldVisibility(String fieldName) {
        return fieldVisibility.getOrDefault(fieldName, "public");
    }

    public void addMethodVisibility(String methodName, String visibility) {
        methodVisibility.put(methodName, visibility);
    }

    public String getMethodVisibility(String methodName) {
        return methodVisibility.getOrDefault(methodName, "public");
    }
}