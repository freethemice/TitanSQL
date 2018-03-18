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
    public void search(final CallbackResults callback)
    {
        new BukkitRunnable() {

            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = TitanSQL.instance.getConnection().prepareCall("SELECT * FROM " + name);
                    rs = ps.executeQuery();
                    List<HashMap<String, ResultData>> results = new ArrayList<HashMap<String, ResultData>>();
                    HashMap<String, ResultData> oneRow;
                    while (rs.next()) {
                        oneRow = new HashMap<String, ResultData>();
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
    public void search(final String type,final Object what, final CallbackResults callback)
    {
        DataType searchfor = getDataType(type);
        search(searchfor, what, callback);
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
                    ps = TitanSQL.instance.getConnection().prepareStatement("SELECT * FROM " + name + " WHERE " + type.getName() + " = ?");
                    type.getType().setPreparedStatement(ps, 1, whatconverted);
                    rs = ps.executeQuery();
                    List<HashMap<String, ResultData>> results = new ArrayList<HashMap<String, ResultData>>();
                    HashMap<String, ResultData> oneRow;
                    while (rs.next()) {
                        oneRow  = new HashMap<String, ResultData>();
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

    private HashMap<String, ResultData> search(String name, Object what)
    {
        DataType searchfor = getDataType(name);
        return search(searchfor, what);
    }
    private HashMap<String, ResultData> search(DataType type, Object what)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultData conver = new ResultData(type, what);
        what =  conver.get();
        try {
            ps = TitanSQL.instance.getConnection().prepareStatement("SELECT * FROM " + this.name + " WHERE " + type.getName() + " = ? LIMIT 1");
            type.getType().setPreparedStatement(ps, 1, what);
            rs = ps.executeQuery();
            HashMap<String, ResultData> oneRow;
            while (rs.next()) {
                oneRow = new HashMap<String, ResultData>();
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
    //SELECT * FROM table_name LIMIT 100,10;
    public void search(int rowNumber, final CallbackResults callback)
    {
        new BukkitRunnable() {

            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = TitanSQL.instance.getConnection().prepareStatement("SELECT * FROM " + name + " LIMIT " + rowNumber + ",1");
                    rs = ps.executeQuery();
                    HashMap<String, ResultData> oneRow;
                    while (rs.next()) {
                        oneRow = new HashMap<String, ResultData>();
                        for (DataType DT: types)
                        {
                            Object result = rs.getObject(DT.getName());
                            ResultData resultData = new ResultData(DT, result);
                            oneRow.put(DT.getName(), resultData);
                        }
                        List<HashMap<String, ResultData>> onlyONe = new ArrayList<HashMap<String, ResultData>>();
                        onlyONe.add(oneRow);
                        callback.onResult(onlyONe);
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(TitanSQL.instance);


    }
    public boolean contains(String name, Object what)
    {
        DataType type = getDataType(name);
        return contains(type, what);
    }
    public boolean contains(DataType type, Object what)
    {
        HashMap<String, ResultData> tmp = search(type, what);
        if (tmp == null) {
            return false;
        }
        if (tmp.size() == 0)
        {
            return false;
        }
        return true;
    }
    //SELECT COUNT(*) FROM fooTable;
    public int size()
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = TitanSQL.instance.getConnection().prepareStatement("SELECT COUNT(*) FROM " + this.name);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(ps, rs);
        }
        return -1;
    }
    //DELETE FROM `lkr8bkxu_firesoftitan`.`fot_test` WHERE  `id`=12345 AND `name`='Farthead1' AND `something`=b'0' LIMIT 1;
    public void delete(String name, Object what)
    {
        DataType type = getDataType(name);
        delete(type, what);
    }
    public void delete(DataType type, final Object what)
    {
        new BukkitRunnable() {

            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultData conver = new ResultData(type, what);
                Object whatconverted = conver.get();
                try {
                    ps = TitanSQL.instance.getConnection().prepareStatement("DELETE FROM " + name + " WHERE " + type.getName() + " = ? LIMIT 1");
                    type.getType().setPreparedStatement(ps, 1, whatconverted);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps);
                }
            }
        }.runTaskAsynchronously(TitanSQL.instance);
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
        new BukkitRunnable() {

            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;

                String AoutputString = "REPLACE INTO " + name + " (";
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
        }.runTaskAsynchronously(TitanSQL.instance);

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
            Statement s = TitanSQL.instance.getConnection().createStatement();
            s.executeUpdate(queryCreateTable);
            s.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
