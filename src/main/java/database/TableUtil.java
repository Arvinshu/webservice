package database;

import io.FileIOUtil;

import java.util.List;

/**
 * Created by Arvin on 2016/6/19.
 */
public class TableUtil {
    private String TableName;  //表名称
    private String TableDesc;  //表注释;

    private String[] ColumnName;    //列名称
    private String[] ColumnDesc;    //列注释

    private String CreateTableScript;    //建表语句
    private String InsertDataScript;    //插入语句

    private String WebservicePort;    //存放获取到的webservice端口和中文事项名称


    public void setTableName(String tableName){
        this.TableName = tableName;
    }

    public String getTableName() {
        return this.TableName;
    }

    public void setTableDesc(final String tableDesc) {
        this.TableDesc = tableDesc;
    }

    public String getTableDesc() {
        return this.TableDesc;
    }

    public void setColumnName(String[] columnName) {
        this.ColumnName = columnName;
    }

    public String[] getColumnName() {
        return this.ColumnName;
    }

    public void setColumnDesc(String[] columnDesc) {
        this.ColumnDesc = columnDesc;
    }

    public String[] getColumnDesc() {
        return this.ColumnDesc;
    }

    public void setCreateTableScript() {
        String column = "";
        String comment = "";
        if (this.ColumnName !=null) {
            String tableName = "WS_" + this.TableName;    //表名前加上“WS_”以表示该表来自webservice
            for (int i = 0; i < this.ColumnName.length; i++) {
                column += "\"" + ColumnName[i] + "\" VARCHAR2(5000),\n";
                comment += "comment on column \"" + tableName + "\".\"" + ColumnName[i] +  "\" is \'" + ColumnDesc[i] + "\';\n";
            }
            column += "EXTRACT_TIME TIMESTAMP(0),\n" + "RESERVERD VARCHAR(50)";
            String s0 =  "/*" + this.TableDesc + "\t" + this.TableName +"\t" + this.getWebserviceDetail() + "*/\n";
            String s1 = "create table \"" + tableName + "\"\n";
            String s2 = "storage(initial 1, next 1, minextents 1, fillfactor 0);\n";
            String s3 = "comment on table \"" +tableName+ "\" is '" + this.TableDesc +"';\n";
            this.CreateTableScript = s0 + s1 + "(\n" + column + "\n)\n" +s2 + s3 + comment;
        }
    }

    public String getCreateTableScript() {
        return this.CreateTableScript;
    }

    public void setWebservicePort(String port) {
        this.WebservicePort = port;
    }

    public String getWebserviceDetail() {
        return this.WebservicePort;
    }
}
