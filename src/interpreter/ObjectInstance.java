package interpreter;

import java.util.*;

public class ObjectInstance {
    private String className;
    private Map<String, Object> fields = new HashMap<>();

    public ObjectInstance(String className, ClassDefinition classDef) {
        this.className = className;
        for (Map.Entry<String, String> entry : classDef.getFields().entrySet()) {
            switch (entry.getValue()) {
                case "Integer": fields.put(entry.getKey(), 0); break;
                case "String":  fields.put(entry.getKey(), ""); break;
                default:        fields.put(entry.getKey(), null); break;
            }
        }
    }

    public Object getField(String name) { return fields.get(name); }
    public void setField(String name, Object value) { fields.put(name, value); }
    public String getClassName() { return className; }
}