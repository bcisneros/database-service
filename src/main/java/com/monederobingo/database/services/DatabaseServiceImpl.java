package com.monederobingo.database.services;

import com.monederobingo.libs.common.context.ThreadContextService;
import com.monederobingo.database.api.interfaces.DatabaseService;
import com.monederobingo.database.common.db.adapter.DataBaseAdapter;
import com.monederobingo.database.common.db.adapter.DatabaseAdapterFactory;
import com.monederobingo.database.common.db.util.DbBuilder;
import com.monederobingo.database.model.InsertQuery;
import com.monederobingo.database.model.SelectQuery;
import com.monederobingo.database.model.ServiceResult;
import com.monederobingo.database.model.UpdateQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseServiceImpl implements DatabaseService
{
    private final ThreadContextService threadContextService;
    private final DatabaseAdapterFactory queryAgentFactory;

    @Autowired
    public DatabaseServiceImpl(ThreadContextService threadContextService, DatabaseAdapterFactory queryAgentFactory)
    {
        this.threadContextService = threadContextService;
        this.queryAgentFactory = queryAgentFactory;
    }

    @Override
    public ServiceResult<String> select(SelectQuery query) throws Exception
    {
        String object = getDatabaseAdapter().selectObject(new DbBuilder<String>()
        {
            @Override
            public String sql() throws SQLException
            {
                return query.getQuery();
            }

            @Override
            public Object[] values()
            {
                return new Object[0];
            }

            @Override
            public String build(ResultSet resultSet) throws Exception
            {
                int columnCount = resultSet.getMetaData().getColumnCount();
                JSONObject jsonObject = new JSONObject();
                buildObject(resultSet, columnCount, jsonObject);
                return jsonObject.toString();
            }
        });

        return new ServiceResult<>(true, "", object);
    }

    private void buildObject(ResultSet resultSet, int columnCount, JSONObject jsonObject) throws Exception
    {
        for (int i = 1; i <= columnCount; i++)
        {
            String columnName = resultSet.getMetaData().getColumnName(i);
            Object object = resultSet.getObject(columnName);
            if(object == null)
                object = "";
            jsonObject.put(columnName, object);
        }
    }

    @Override
    public ServiceResult<List<String>> selectList(SelectQuery query) throws Exception
    {
        List<String> object = getDatabaseAdapter().selectList(new DbBuilder<String>()
        {
            @Override
            public String sql() throws SQLException
            {
                return query.getQuery();
            }

            @Override
            public Object[] values()
            {
                return new Object[0];
            }

            @Override
            public String build(ResultSet resultSet) throws Exception
            {
                int columnCount = resultSet.getMetaData().getColumnCount();
                JSONObject jsonObject = new JSONObject();
                buildObject(resultSet, columnCount, jsonObject);
                return jsonObject.toString();
            }
        });

        return new ServiceResult<>(true, "", object);
    }

    private DataBaseAdapter getDatabaseAdapter() throws InterruptedException
    {
        return queryAgentFactory.getQueryAgent(threadContextService.getEnvironment());
    }

    @Override public ServiceResult<Long> insert(InsertQuery query) throws Exception
    {
        long newId = getDatabaseAdapter().executeInsert(query.getQuery(), query.getIdColumnName());
        return new ServiceResult<>(true, "", newId);
    }

    @Override public ServiceResult<Integer> update(UpdateQuery query) throws Exception
    {
        int updatedRows = getDatabaseAdapter().executeUpdate(query.getQuery());
        return new ServiceResult<>(true, "", updatedRows);
    }
}
