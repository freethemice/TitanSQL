package com.firesoftitan.play.titansql;


import java.sql.PreparedStatement;

public class PreparedStatementHolder {

    private PreparedStatement preparedStatement;
    private int count;
    public PreparedStatementHolder(PreparedStatement preparedStatement)
    {
        this.preparedStatement = preparedStatement;
        this.count = 0;
    }
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public int getCount() {
        return count;
    }
    public void addCount()
    {
        count++;
    }

}
