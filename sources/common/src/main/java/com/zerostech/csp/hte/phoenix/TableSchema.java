package com.zerostech.csp.hte.phoenix;

import java.io.Serializable;

/**
 * Created on 2019/3/2.
 *
 * @author 迹_Jason
 */
public class TableSchema implements Serializable {
    private String columnName;
    private String columnQualifier;
    private String type="";

    public TableSchema() {
    }

    public TableSchema(String columnName, String columnQualifier) {
        this.columnName = columnName;
        this.columnQualifier = columnQualifier;
    }

    public TableSchema(String columnName, String columnQualifier, String type) {
        this.columnName = columnName;
        this.columnQualifier = columnQualifier;
        this.type = type;
    }

    public String getColumnName() {
        return columnName;
    }

    public TableSchema setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getColumnQualifier() {
        return columnQualifier;
    }

    public TableSchema setColumnQualifier(String columnQualifier) {
        this.columnQualifier = columnQualifier;
        return this;
    }

    public String getType() {
        return type;
    }

    public TableSchema setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "columnName='" + columnName + '\'' +
                ", columnQualifier='" + columnQualifier + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
