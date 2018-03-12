package com.firesoftitan.play.titansql;

import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Table {

    private List<DataType> types;
    private HashMap<String, DataType> typesByName;
    private String name;
    private HashMap<String, Object> tempRow;
    public Table(String myName)
    {
        this.types = new ArrayList<DataType>();
        this.typesByName = new HashMap<String, DataType>();
        this.name = myName;
    }
    public DataType getDataType(String name)
    {
        if (typesByName.containsKey(name))
        {
            return  typesByName.get(name);
        }
        return null;
    }
    public void addDataType(String name, DataTypeEnum type, Boolean NOT_NULL, Boolean UNIQUE, boolean PRIMARY_KEY)
    {
        DataType DT = new DataType(name, type,NOT_NULL, UNIQUE,PRIMARY_KEY);
        DT.setIndex(types.size());
        types.add(DT);
        typesByName.put(DT.getName(), DT);

    }
    public void search(final DataType type,final Object what, final CallbackResults callback)
    {
        new BukkitRunnable() {

            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;
                ResultData conver = new ResultData(type, what);
                Object whatconverted =  conver.get();
                try {
                    ps = TitanSQL.instance.getConnection().prepareStatement("SELECT * FROM " + name + " WHERE " + type.getName() + " = ? LIMIT 1");
                    type.getType().setPreparedStatement(ps, 1, whatconverted);
                    rs = ps.executeQuery();
                    List<HashMap<String, ResultData>> results = new ArrayList<HashMap<String, ResultData>>();
                    HashMap<String, ResultData> oneRow = new HashMap<String, ResultData>();
                    while (rs.next()) {
                        oneRow.clear();
                        for (DataType DT: types)
                        {
                            Object result = rs.getObject(DT.getName());
                            ResultData resultData = new ResultData(DT, result);
                            oneRow.put(DT.getName(), resultData);
                        }
                        results.add(oneRow);
                    }
                    callback.onResult(results);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(TitanSQL.instance);
    }
    public HashMap<String, ResultData> search(DataType type, Object what)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultData conver = new ResultData(type, what);
        what =  conver.get();
        try {
            ps = TitanSQL.instance.getConnection().prepareStatement("SELECT * FROM " + this.name + " WHERE " + type.getName() + " = ? LIMIT 1");
            type.getType().setPreparedStatement(ps, 1, what);
            rs = ps.executeQuery();
            HashMap<String, ResultData> oneRow = new HashMap<String, ResultData>();
            while (rs.next()) {
                oneRow.clear();
                for (DataType DT: types)
                {
                    Object result = rs.getObject(DT.getName());
                    ResultData resultData = new ResultData(DT, result);
                    oneRow.put(DT.getName(), resultData);
                }
                return oneRow;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(ps, rs);
        }
        return null;
    }
    private void startRow()
    {
        tempRow = new HashMap<String, Object>();
    }
    public boolean setDataField(String dataname, Object value)
    {
        if (!typesByName.containsKey(dataname))
        {
            return false;
        }
        DataType dt = typesByName.get(dataname);

        if (!dt.getType().checkObject(value))
        {
            return false;
        }
        if (tempRow == null)
        {
            startRow();
        }
        tempRow.put(dataname, value);
        return true;
    }
    public void insertData()
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String AoutputString = "REPLACE INTO " + this.name + " (";
        String BoutputString = ") VALUES(";
        List<String> order = new ArrayList<String>();
        for(DataType dt: types)
        {
            if (tempRow.containsKey(dt.getName())) {
                AoutputString = AoutputString + dt.getName() + ",";
                BoutputString = BoutputString + "?,";
                order.add(dt.getName());
            }
        }
        AoutputString = AoutputString.substring(0, AoutputString.length() - 1);
        BoutputString = BoutputString.substring(0, BoutputString.length() - 1);


        try {
            ps = TitanSQL.instance.getConnection().prepareStatement(AoutputString + BoutputString +")", Statement.RETURN_GENERATED_KEYS);

            int i = 1;
            for(String key: order)
            {
                DataTypeEnum DT = typesByName.get(key).getType();
                DT.setPreparedStatement(ps, i, tempRow.get(key));
                i++;
            }
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            close(ps);
        }

        //send code
        tempRow.clear();
        tempRow = null;
    }
    private void close(PreparedStatement ps) {
        close(ps, null);
    }
    private void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private String getCreateTable()
    {
        String queryCreateTable = "CREATE TABLE IF NOT EXISTS " + name + " (";
        for(DataType DT: types)
        {
            //this is wrong
            queryCreateTable = queryCreateTable + DT.getCreateTable() + ",";
        }
        queryCreateTable = queryCreateTable.substring(0, queryCreateTable.length() - 1);
        queryCreateTable = queryCreateTable + ")";
        return queryCreateTable;
    }
    public void createTable()
    {
        try {
            String queryCreateTable = getCreateTable();
            System.out.println(queryCreateTable);
            Statement s = TitanSQL.instance.getConnection().createStatement();
            s.executeUpdate(queryCreateTable);
            s.close();
        }
        catch (Exception e)
        {

        }
    }
}
