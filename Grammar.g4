grammar Grammar;

options {
    language = Java;
    }

@header {
    import com.yapis.handler.GrammarHandler;
    import com.yapis.model.*;
    }

@members {
    private GrammarHandler handler = new GrammarHandler();
    }

program: 'program' {handler.prepareFiles();} WS* 'begin' WS*
            (variable | expression SEMICOLON | statement | function | procedure | function_call | print)* WS*
        'end' {handler.closeFiles();};

variable: WS* type WS+ name {
        handler.addVar(new Variable($name.text, $type.text, handler.scope));
    } ' = '? (value {
        handler.getVarByName($name.text).setValue($value.val);
    } | expression {
        handler.getVarByName($name.text).setValue($expression.val);
    } | function_call {
        handler.getVarByName($name.text).setValue($function_call.val);
    })? (', ' name {
        handler.addVar(new Variable($name.text, $type.text, handler.scope));
    } ' = '? (value {
        handler.getVarByName($name.text).setValue($value.val);
    } | expression {
        handler.getVarByName($name.text).setValue($expression.val);
    } | function_call {
        handler.getVarByName($name.text).setValue($function_call.val);
    })?)* WS* SEMICOLON? WS*{
        handler.writeVariable($name.text, handler.scope);
    };

type: ('int' | 'String'
//| 'char' | 'String[]'
);
name: STRING;

value returns[String val, String typeOfVal]: ('"'WS* STRING WS*'"' {
        $val = "\"" + $STRING.text + "\""; $typeOfVal = "String";
    } | INT {
        $val = $INT.text; $typeOfVal = "Int";
    } | STRING {
        $val = $STRING.text; $typeOfVal = "Var";
    });

//-------------------

expression returns[String val]:  WS* s1=value WS* {$val = $s1.text;}
    (WS* MATH_SYMB WS* (s2=value)* {if ($s2.text!=null) {
                                        if ($s1.typeOfVal.equals("String")) {
                                            $val = $val + handler.getMathSign("String", $MATH_SYMB.text, $val,
                                            $s2.text, handler.scope);
                                        } else {
                                            $val = $val + " " + handler.getMathSign("Int", $MATH_SYMB.text, $val,
                                            $s2.text, handler.scope);
                                        }
                                    } else {$val = $val + " " + $MATH_SYMB.text;
    }})*;

relation returns[String strValF, String strValS, String typeOfValue, String sign]: WS* v1=value WS*
                (('==' {$sign="==";} | '!=' {$sign="!=";} | '<' {$sign="<";} | '<=' {$sign="<=";} | '>=' {$sign=">=";}
                | '>' {$sign=">";}) WS*	v2=value WS* { $strValF = $v1.val; $strValS = $v2.val;})*;

//-------------------------------

statement: ifstatement | whilestatement | forstatement | switchstatement | block;

ifstatement: 'if' WS* OPEN_BRACKET WS*
     relation {
        handler.makeRelationHeader($relation.strValF, $relation.strValS, "if", handler.scope);
     } WS* CLOSE_BRACKET WS* CURLY_OPEN_BRACKET
     (WS* (variable | expression SEMICOLON WS* | function_call | statement | print) {
        handler.makeRelationBody($expression.val + ";", handler.scope);
     })* WS* CURLY_CLOSE_BRACKET WS* ('else' WS* CURLY_OPEN_BRACKET) {
        handler.makeRelationBody("} else {", handler.scope);
     }
     ( WS* (variable | expression SEMICOLON WS* | function_call | statement | print) {
        handler.makeRelationBody($expression.val + ";", handler.scope);
     })* WS* CURLY_CLOSE_BRACKET WS*{
        handler.closeRelation(handler.scope);
     };

whilestatement: 'while' WS* OPEN_BRACKET WS* relation {
        handler.makeRelationHeader($relation.strValF, $relation.strValS, "while", handler.scope);
    } CLOSE_BRACKET WS* CURLY_OPEN_BRACKET (WS* (variable | expression SEMICOLON WS* {
        handler.makeRelationBody($expression.val + ";", handler.scope);
    }| function_call | statement | print))* WS* CURLY_CLOSE_BRACKET WS* {
        handler.closeRelation(handler.scope);
    };

