package com.yapis.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Variable<T> {

    private String name;
    private String type;
    private T value;
    private String scope;
    private boolean assignment = false;

    public Variable(String name, String type, String scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    public Variable(Variable<T> variable) {
        this(variable.name, variable.type, variable.value, variable.scope);
    }

    public Variable(String name, String type, T value, String scope) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.scope = scope;

    }

}
