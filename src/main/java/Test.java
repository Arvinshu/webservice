import database.TableUtil;
import io.FileIOUtil;
import java.io.IOException;
import java.util.List;
import static io.FileIOUtil.WriteMessage;

/**
 * 生成空表
 * Created by shuxinwei on 2016/6/24.
 */
public class Test {
    public static void main(String[] args) {
        String rootPath = "D:\\logs\\";    //根目录
        String PATH = "D:\\logs\\temp\\";    //文件操作目录

        //String tableFile_columnName_path = PATH + "EN\\";    //EN目录用于存放webservice英文列名（用于列名称）

        //String tableFile_columnDesc_path = PATH + "CN\\";    //CN目录用于存放webservice中文列名（用于列注释）

        String webServiceConf = rootPath + "webservicedata.txt";    //webserviceport.txt用于存放webservice端口和中文事项名称，格式为<端口号,列说明,列名称>  示例：<2016001,大型掘路>, <2016001,大型掘路,ZF_LAWCASEINFO_SHARED>
        String WebServiceNDT = rootPath + "port_no_data.txt";
        String webServiceSQL = PATH + "webserviceSQL.txt";    //webserviceSQL.txt用于存放建表SQL语句

        int line = 2;    //指定列名所在文件的的行数
        int wscolumn = 2;    //指定webserviceport.txt的列数目

        //etl配置信息
        //String etlHost = "192.168.128.132";
        String etlHost = "127.0.0.1";
        int etlPort = 1234;
        String etlUserName = "admin";
        String etlPassword = "admin";

        //etl项目与转换信息
        String etlProject = "webservice";
        String etlTransformation = "webservice";
        String etlInterface = "v1";
        String etlOp = "v2";

        try {
            //获取webservice接口
            FileIOUtil fileIO = new FileIOUtil();
            List WebserviceDetail = fileIO.ReadTableConfig(webServiceConf);

            //判断SQL写入文本是否存在，如果存在则清空
            fileIO.FileEmpty(webServiceSQL);
            fileIO.FileEmpty(WebServiceNDT);

            //逐行获取webservice端口和中文事项名称
            for(int intface = 0; intface < WebserviceDetail.size(); intface++) {
                String[] tableDetail = fileIO.SplitLine(String.valueOf(WebserviceDetail.get(intface)));
                String webservicePort = tableDetail[0];
                String tableDescp = tableDetail[1];
                String tableName = null;
                if(wscolumn == 2) {
                    tableName = FileIOUtil.c2e(tableDescp);

                }else {
                    tableName = tableDetail[2];
                }
                String table_en = "D:\\logs\\temp\\0\\00000.txt";
                String table_cn = "D:\\logs\\temp\\1\\11111.txt";

                int failTimes = 0;
                boolean success = true;
                if(fileIO.FileExit(table_en) && fileIO.FileExit(table_cn)) {
                    TableUtil table = new TableUtil();
                    table.setTableDesc(tableDescp);
                    table.setTableName(tableName);
                    table.setColumnName(fileIO.ReadColumn(line,table_en));
                    table.setColumnDesc(fileIO.ReadColumn(line,table_cn));
                    table.setWebservicePort(webservicePort);
                    table.setCreateTableScript();
                    String trans_data = PATH + "trans\\" +tableName + "_" + webservicePort + ".txt";
                    fileIO.ReadColumn(table_en, trans_data);
                    if(failTimes != 0) {
                        System.out.println(webservicePort + " failed " + failTimes + " times.");
                    }
                    WriteMessage(table.getCreateTableScript(),webServiceSQL);
                    table = null;
                }
                else {
                    System.out.println("ETL on Interface: " + webservicePort + " has no data!");
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