forstatement: 'for' WS* OPEN_BRACKET {
        handler.makeLoopHeader(handler.scope);
    } WS* variable WS* relation WS* SEMICOLON WS* expression WS* CLOSE_BRACKET WS* CURLY_OPEN_BRACKET {
        handler.makeLoopHeaderParams($relation.strValF, $relation.sign, $relation.strValS, $expression.val,
                                    handler.scope);
    } (WS* variable | expression SEMICOLON WS* {
        handler.makeRelationBody($expression.val + ";", handler.scope);
    } WS* | function_call | statement | print)* WS* CURLY_CLOSE_BRACKET WS* {
        handler.closeRelation(handler.scope);
    };

switchstatement: 'switch' WS* OPEN_BRACKET expression CLOSE_BRACKET WS* {
                handler.makeRelationHeader($expression.val, $expression.val, "switch", handler.scope);
            } CURLY_OPEN_BRACKET WS* (WS* 'case' WS* value WS* {
                handler.makeRelationHeader($value.val, $value.val, "case", handler.scope);
            } COLON WS* (WS* variable | expression SEMICOLON WS* | function_call | statement | print)* WS*
                'break' SEMICOLON {
                handler.makeRelationHeader($value.val, $value.val, "endcase", handler.scope);
            })+ WS* (WS* 'default' WS* {
                handler.makeRelationHeader($value.val, $value.val, "default", handler.scope);
            } COLON WS* (WS* variable | expression SEMICOLON WS* | function_call | statement | print)* WS*
                'break' SEMICOLON {
                handler.makeRelationHeader($value.val, $value.val, "endcase", handler.scope);
            })? WS* CURLY_CLOSE_BRACKET WS* {
                handler.closeRelation(handler.scope);
            };

block: WS* 'block' WS* name WS* CURLY_OPEN_BRACKET WS* {
            handler.scope = "block" + $name.text;
        } {
            handler.makeBlockHeader();
        } (WS* variable | expression SEMICOLON WS* {
            handler.makeRelationBody($expression.val + ";", handler.scope);
        } WS* | function_call | statement | print)* WS* CURLY_CLOSE_BRACKET WS* {
            handler.closeBlock(handler.scope);
        } {
            handler.scope = "global";
        };

//-------------

procedure: 'procedure' WS+ name {
                handler.scope = "block" + $name.text;
            } WS* OPEN_BRACKET WS* parameters? WS* CLOSE_BRACKET WS* CURLY_OPEN_BRACKET WS*{
                handler.makeProcedureHeader($name.text, $parameters.val, handler.scope);
            } WS* (variable | expression SEMICOLON WS* | function_call | statement | print)* WS* CURLY_CLOSE_BRACKET {
    		    handler.closeProcedure(handler.scope);
    		} {
    		    handler.scope = "global";
    		} WS*;

function returns[String val]: 'function' WS+ type WS+ name {
                handler.scope = $name.text;
            } WS* OPEN_BRACKET WS* parameters? CLOSE_BRACKET WS* CURLY_OPEN_BRACKET {
                handler.makeFuncHeader($type.text + " " + $name.text, $parameters.val, handler.scope);
            } WS* (variable | expression WS* | function_call | statement | print)* WS*
            'return' WS+ expression WS* SEMICOLON WS* CURLY_CLOSE_BRACKET {
                handler.closeFunc($expression.val, handler.scope);
            } {
                handler.scope = "global";
            };

function_call returns[String val] : WS* name WS* OPEN_BRACKET WS* parameters? WS* CLOSE_BRACKET SEMICOLON WS* {
                handler.makeFuncCall($name.text, $parameters.val);
                $val=$name.text + " (" +  $parameters.val + ")";
            };

parameters returns[String val]:	p1=parameter {$val = $p1.val;} WS* (',' WS* p2=parameter {$val = $val + ","+ $p2.val;}
            WS*)*;

parameter returns[String val]: (type WS* {$val = $type.text + " ";})* name {
                                if($val ==null) {
    	                            $val = $name.text;
    	                        } else $val = $val + $name.text;
    	  };

print: 'print' WS* OPEN_BRACKET WS* expression WS* CLOSE_BRACKET SEMICOLON WS*{
            handler.print(handler.scope, $expression.val);
        };

OPEN_BRACKET: '(';
CLOSE_BRACKET: ')';
CURLY_OPEN_BRACKET: '{';
CURLY_CLOSE_BRACKET: '}';

COLON: ':';
SEMICOLON: ';';

MATH_SYMB: ('+' | '-' | '/' | '*' | '=' | '+=' | '-=' | '++' | '--');
STRING: ([a-z] | [A-Z])+;
INT: [0-9]+;
WORD: 'a'..'z'+;
WS: (' ' | '\t' | '\n' | '\r')*;
SYMB: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;