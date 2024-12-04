//package com.common.java;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <b><code>AdjustExposure</code></b>
 * <p/>
 * <p>
 * <p/>
 * <b>Creation Time:</b> 2024/12/2 16:28
 *
 * @author yang xiong
 * @since cmgde2eas-sr-be 0.1.0
 */
public class AdjustXmp {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args){
        AdjustXmp adjustXmp = new AdjustXmp();
        adjustXmp.start();
    }

    public void start(){
        String[]configs = readConfig("./adjust_xmp_config");
        String pathName = configs[0];
        String findKey = configs[1];
        String replaceReg = configs[2];
        String format = configs[3];
        double min = Double.valueOf(configs[4]);
        double max = Double.valueOf(configs[5]);
        String replaceValue = Double.valueOf(configs[6]);
        File path = new File(pathName);
        List<String> list = Arrays.stream(path.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xmp");
            }
        })).sorted().collect(Collectors.toList());
        int size = list.size();
        if(size < 2){
            return;
        }
        int listSize = list.size();
        double step = (Math.abs(max - min)) / (size - 1);
        for(int i = 0;i < listSize; i++){
            String fileName = list.get(i);
            String filePath = pathName + File.separator + fileName;
            double exposureValue = max - step * i;
            String exposureValueStr = String.format(exposureValue > 0 ? "+%.2f" : "%.2f", exposureValue);
//            System.out.println(fileName + " " + exposureValueStr);
            adjust(filePath, fileName, exposureValueStr);
        }
    }

    public void adjust(String filePath, String fileName, String exposureValue){
        StringBuffer sb = new StringBuffer();
        boolean isSuccess = true;
        String origin = null;
        String edit = null;;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while(null != (line = br.readLine())){
                if(line.contains("crs:Exposure2012=")){
                    origin = line;
                    edit = line.replaceFirst("(?<=\\\").*?(?=\\\")", exposureValue);
                    sb.append(edit);
                } else {
                    sb.append(line);
                }
                sb.append("\n");
            }
        } catch (Exception e) {
            isSuccess = false;
            System.err.println("readAllLinesUseNioReader:");
            e.printStackTrace();
        }

        if(sb.length() > 0){
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                bw.write(sb.toString());
            } catch (Exception e) {
                isSuccess = false;
                System.err.println("writeAllLinesUseNioWriter:" + e);
            }
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("./result.txt"), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                String result = String.format("%s %s %s \"%s\" --> \"%s\"\r\n",
                        DTF.format(LocalDateTime.now()),
                        isSuccess,
                        fileName,
                        origin,
                        edit
                );
                System.out.println(result);
                bw.write(result);
            } catch (Exception e) {
                System.err.println("writeAllLinesUseNioWriter:" + e);
            }
        }
    }

    /********************************************************公共方法***********************************************************/

    /**
     * @description: 去掉字符串注释符号‘//’之后的内容
     * @param:
     * @return: String
     * @throws
     * @author yang xiong
     * @date 2024/10/25 17:38
     **/
    private static String removeComment(String line){
        if(null != line && line.indexOf("//") >= 0){
            return line.substring(0, line.indexOf("//"));
        }
        return line;
    }

    /**
     * @description: 去除首尾空字符串，如果去除首尾空字符串之后的字符串是个空字符串则返回 null
     * @param: str
     * @return: java.lang.String
     * @throws
     * @author yang xiong
     * @date 2024/10/25 17:51
     **/
    private static String trim(String str){
        if(null != str){
            String trimStr = str.trim();
            return trimStr.isEmpty() ? null : trimStr;
        }
        return str;
    }

    /**
     * @description: 读取文件，去除//之后以及空行。每行一项
     * @param: configPath
     * @return: java.lang.String[]
     * @throws
     * @author yang xiong
     * @date 2024/12/4 16:24
     **/
    public String[] readConfig(String configPath) {
        List<String> configList = new ArrayList<>(10);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(configPath), StandardCharsets.UTF_8)) {
            String line;
            String configItem;
            while(null != (line = br.readLine())){
                configItem = trim(removeComment(line));
                if(null != configItem){
                    configList.add(configItem);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        if(configList.size()<1){
            System.out.println("请配置"+configPath);
        }
        return configList.toArray(new String[configList.size()]);
    }

    /**
     * @description: 输出固定长度字符串
     * @param: str
     * @param: len
     * @return: java.lang.String
     * @throws
     * @author yang xiong
     * @date 2024/12/4 16:29
     **/
    public String getCell(String str, int len){
        if(null == str){
            str = "";
        }
        String format = "%-" + len + "s\t";
        return String.format(format, str);
    }
}
