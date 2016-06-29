import java.util.List;
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
import static io.FileIOUtil.WriteMessage;

/**
 * Created by shuxinwei on 2016/6/25.
 */
public class TableAddColumn {
    public static void main(String[] args) {
        String webServiceConf ="D:\\log\\tabledetails.txt";
        String SQLoutPath = "D:\\log\\tablescript.txt";

        try{
            FileIOUtil fileIO = new FileIOUtil();
            List WebserviceDetail = fileIO.ReadTableConfig(webServiceConf);

            String alter = "";
            String comment = "";
            String totaled = "";
            for(int intface = 0; intface < WebserviceDetail.size(); intface++) {
                String[] tableDetail = fileIO.SplitLine(String.valueOf(WebserviceDetail.get(intface)));
                String mos = tableDetail[0];
                String tableName = tableDetail[1];

                alter = "alter table \"" + mos + "\".\"" + tableName + "\" add column(\"EXTRACT_TIME\" TIMESTAMP(0));\n" +
                        "alter table \"" + mos + "\".\"" + tableName + "\" add column(\"RESERVERD\" VARCHAR(50));\n";
                comment = "comment on column \"" + mos + "\".\"" + tableName + "\".\"EXTRACT_TIME\" is '抽取时间';\n" +
                        "comment on column \"" + mos + "\".\"" + tableName + "\".\"RESERVERD\" is '保留字段';\n";
                totaled += alter + comment;
            }

            WriteMessage(totaled,SQLoutPath);
            System.out.println("done");

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
