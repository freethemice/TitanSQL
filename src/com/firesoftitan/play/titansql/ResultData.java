package com.firesoftitan.play.titansql;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ResultData {
    private DataType dataType;
    private Object result;
    public ResultData(DataType DataType, Object result)
    {
        this.dataType = DataType;
        this.result = result;
    }
    public DataType getEnum()
    {
        return dataType;
    }
    public String getName()
    {
        return dataType.getName();
    }
    public Byte getByte()
    {
        if (dataType.getType() == DataTypeEnum.BYTE)
        {
            if (result instanceof  Byte) {
                return (Byte) result;
            }
        }
        return null;
    }
    public Short getShort()
    {
        if (dataType.getType() == DataTypeEnum.SHORT)
        {
            if (result instanceof  Short) {
                return (Short) result;
            }
        }
        return null;
    }
    public Integer getInteger()
    {
        if (dataType.getType() == DataTypeEnum.INTEGER)
        {
            if (result instanceof  Integer) {
                return (Integer) result;
            }
        }
        return null;
    }
    public Long getLong()
    {
        if (dataType.getType() == DataTypeEnum.LONG)
        {
            if (result instanceof  Long) {
                return (Long) result;
            }
        }
        return null;
    }
    public Float getFloat()
    {
        if (dataType.getType() == DataTypeEnum.FLOAT)
        {
            if (result instanceof Float) {
                return (Float) result;
            }
        }
        return null;
    }
    public Double getDouble()
    {
        if (dataType.getType() == DataTypeEnum.DOUBLE)
        {
            if (result instanceof  Double) {
                return (Double) result;
            }
        }
        return null;
    }
    public String getString()
    {
        if (dataType.getType() == DataTypeEnum.STRING)
        {
            if (result instanceof String) {
                return (String) result;
            }
        }
        return null;
    }
    public Boolean getBoolean()
    {
        if (dataType.getType() == DataTypeEnum.BOOLEAN)
        {
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        }
        return null;
    }
    public ItemStack getItemStack()
    {
        if (dataType.getType() == DataTypeEnum.ITEMSTACK)
        {
            if (result instanceof String)
            {
                ItemStack nresults = TitanSQL.decodeItemStack((String)result);
                return nresults;
            }
        }
        return null;
    }
    public Location getLocation()
    {
        if (dataType.getType() == DataTypeEnum.LOCATION)
        {
            if (result instanceof String)
            {
                Location nresults = TitanSQL.decodeLocation((String)result);
                return nresults;
            }
        }
        return null;
    }
    public UUID getUUID()
    {
        if (dataType.getType() == DataTypeEnum.UUID)
        {
            if (result instanceof String) {
                return UUID.fromString((String)result);
            }
        }
        return null;
    }
    public List<Integer> getIntList()
    {
        if (dataType.getType() == DataTypeEnum.INTLIST)
        {
            if (result instanceof String)
            {
                List<Integer> nresults = TitanSQL.decodeIntList((String)result);
                return nresults;
            }
        }
        return null;
    }
    public List<String> getIntString()
    {
        if (dataType.getType() == DataTypeEnum.INTLIST)
        {
            if (result instanceof String)
            {
                List<String> nresults = TitanSQL.decodeStringList((String)result);
                return nresults;
            }
        }
        return null;
    }
    public List<ItemStack> getItemList()
    {
        if (dataType.getType() == DataTypeEnum.ITEMLIST)
        {
            if (result instanceof String)
            {
                List<ItemStack> nresults = TitanSQL.decodeItemList((String)result);
                return nresults;
            }
        }
        return null;
    }
    public Object get()
    {
        if (dataType.getType() == DataTypeEnum.BYTE)
        {
            if (result instanceof  Byte) {
                return (Byte) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.SHORT)
        {
            if (result instanceof  Short) {
                return (Short) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.INTEGER)
        {
            if (result instanceof  Integer) {
                return (Integer) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.LONG)
        {
            if (result instanceof  Long) {
                return (Long) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.FLOAT)
        {
            if (result instanceof Float) {
                return (Float) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.DOUBLE)
        {
            if (result instanceof  Double) {
                return (Double) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.STRING)
        {
            if (result instanceof String) {
                return (String) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.BOOLEAN)
        {
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        }
        if (dataType.getType() == DataTypeEnum.ITEMSTACK)
        {
            if (result instanceof String)
            {
                ItemStack nresults = TitanSQL.decodeItemStack((String)result);
                return nresults;
            }
        }
        if (dataType.getType()  == DataTypeEnum.LOCATION)
        {
            if (result instanceof String)
            {
                Location nresults = TitanSQL.decodeLocation((String)result);
                return nresults;
            }
        }
        if (dataType.getType() == DataTypeEnum.UUID)
        {
            if (result instanceof String) {
                return UUID.fromString((String)result);
            }
        }
        if (dataType.getType() == DataTypeEnum.INTLIST)
        {
            if (result instanceof String)
            {
                List<Integer> nresults = TitanSQL.decodeIntList((String)result);
                return nresults;
            }
        }
        if (dataType.getType() == DataTypeEnum.INTLIST)
        {
            if (result instanceof String)
            {
                List<String> nresults = TitanSQL.decodeStringList((String)result);
                return nresults;
            }
        }
        if (dataType.getType() == DataTypeEnum.ITEMLIST)
        {
            if (result instanceof String)
            {
                List<ItemStack> nresults = TitanSQL.decodeItemList((String)result);
                return nresults;
            }
        }
        return null;
    }
}
