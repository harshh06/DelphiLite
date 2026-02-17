

# DelphiLite Interpreter

**Current Status:** Phase 0 Complete (Setup & Basic Grammar Verification)

This project is a Delphi/Pascal interpreter built using Java and ANTLR4.

## 📋 Prerequisites

* **Java 11+** (Required by ANTLR 4.13.2)
    * *Check version:* `java -version`
* **ANTLR 4.13.2 Complete JAR**
    * Included in the `lib/` folder (or download from [antlr.org](https://www.antlr.org/download.html)).

## 📂 Project Structure

We follow a strict directory structure to separate source grammar from generated code.

```text
DelphiLite/
├── grammar/               # Source grammar files (.g4)
│   └── delphi.g4
├── src/                   # Source code & generated parser
│   └── grammar/           # AUTO-GENERATED: Parser/Lexer files (do not edit)
├── tests/                 # Pascal test files
│   └── test0.pas
├── lib/                   # Libraries
│   └── antlr-4.13.2-complete.jar
└── README.md

```

## ⚙️ Setup (First Time)

1. **Clone the repository.**
2. **Ensure the ANTLR JAR is present:**
* Verify that `lib/antlr-4.13.2-complete.jar` exists.
* If not, download it and place it there manually.



## 🚀 Build & Run Workflow

We use terminal commands to generate the parser, compile it, and test it. Run these commands from the project root (`DelphiLite/`).

### Step 1: Generate Parser (ANTLR)

This reads `grammar/delphi.g4` and generates the Java parser code into `src/grammar/`.

```bash
java -jar lib/antlr-4.13.2-complete.jar -package grammar -visitor grammar/delphi.g4 -o src

```

### Step 2: Compile Java Code

Compiles the generated parser and lexer.

```bash
javac -cp lib/antlr-4.13.2-complete.jar src/grammar/*.java

```

### Step 3: Run the TestRig (Visualization)

Parses the test file and opens a GUI window showing the syntax tree.

```bash
java -cp "src:lib/antlr-4.13.2-complete.jar" org.antlr.v4.gui.TestRig grammar.delphi program -gui tests/test0.pas

```

*Note: If on Windows, replace the colon `:` in the classpath with a semicolon `;`.*

---

## 🧪 Current Grammar Capabilities

As of Phase 0, the grammar (`delphi.g4`) is minimal. It supports:

* `program Name;` structure.
* `begin ... end.` blocks.
* `writeln('string');` statements.

**Example Test File (`tests/test0.pas`):**

```pascal
program Hello;
begin
  writeln('Hello DelphiLite');
end.
```
