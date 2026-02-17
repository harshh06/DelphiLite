package interpreter;

import java.util.*;

public class Environment {
    private Map<String, Object> variables = new HashMap<>();
    private Environment parent;

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        if (variables.containsKey(name)) return variables.get(name);
        if (parent != null) return parent.get(name);
        throw new RuntimeException("Undefined variable: " + name);
    }

    public void set(String name, Object value) {
        if (variables.containsKey(name)) { variables.put(name, value); return; }
        if (parent != null) { parent.set(name, value); return; }
        throw new RuntimeException("Undefined variable: " + name);
    }

    public boolean exists(String name) {
        if (variables.containsKey(name)) return true;
        if (parent != null) return parent.exists(name);
        return false;
    }
}