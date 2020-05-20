package com.yapis.handler;

import com.yapis.model.Memory;
import com.yapis.model.Variable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GrammarHandler {

    private final File outputFile;
    private final File functionFile;
    public String scope = "global";

    public void addVar(Variable variable) {
        Memory.vars.put(variable.getName(), variable);
    }

    public Variable getVarByName(String name) {
        return findVarByScope(Memory.vars.get(name));
    }

    private Variable findVarByScope(Collection<Variable> vars) {
        return vars.stream().filter(var -> var.getScope().equals(scope)).findFirst().orElseGet(null);
    }

    public GrammarHandler() {
        outputFile = new File("src/main/java/com/yapis/compile/Output.java");
        functionFile = new File("src/main/java/com/yapis/compile/Functions.java");
        prepareFiles();
    }

    public void writeVariable(String name, String scope) {
        var builder = new StringBuilder();
        Memory.vars.get(name).stream()
                .filter(var -> var.getScope().equals(scope) && !var.isAssignment())
                .peek(var -> var.setAssignment(true))
                .forEach(var -> builder.append("\n").append(var.getType()).append(" ").append(var.getName()).
                        append(" = ").append(var.getValue()).append(";"));
        checkFunc(scope, builder.toString());
    }

    public String getMathSign(String typeVar, String sign, String firstVal, String secondVal, String scope) {
        if (isVar(firstVal)) {
            checkVarNameByScope(scope, firstVal);
        }
        if (isVar(secondVal)) {
            checkVarNameByScope(scope, secondVal);
        }

        String math;
        if (typeVar.equals("String")) {
            math = makeMathStr(sign, firstVal, secondVal);
        } else {
            math = makeMathInt(sign) + " " + secondVal;
        }
        return math;
    }

    private boolean isVar(String val) {
        var pattern = Pattern.compile("^[a-zA-Z]*$");
        var matcher = pattern.matcher(val);
        return matcher.matches();
    }

    private String makeMathStr(String sign, String firstVal, String secondVal) {
        var mathSign = "";
        if (sign.equals("+")) {
            mathSign = "+ " + secondVal;
        } else if (sign.equals("-")) {
            mathSign = ".replaceAll(" + firstVal + "," + secondVal + ")";
        }
        return mathSign;
    }

    private String makeMathInt(String sign) {
        return sign;
    }

//    private List<Variable> findVarByScope(String scope) {
//        return Memory.vars.entries().stream()
//                .map(Map.Entry::getValue)
//                .filter(var -> var.getScope().equals(scope) && !var.isAssignment())
//                .collect(Collectors.toList());
//    }

    private boolean existNameInScope(String scope, String name) {
        return Memory.vars.get(name).stream()
                .anyMatch(var -> var.getScope().equals(scope) || var.getScope().equals("global"));
    }

    public void makeRelationHeader(String firstArg, String secondArg, String typeOfRel, String scope) {
        checkVarNameByScope(scope, firstArg);
        checkVarNameByScope(scope, secondArg);
        var builder = new StringBuilder();
        switch (typeOfRel) {
            case "if":
                builder.append("\nif (").append(firstArg).append(" == ").append(secondArg).append(") {\n");
                break;
            case "while":
                builder.append("\nwhile (").append(firstArg).append(" == ").append(secondArg).append(") {\n");
                break;
            case "switch":
                builder.append("\nswitch (").append(firstArg).append(") {");
                break;
            case "case":
                builder.append("\ncase ").append(firstArg).append(":\n");
                break;
            case "default":
                builder.append("\ndefault: \n");
                break;
            case "break":
                builder.append("\nbreak;");
                break;
        }
        checkFunc(scope, builder.toString());
    }

    public void makeRelationBody(String s, String scope) {
        checkFunc(scope, s);
    }

    public void closeRelation(String scope) {
        checkFunc(scope, "}");
    }

    public void makeLoopHeader(String scope) {
        checkFunc(scope, "\nfor (");
    }

    public void makeLoopHeaderParams(String firstArg, String sign, String secondArg, String thirdArg, String scope) {
        var reg = "[^a-z]";
        checkVarNameByScope(scope, firstArg.replaceAll(reg, ""));
        checkVarNameByScope(scope, secondArg.replaceAll(reg, ""));
        checkVarNameByScope(scope, thirdArg.replaceAll(reg, ""));

        checkFunc(scope, firstArg + " " + sign + " " + secondArg + "; " + thirdArg + ") {");
    }

    public void makeProcedureHeader(String header, String params, String scope) {
        createParamsCopyInScope(scope, findNameInParams(params));
        writeInFile("public static void " + header + " (" + params + ") {", functionFile);
    }

    public void closeProcedure(String scope) {
        cleanScope(scope);
        writeInFile("\n}", functionFile);
    }

    public void makeFuncHeader(String header, String params, String scope) {
        createParamsCopyInScope(scope, findNameInParams(params));
        writeInFile("public static " + header + " (" + params + ") {", functionFile);
    }

    public void makeBlockHeader() {
        writeInFile("{", outputFile);
    }

    public void closeBlock(String scope) {
        cleanScope(scope);
        writeInFile("\n}", outputFile);
    }

    public void closeFunc(String expression, String scope) {
        checkVarNameByScope(scope, expression);
        cleanScope(scope);
        writeInFile("return " + expression + ";\n}", functionFile);
    }

    public void makeFuncCall(String name, String params) {
        writeInFile(name + "(" + params + ");", outputFile);
    }

    private void writeInFile(String str, File file) {
        try {
            var writer = new FileWriter(file, true);
            writer.write(str + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareFiles() {
        cleanFile(functionFile);
        cleanFile(outputFile);
        writeInFile("package com.yapis.compile;\n\n"
                + "import org.w3c.dom.*;"
                + "import static com.yapis.compile.Functions.*;\n\n", outputFile);
        writeInFile("public class Output {", outputFile);
        writeInFile("\tpublic static void main(String[] args) {", outputFile);
        writeInFile("package com.yapis.compile;\n\npublic class Functions {", functionFile);
    }

    private void checkFunc(String scope, String s) {
        if ("global".equals(scope) || null == scope || scope.startsWith("block")) {
            writeInFile(s, outputFile);
        } else {
            writeInFile(s, functionFile);
        }
    }

    public void closeFiles() {
        writeInFile("\t}", outputFile);
        writeInFile("}", outputFile);
        writeInFile("}", functionFile);
    }

    private void cleanFile(File file) {
        try {
            var writer = new FileWriter(file);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkVarNameByScope(String scope, String name) {
        if (!name.isEmpty() && isVar(name) && !existNameInScope(scope, name)) {
            System.out.println("Error! Nonexisting name: " + name);
        }
    }

    public void print(String scope, String name) {
        checkVarNameByScope(scope, name);
        checkFunc(scope, "System.out.println(" + name + ");");
    }

    private void cleanScope(String scope) {
        var vars = Memory.vars.entries().stream()
                .filter(entry ->
                        entry.getValue().getScope().equals(scope)
                ).map(Map.Entry::getValue)
                .collect(Collectors.toList());
        vars.forEach(var -> Memory.vars.remove(var.getName(), var));
    }

    private void createParamsCopyInScope(String scope, List params) {
        var newVars = Memory.vars.entries().stream()
                .filter(entry ->
                        params.contains(entry.getValue().getName()))
                .map(Map.Entry::getValue)
                .map((Function<Variable, Variable>) Variable::new)
                .peek(var -> var.setScope(scope))
                .collect(Collectors.toList());
        newVars.forEach(newVar -> Memory.vars.put(newVar.getName(), newVar));

    }

    private List<String> findNameInParams(String params) {
        var result = new ArrayList<String>();
        Arrays.stream(params.split(",")).forEach(param -> result.add(param.split(" ")[1]));
        return result;
    }

}
