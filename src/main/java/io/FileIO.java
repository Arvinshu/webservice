package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuxinwei on 2016/6/14.
 */
public interface FileIO {
    //获取指定目录下所有文件的文件名
    ArrayList<File> getListFiles(Object obj) throws IOException;

    //读取指定文件和指定行数，获取列名称和列注释
    String [] ReadColumn(int lineNumber, String filePath) throws Exception;

    //读取文本文件，获取表名称和表注释
    List ReadTableConfig(String filepath) throws Exception;

    //计算文本行数
    int CountLines(String path) throws IOException;
}
