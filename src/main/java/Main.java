import com.dameng.etl.driver.RunLog;
import database.TableUtil;
import etl.ETLUtil;
import io.FileIOUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.FileIOUtil.*;

/**
 * 只需要将webservice端口和事项中文名称按照格式放在指定的位置，系统将根据接口自动读取webservice接口，并生成建表语句
 * Created by Arvin on 2016/6/20.
 */

public class Main {
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
        //String etlHost = "192.168.128.133";
        String etlHost = "192.168.1.105";
        //String etlHost = "127.0.0.1";
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
                String table_en = "";
                String table_cn = "";

                int failTimes = 0;
                boolean success = true;
                //循环OP
                do {
                    for(int op = 0; op <= 1; op++) {
                        //String filePath = PATH + op + "\\" + webservicePort +".txt";
                        if(op == 0) {
                            table_en = PATH + op + "\\" + webservicePort +".txt";
                        }
                        else {
                            table_cn = PATH + op + "\\" + webservicePort +".txt";
                        }
                        //新建etl连接
                        ETLUtil etlUtil = new ETLUtil();
                        // ETL连接参数
                        int loopnum = 0;
                        if (etlUtil.conn(etlHost, etlPort, etlUserName, etlPassword)) {
                            etlUtil.findProject(etlProject);
                            etlUtil.findTransformation(etlTransformation);

                            Map<String,String> map = new HashMap<String,String>();
                            map.put(etlInterface, webservicePort);
                            map.put(etlOp, Integer.toString(op));
                            //设置ETL变量
                            etlUtil.setVariables(map);

                            long id = etlUtil.TransformationSyncRun();
                            RunLog log = etlUtil.getRunLog(id);
                            if(log.getStatus().toString() != "FINISHED") {
                                System.out.println(log.getMessage());
                                //将执行失败的任务存入文本
                                String errmsg = "webservice端口执行ETL失败。\t" + webservicePort;
                                WriteERROR(errmsg);
                            }
                            etlUtil.close();

                            //TODO:执行文件检测，查看英文和中文的列名长度是否匹配，如果不匹配则重新执行ETL流程
                            if(op == 1) {
                                //捕获找不到文件的错误，如果没有文件，说明接口访问正常，而接口无数据
                                try{
                                    if(fileIO.CountColumn(table_en, table_cn)) {
                                        success = true;
                                        System.out.println("ETL Get Interface: " + webservicePort + " successed.");
                                    }
                                    else {
                                        success = false;
                                        failTimes++;
                                        System.out.println("ETL Get Interface: " + webservicePort + " failed!!!");
                                    }
                                }
                                catch (FileNotFoundException e) {
                                    WriteMessage(webservicePort + "," + tableDescp + "," + tableName + "\t" + " has no data!", WebServiceNDT);
                                    WriteERROR(e.toString());
                                }

                            }
                        }
                        else
                        {
                            String errmsg = "连接ETL失败，在执行端口: " + webservicePort;
                            System.out.println(errmsg);
                            WriteERROR(errmsg);
                        }
                        etlUtil = null;
                    }
                }
                //要求循环条件是boolean类型，值为true时循环执行，否则循环结束
                while (success == false);

                if(fileIO.FileExit(table_en) && fileIO.FileExit(table_cn)) {
                    TableUtil table = new TableUtil();
                    table.setTableDesc(tableDescp);
                    table.setTableName(tableName);
                    table.setColumnName(fileIO.ReadColumn(line,table_en));
                    table.setColumnDesc(fileIO.ReadColumn(line,table_cn));
                    table.setWebservicePort(webservicePort);
                    table.setCreateTableScript();
                    //TODO:遍历EN文本，生成插入语句
                    String trans_data = PATH + "trans\\" +tableName + "_" + webservicePort + ".txt";
                    fileIO.ReadColumn(table_en, trans_data);
                    ;
                    //System.out.println("/*" + webservicePort + "*/");
                    if(failTimes != 0) {
                        System.out.println(webservicePort + " failed " + failTimes + " times.");
                    }
                    //System.out.println(table.getCreateTableScript());
                    WriteMessage(table.getCreateTableScript(),webServiceSQL);
                    table = null;
                }
                else {
                    System.out.println("ETL on Interface: " + webservicePort + " has no data!");
                }
                //强制进行垃圾回收
                //Runtime.getRuntime().gc();
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