package io;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Arvin on 2016/6/19.
 */
public class FileIOUtil implements FileIO {
    private String encoding = "GBK";    //设置所读取文件的编码格式

    /***
     * 读取文件指定行，用于获取列名和列注释（一次只返回一行数据）
     * @param lineNumber
     * @param filePath
     * @return
     * @throws Exception
     */
    public String [] ReadColumn(int lineNumber, String filePath) throws IOException {
        //string转换file
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file),encoding));
        String s = "";
        if(lineNumber == 0) {
            //TODO 处理异常或者处理表名称或表注释
            String error = filePath + " 读取列数为0，程序无法处理";
            this.WriteERROR(error);
        }
        else {

        }
        int lines = 0;
        while (lines != lineNumber) {
            lines++;
            s = br.readLine();
        }
        br.close();
        //去掉双引号和单引号
        s = s.replaceAll("\"","").replaceAll("\'","");
        //以逗号分隔，返回数组
        return s.split(",");
    }

    /***
     * 传入文件路径，将文件进行格式替换后再写入另一个文本。
     * @param inFilePath
     * @param outFilePath
     * @throws Exception
     */
    public void ReadColumn(String inFilePath, String outFilePath) throws IOException {
        File inFile = new File(inFilePath);
        File outFile = new File(outFilePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(inFile),encoding));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        //FileWriter writer = new FileWriter(outFile);
        String line = "";
        int lineNumber = 0;
        List list  = new ArrayList();
        while ((line = br.readLine()) != null && line != "") {
            lineNumber++;
            if(lineNumber > 2) {
                //line.replaceAll("\"","").replaceAll("\'","").replaceAll(",","\t");
                //list.add(line);
                String test = line.replaceAll("\"","");
                String test1 = test.replaceAll("\',\'", "\t");
                String test2 = test1;
                bw.write(test1);
                bw.newLine();
                //writer.write(line);
            }
        }
        br.close();
        bw.close();
        //writer.close();
        //return list;
    }

    public int CountLines(String path) throws IOException {
        int lines = 0;
        File file = new File(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file),encoding));
        //LineNumberReader reader = new LineNumberReader(new FileReader(file));
        String s = "";
        while (br.readLine() != null && br.readLine() != ""){
            lines++;
        }
        br.close();
        return lines;
    }

    /***
     * 传入一个文本路径，读取文本内所有数据，并以list形式返回文本内所有数据
     * @param filePath
     * @return
     * @throws Exception
     */
    public List ReadTableConfig(String filePath) throws IOException {
        //string路径转换为file
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file),encoding));
        String line = "";
        List stringlist  = new ArrayList();
        while((line = br.readLine()) != null) {
            //Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            //Matcher m = p.matcher(line);
            //stringlist.add(m);
            stringlist.add(line);
        }
        br.close();
        return stringlist;
    }


    //递归遍历目录下的所有文件，返回所有文件的列表
    public ArrayList<File> getListFiles(Object obj) throws IOException {
        File directory = null;
        if (obj instanceof File) {
            directory = (File) obj;
        } else {
            directory = new File(obj.toString());
        }
        ArrayList<File> files = new ArrayList<File>();
        if (directory.isFile()) {
            files.add(directory);
            return files;
        } else if (directory.isDirectory()) {
            File[] fileArr = directory.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File fileOne = fileArr[i];
                files.addAll(getListFiles(fileOne));
            }
        }
        return files;
    }

    public String [] SplitLine(String filePath) throws Exception {
        return filePath.replaceAll("\"","").replaceAll("\'","").split(",");
        //return filePath.split(",");
    }

    public boolean CountColumn(String filePathEN, String filePathCN) throws FileNotFoundException, IOException {
        int lineNumber = 2;
        File fileEN = new File(filePathEN);
        File fileCN = new File(filePathCN);
        if(fileCN.exists() == false && fileEN.exists() == false) {
            throw new FileNotFoundException("File no found");
        }
        String en = "";
        String cn = "";
        BufferedReader brEN = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(fileEN),encoding));
        BufferedReader brCN = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(fileCN),encoding));
        int lines = 0;
        while (lines != lineNumber) {
            lines++;
            en = brEN.readLine();
            cn = brCN.readLine();
        }
        brEN.close();
        brCN.close();
        //去掉双引号和单引号
        en = en.replaceAll("\"","").replaceAll("\'","");
        cn = cn.replaceAll("\"","").replaceAll("\'","");
        //以逗号分隔，返回数组
        if(en.split(",").length == cn.split(",").length) {
            return true;
        }
        else {
            return false;
        }

    }

    /***
     * 传入一个文件的绝对路径，判断文件是否存在，如果存在，则清空目录
     * @param path
     * @return
     * @throws IOException
     */
    public boolean FileExit(String path) throws IOException{
        File file = new File(path);
        if(file.exists()){
            return true;
        }
        else {
            return false;
        }
    }

    public void FileEmpty(String path) throws IOException{
        File file = new File(path);
        if(file.exists()){
            FileWriter fw =  new FileWriter(file);
            fw.write("");
            fw.close();
        }
    }

    public static String c2e(String c)
    {
        StringBuffer nameSb = new StringBuffer();
        for (int j = 0; j < c.length(); j++) {
            char word = c.charAt(j);
            // 提取汉字的首字母
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                nameSb.append(pinyinArray[0].charAt(0));
            }
            else {
                nameSb.append(word);
            }
        }
        return nameSb.toString().toUpperCase();
    }

    public static void WriteERROR(String error) throws IOException {
        File fileName = new File("D:\\logs\\temp\\error.txt");
        //追加模式
        FileWriter writer=new FileWriter(fileName,true);
        SimpleDateFormat format=new SimpleDateFormat();
        String time=format.format(new Date());
        writer.write(time + "\t" + error + "\r\n");
        writer.close();
    }

    public static void WriteMessage(String message, String Path) throws IOException {
        if(message != null) {
            File file = new File(Path);
            FileWriter writer = new FileWriter(file, true);
            writer.write(message + "\r\n");
            writer.close();
        }
        else {
            System.out.println("传入字符串为空");
        }
    }

    public static void main(String[] args) {
        String en = "D:\\logs\\20160240.txt";
        String cn = "D:\\logs\\20160241.txt";
        FileIOUtil f = new FileIOUtil();
        try{
            boolean b = f.CountColumn(en, cn);
            System.out.println(b);
        }
        catch (IOException e) {
            System.out.println(e);
        }

    }
}
