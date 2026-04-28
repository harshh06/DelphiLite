package compiler;

import grammar.*;
import org.antlr.v4.runtime.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * CLI: parses a .pas file and writes LLVM IR (.ll).
 * Usage: {@code java -cp ... compiler.DelphiCompiler [-o out.ll] <file.pas>}
 */
public final class DelphiCompiler {

    public static void main(String[] args) throws Exception {
        String outPath = "output.ll";
        String inPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("-o".equals(args[i]) && i + 1 < args.length) {
                outPath = args[++i];
            } else if (!args[i].startsWith("-")) {
                inPath = args[i];
            }
        }
        if (inPath == null) {
            System.err.println("Usage: DelphiCompiler [-o output.ll] <file.pas>");
            System.exit(1);
        }

        CharStream input = CharStreams.fromFileName(inPath);
        delphiLexer lexer = new delphiLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphiParser parser = new delphiParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException("parse error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        delphiParser.ProgramContext tree = parser.program();
        DelphiLLVMGenerator gen = new DelphiLLVMGenerator();
        String ll = gen.generate(tree);
        Path p = Paths.get(outPath);
        Path parent = p.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(p, ll, StandardCharsets.UTF_8);
        System.err.println("Wrote " + outPath);
    }
}
