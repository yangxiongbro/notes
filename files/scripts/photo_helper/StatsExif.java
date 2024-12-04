// package com.richstonedt.cmgde2eas.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <b><code>StatsExif</code></b>
 * <p/>
 * <p>
 * <p/>
 * <b>Creation Time:</b> 2024/12/3 9:55
 *
 * @author yang xiong
 * @since cmgde2eas-ai-invoke-be 0.1.0
 */
public class StatsExif {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //   exif:FNumber="8/1"
    //   exif:ExposureTime="1/400"

    //   <exif:ISOSpeedRatings>
    //    <rdf:Seq>
    //     <rdf:li>800</rdf:li>
    //    </rdf:Seq>
    //   </exif:ISOSpeedRatings>

    //    crs:WhiteBalance="Daylight"
    //    crs:Temperature="5500"
    //    crs:Tint="+10"

    public static final String QUOTE = "QUOTE";

    public static final String XML = "XML";

    public static final String[] FIELD_NAMES = new String[]{
            "ExposureTime", "FNumber", "ISO"//, "WhiteBalance", "Temperature", "Tint"
    };
    public static final String[] FIELD_KEYS = new String[]{
            "exif:ExposureTime=", "exif:FNumber=", "<rdf:li>"//, "crs:WhiteBalance=", "crs:Temperature=", "crs:Tint="
    };
    public static final String[] FIELD_VALUE_FLAGS = new String[]{
            QUOTE, QUOTE, XML//, QUOTE, QUOTE, QUOTE
    };
    public static final String[] BEGIN_SEARCH_FIELD_KEYS = new String[]{
            null, null, "<exif:ISOSpeedRatings>"//, null, null, null
    };
    public static final String[] END_SEARCH_FIELD_KEYS = new String[]{
            null, null, "</exif:ISOSpeedRatings>"//, null, null, null
    };

    public static final int CELL_SIZE = 30;

    public static void main(String[]args) {
        StatsExif statsExif = new StatsExif();
        statsExif.start();
    }

    public void start() {
        String[]configs = readConfig("./stats_exif_config");
        String pathName = configs[0];
        File path = new File(pathName);
        List<String> fileNameList = Arrays.stream(
                path.list((dir, name) -> name.endsWith(".xmp"))
        ).sorted().collect(Collectors.toList());
        String[] lastExifs = new String[FIELD_KEYS.length];
        String[] exifs = new String[FIELD_KEYS.length];
        String startFileName = "";
        String endFileName = "";
        String resultFilePath = pathName + File.separator + "stats_exif_result.txt";
        boolean[]searchFieldFlags = new boolean[FIELD_KEYS.length];
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(resultFilePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            // 输出表头
            bw.write("\r\n");
            bw.write("\r\n");
            bw.write(DTF.format(LocalDateTime.now()));
            bw.write("\r\n");
            bw.write(getCell("FromFileName", CELL_SIZE));
            bw.write(getCell("ToFileName", CELL_SIZE));
            for(String fieldName:FIELD_NAMES){
                bw.write(getCell(fieldName, CELL_SIZE));
            }
            bw.write("\r\n");

            for(String fileName:fileNameList){
                initArray(exifs);
                statsExif(pathName + File.separator + fileName, fileName, searchFieldFlags, exifs);
                if(!judgeSameExif(lastExifs, exifs)){
                    bw.write(getCell(startFileName, CELL_SIZE));
                    bw.write(getCell(endFileName, CELL_SIZE));
                    for(String exif:lastExifs){
                        bw.write(getCell(exif, CELL_SIZE));
                    }
                    bw.write("\r\n");

                    copyArray(lastExifs, exifs);
                    startFileName = fileName;
                }
                endFileName = fileName;
            }
            bw.write(getCell(startFileName, CELL_SIZE));
            bw.write(getCell(endFileName, CELL_SIZE));
            for(String exif:lastExifs){
                bw.write(getCell(exif, CELL_SIZE));
            }
            bw.write("\r\n");
            System.err.println("输出到文件：" + resultFilePath);
        } catch (Exception e) {
            System.err.println("writeAllLinesUseNioWriter:" + e);
        }
    }

    public String statsExif(String filePath, String fileName, boolean[]searchFieldFlags, String[]row){
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while(null != (line = br.readLine())){
                int fieldSize = FIELD_KEYS.length;
                for(int i = 0;i < fieldSize;i++){
                    String fieldKey = FIELD_KEYS[i];
                    String beginSearchFieldKey = BEGIN_SEARCH_FIELD_KEYS[i];
                    String endSearchFieldKey = END_SEARCH_FIELD_KEYS[i];

                    if(null == beginSearchFieldKey || null == endSearchFieldKey){
                        searchFieldFlags[i] = true;
                    } else {
                        if(line.contains(beginSearchFieldKey)){
                            searchFieldFlags[i] = true;
                            break;
                        } else if(line.contains(endSearchFieldKey)){
                            searchFieldFlags[i] = false;
                            break;
                        }
                    }

                    if(searchFieldFlags[i] && line.contains(fieldKey)){
                        row[i] = getFieldValue(line, FIELD_VALUE_FLAGS[i]);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("readAllLinesUseNioReader:"+filePath);
            e.printStackTrace();
        }
        return null;
    }

    public boolean judgeSameExif(String[] lastExifs, String[] exifs){
        int size = exifs.length;
        for(int i = 0;i < size;i++){
            if(!exifs[i].equals(lastExifs[i])){
                return false;
            }
        }
        return true;
    }

    public void initArray(String[]row){
        int size = row.length;
        for(int i = 0;i < size;i++){
            row[i] = "";
        }
    }

    public void copyArray(String[] newArray, String[] oldArray){
        int size = oldArray.length;
        for(int i = 0;i < size;i++){
            newArray[i] = oldArray[i];
        }
    }


    /**
     * @description: 获取""/></内的内容
     * @param: str
     * @return: java.lang.String
     * @throws
     * @author yang xiong
     * @date 2024/12/4 14:27
     **/
    public String getFieldValue(String str, String flag){
        if(null != str){
            int index = -1;
            int lastIndex = -1;
            if(QUOTE.equals(flag)){
                index = str.indexOf("\"");
                lastIndex = str.lastIndexOf("\"");
            } else if(XML.equals(flag)){
                index = str.indexOf(">");
                lastIndex = str.lastIndexOf("</");
            }
            if(index >= 0 && lastIndex >= 0){
                return str.substring(index + 1, lastIndex);
            }
        }
        return "";
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
