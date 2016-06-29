package etl;

import com.dameng.etl.api.model.ColumnBean;
import com.dameng.etl.api.model.DatabaseDataSourceParamBean;
import com.dameng.etl.api.model.TransformationBean;
import com.dameng.etl.driver.*;
import com.dameng.etl.driver.ds.DatabaseDataSource;
import com.dameng.etl.driver.ds.SQLQueryDataSet;
import com.dameng.etl.driver.ds.TableDataSet;
import com.dameng.etl.driver.flow.CommonNode;
import com.dameng.etl.driver.flow.transformation.SQLQuerySourceNode;
import com.dameng.etl.driver.flow.transformation.TableDestinationNode;
import com.dameng.etl.driver.flow.transformation.TableSourceNode;

import java.util.Map;


/**
 * Created by shuxinwei on 2016/6/14.
 */
public class ETLUtil {
    private Connection connection;// 链接

    private Project project;// 工程

    private Transformation transformation;// 转换

    private DatabaseDataSource sourceDataSource;// 读取数据源

    private TableSourceNode tableSourceNode; // 读取表节点

    private SQLQuerySourceNode sqlQuerySourceNode; // 读取sql节点

    private TableDestinationNode tableDestinationNode; // 装载表节点

    /**
     * 连接ETL
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @return
     */
    public boolean conn(String ip, int port, String username, String password)
    {
        boolean flag = true;
        try
        {
            connection = new Connection(ip, port, username, password);
            connection.open();
            Thread.sleep(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 关闭ETL
     */
    public void close()
    {
        if (!connection.isClosed())
        {
            connection.close();
        }
    }

    /**
     * 创建数据库数据源
     *
     * @param datasourceName
     * @param dataBase 数据库类型，如Oracle 11的数据库名为Oracle11
     * @param dbHost
     * @param dbPort
     * @param dbName
     * @param dbUserName
     * @param dbPassword
     */
    public void createDatabaseDataSource(String datasourceName, String dataBase, String dbHost, String dbPort,
                                         String dbName, String dbUserName, String dbPassword)
    {
        sourceDataSource = connection.findDatabaseDataSource(datasourceName);
        if (sourceDataSource == null)
        {
            // 获取数据库数据源的默认连接参数对象,参数dbType的可选值见DMETL客户端的新建数据库数据源对话框
            DatabaseDataSourceParamBean dataBaseDataSourceBean = connection.newDefaultDatabaseDataSourceParam(dataBase);
            dataBaseDataSourceBean.setDbHost(dbHost);
            dataBaseDataSourceBean.setDbPort(dbPort);
            dataBaseDataSourceBean.setDbName(dbName); // 服务名
            dataBaseDataSourceBean.setDbUserName(dbUserName);
            dataBaseDataSourceBean.setDbPassword(dbPassword);
            // 在DMETL中创建数据源
            sourceDataSource =
                    connection.createDatabaseDataSource(datasourceName, "此数据源由API程序自动创建", dataBaseDataSourceBean);
        }
    }

    /**
     * 添加表数据集
     *
     * @param schema
     * @param table
     */
    public TableDataSet[] addTable(String schema, String table)
    {
        TableDataSet[] tableDataSets = sourceDataSource.getTableDataSets(null, schema, table);
        if (tableDataSets.length == 0)
        {
            tableDataSets = sourceDataSource.addTable(null, schema, table);
        }
        return tableDataSets;
    }

    /**
     * 查找创建工程
     */
    public void findProject(String projectName)
    {
        if (connection != null && !connection.isClosed())
        {
            project = connection.findProject(projectName);
            if (project == null)
            {
                project = connection.createProject(projectName, "此工程由应用程序使用API创建");
            }
        }
    }

    /**
     * 设置工程变量
     */
    public void setVariables(Map<String, String> variables)
    {
        if (variables != null)
        {
            for (String key : variables.keySet())
            {
                Variable v = project.getVariableRootDir().findVariable(key);
                if (v != null)
                {
                    v.modify(key, variables.get(key), null);
                }
                else
                {
                    project.getVariableRootDir().createVariable(key, variables.get(key), null);
                }
            }
        }
    }

    /**
     * 查找创建转换
     */
    public void findTransformation(String transformationName)
    {
        if (project != null)
        {
            // 获取转换根目录
            TransformationDir transformationDir = project.getTransformationRootDir();
            transformation = transformationDir.findTransformation(transformationName);
            // 如果不存在则创建转换
            if (transformation == null)
            {
                TransformationBean transformationBean = new TransformationBean();
                transformationBean.setName(transformationName);
                transformationBean.setDescription("此转换由应用程序使用API创建");
                transformationBean.setEnabled(true);
                transformation = transformationDir.createTransformation(transformationBean);
            }
        }
    }

    /**
     * 创建读取节点
     */
    public void createSourceNode(String schema, String table)
    {
        TableDataSet[] tableDataSets = sourceDataSource.getTableDataSets(null, schema, table);
        if (tableDataSets.length == 0)
        {
            tableDataSets = sourceDataSource.addTable(null, schema, table);
        }
        CommonNode[] nodes = transformation.getBeginNodes();
        for (CommonNode node : nodes)
        {
            if (node.getName().equalsIgnoreCase("读取表_" + table))
            {
                tableSourceNode = (TableSourceNode)node;
                return;
            }
        }
        // 新建表数据节点
        tableSourceNode = new TableSourceNode(transformation);
        tableSourceNode.setName("读取表_" + table);
        /* 以下1项必须设置，其他按需设置 */
        tableSourceNode.setDataSetId(tableDataSets[0].getDataSetBean().getId());
        // 设置输出列
        ColumnBean[] tableColumnBeans = tableDataSets[0].getDataSetBean().getColumns();
        String[] tableColumnNames = new String[tableColumnBeans.length];
        for (int i = 0; i < tableColumnNames.length; i++)
        {
            tableColumnNames[i] = tableColumnBeans[i].getName();
        }
        tableSourceNode.setSelectedColumns(tableColumnNames);
        // 添加节点
        transformation.addNode(tableSourceNode);
    }

    /**
     * 创建读取节点
     */
    public void createSqlSourceNode(String name, String sql)
    {
        SQLQueryDataSet[] sqlQueryDataSets = sourceDataSource.getSQLQueryDataSets(null, name);
        SQLQueryDataSet sqlQueryDataSet = null;
        if (sqlQueryDataSets == null || sqlQueryDataSets.length > 0)
        {
            sqlQueryDataSets[0].delete();
        }
        // 创建新的sql查询
        sqlQueryDataSet = (SQLQueryDataSet)sourceDataSource.addSQLQuery(name, null, sql);

        // 创建SQL查询数据源节点
        sqlQuerySourceNode = new SQLQuerySourceNode(transformation);
        sqlQuerySourceNode.setDataSetId(sqlQueryDataSet.getDataSetBean().getId());

        // 设置需要读取的列
        ColumnBean[] sqlColumnBeans = sqlQueryDataSet.getDataSetBean().getColumns();
        String[] sqlColumnNames = new String[sqlColumnBeans.length];
        for (int i = 0; i < sqlColumnNames.length; i++)
        {
            sqlColumnNames[i] = sqlColumnBeans[i].getName();
        }
        sqlQuerySourceNode.setSelectedColumns(sqlColumnNames);

        // 添加节点
        transformation.addNode(sqlQuerySourceNode);
    }

    /**
     * 创建装载节点
     */
    public void createDestinationNode(String schema, String table)
    {
        TableDataSet[] tableDataSets = sourceDataSource.getTableDataSets(null, schema, table);
        if (tableDataSets.length == 0)
        {
            tableDataSets = sourceDataSource.addTable(null, schema, table);
        }
        CommonNode[] nodes = transformation.getEndNodes();
        for (CommonNode node : nodes)
        {
            if (node.getName().equalsIgnoreCase("装载表_" + table))
            {
                tableDestinationNode = (TableDestinationNode)node;
                return;
            }
        }
        // 新建表数据节点
        tableDestinationNode = new TableDestinationNode(transformation);
        tableDestinationNode.setName("装载表_" + table);
        /* 以下1项必须设置，其他按需设置 */
        tableDestinationNode.setDataSetId(tableDataSets[0].getDataSetBean().getId());
        // 设置输出列
        ColumnBean[] tableColumnBeans = tableDataSets[0].getDataSetBean().getColumns();
        String[] tableColumnNames = new String[tableColumnBeans.length];
        for (int i = 0; i < tableColumnNames.length; i++)
        {
            tableColumnNames[i] = tableColumnBeans[i].getName();
            // 查找列
            if (i == 0)
            {
                String[] lookupCoumns = new String[1];
                lookupCoumns[0] = new String(tableColumnBeans[i].getName());
                tableDestinationNode.addLookupColumns(lookupCoumns);
                //tableDestinationNode.setInsertUpdate(true);
            }
        }
        tableDestinationNode.setSelectedColumns(tableColumnNames);

        // 添加节点
        transformation.addNode(tableDestinationNode);
        // transformation.addSuccessRouter(tableSourceNode,
        // tableDestinationNode);
        transformation.addSuccessRouter(sqlQuerySourceNode, tableDestinationNode);
    }

    /**
     * 保存转换
     */
    public void saveTransformation()
    {
        // 对节点和连接线自动布局，让流程图更美观
        transformation.layout();
        // 保存流程
        transformation.save();
    }

    /**
     * 运行转换-同步
     *
     * @return
     */
    public long TransformationSyncRun()
    {
        return transformation.syncRun();
    }

    /**
     * 运行转换-异步
     *
     * @return
     */
    public long TransformationAsyncRun()
    {
        return transformation.asyncRun();
    }

    /**
     * 获取转换日志
     *
     * @param executableId
     * @return
     */
    public RunLog getRunLog(long executableId)
    {
        return transformation.getRunLog(executableId);
    }

    /**
     * 获取所有执行日志
     *
     * @param executableId
     * @return
     */
    public RunLog getAllRunLog(long executableId)
    {
        return transformation.getRunLog(executableId);
    }
}
