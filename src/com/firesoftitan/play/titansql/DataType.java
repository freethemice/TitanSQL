package com.firesoftitan.play.titansql;

public class DataType {
    private DataTypeEnum type;
    private Boolean NOT_NULL;
    private Boolean UNIQUE;
    private boolean PRIMARY_KEY;
    private String name;
    private Integer index;
    public DataType(String name, DataTypeEnum type, Boolean NOT_NULL, Boolean UNIQUE, boolean PRIMARY_KEY)
    {
        this.name = name;
        this.type = type;
        this.NOT_NULL = NOT_NULL;
        this.UNIQUE = UNIQUE;
        this.PRIMARY_KEY = PRIMARY_KEY;
    }
    public String getSQLTypes()
    {
        switch (type.getName())
        {
            case "byte":
                return"TINYINT";
            case "short":
                return"SMALLINT";
            case "integer":
                return"INT";
            case "long":
                return"BIGINT";
            case "float":
                return"FLOAT";
            case "double":
                return"DOUBLE";
            case "string":
                return"LONGTEXT";
            case "chararray":
                return"VARCHAR(100)";
            case "boolean":
                return"BIT";
            case "itemstack":
                return"LONGTEXT";
            case "location":
                return"LONGTEXT";
            case "uuid":
                return"LONGTEXT";
            case "intlist":
                return"LONGTEXT";
            case "stringlist":
                return"LONGTEXT";
            case "itemlist":
                return"LONGTEXT";

        }
        return null;
    }
    public int getIndex()
    {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public Boolean get_NOT_NULL() {
        return NOT_NULL;
    }

    public Boolean get_UNIQUE() {
        return UNIQUE;
    }


    public boolean is_PRIMARY_KEY() {
        return PRIMARY_KEY;
    }

    public DataTypeEnum getType() {
        return type;
    }
    public String getCreateTable()
    {
        String out = name + " " + getSQLTypes();

        if (NOT_NULL)
        {
            out = out + " NOT NULL";
        }
        if (UNIQUE)
        {
            out = out + " UNIQUE";
        }
        if (PRIMARY_KEY)
        {
            out = out + " PRIMARY KEY";
        }
        return out;
    }
}
