package interpreter;

import grammar.delphiParser;
import java.util.*;

public class ClassDefinition {
    private String name;
    private Map<String, String> fields = new LinkedHashMap<>();
    private Map<String, String> fieldVisibility = new HashMap<>();
    private Map<String, String> methodVisibility = new HashMap<>();
    private String parentClassName;
    private List<String> implementedInterfaces = new ArrayList<>();

        public String getParentClassName() { return parentClassName; }
    public void setParentClassName(String parentClassName) { this.parentClassName = parentClassName; }

    public List<String> getImplementedInterfaces() { return implementedInterfaces; }
    public void addImplementedInterface(String iface) { implementedInterfaces.add(iface); }

    public boolean hasField(String fieldName) { return fields.containsKey(fieldName); }

    public boolean hasMethod(String methodName) { return methodVisibility.containsKey(methodName); }

    public void inheritFrom(ClassDefinition parent) {
        for (Map.Entry<String, String> entry : parent.fields.entrySet()) {
            if (!fields.containsKey(entry.getKey())) {
                fields.put(entry.getKey(), entry.getValue());
                fieldVisibility.put(entry.getKey(), parent.fieldVisibility.get(entry.getKey()));
            }
        }
        for (Map.Entry<String, String> entry : parent.methodVisibility.entrySet()) {
            if (!methodVisibility.containsKey(entry.getKey())) {
                methodVisibility.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
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