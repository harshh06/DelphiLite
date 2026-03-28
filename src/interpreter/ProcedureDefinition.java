package interpreter;

import grammar.delphiParser;

public class ProcedureDefinition {
    private String name;
    private delphiParser.FormalParameterListContext params;
    private delphiParser.VariableDeclarationSectionContext locals;
    private delphiParser.BlockContext body;
    private boolean isFunction;

    public ProcedureDefinition(String name, delphiParser.FormalParameterListContext params,
                               delphiParser.VariableDeclarationSectionContext locals,
                               delphiParser.BlockContext body, boolean isFunction) {
        this.name = name;
        this.params = params;
        this.locals = locals;
        this.body = body;
        this.isFunction = isFunction;
    }

    public String getName() { return name; }
    public delphiParser.FormalParameterListContext getParams() { return params; }
    public delphiParser.VariableDeclarationSectionContext getLocals() { return locals; }
    public delphiParser.BlockContext getBody() { return body; }
    public boolean isFunction() { return isFunction; }
}
