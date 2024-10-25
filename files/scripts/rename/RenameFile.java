//package com.common.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * <b><code>RenameFile</code></b>
 * <p/>
 * <p>
 * <p/>
 * <b>Creation Time:</b> 2024/10/19 1:19
 *
 * @author yang xiong
 * @since CommonJava 1.0
 */
public class RenameFile {
    public static final String CHARSET_NAME = "UTF-8";

    public static final String[] paramsTips = new String[]{
            "请配置目录参数",
            "请配置匹配模式参数",
            "请配置重命名模式参数"
    };

    public static final Map<String, Function<String[],String>> REPLACE_PROCESS_FUNCTION_MAP = new HashMap<>();

    public static final Map<String, Function<String,String>> REPLACEMENT_FUNCTION_MAP = new HashMap<>();

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        REPLACE_PROCESS_FUNCTION_MAP.put("--r", (ps) -> {
//            if(ps[0].matches(ps[1])){
                // fileName.replaceAll(pattern, replace);
                return ps[0].replaceAll(ps[1], ps[2]);
//            }
//            return ps[0];
        });
        REPLACE_PROCESS_FUNCTION_MAP.put("--s", (ps) -> {
            if(ps[0].contains(ps[1])){
                // fileName.replace(pattern, replace);
                return ps[0].replace(ps[1], ps[2]);
            }
            return ps[0];
        });
        REPLACEMENT_FUNCTION_MAP.put("--d", replace -> "");
        REPLACEMENT_FUNCTION_MAP.put("--r", replace -> replace);
    }

    public static void main(String[] args) throws IOException {
        String[] params = new String[3];
        int paramsNum = 0;
        Map<String, String> patternMap = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("./config"), Charset.forName(CHARSET_NAME))) {
            String line;
            while(paramsNum < 3 && null != (line = br.readLine())){
                params[paramsNum] = trim(removeComment(line));
                if(null != params[paramsNum]){
                    paramsNum++;
                }
            }
            while(null != (line = br.readLine())){
                line = removeComment(line);
                String lines[] = line.split("-->", 2);
                lines[0] = trim(lines[0]);
                if(null == lines[0]) {
                    continue;
                } else if(lines.length>=2) {
                    patternMap.put(lines[0], Optional.ofNullable(trim(lines[1])).orElse("").trim());
                } else {
                    patternMap.put(lines[0], "");
                }
            }
        } catch (Exception e) {
            System.err.println("readAllLinesUseNioReader:");
            e.printStackTrace();
        }

        if(paramsNum < 3){
            while (paramsNum < 3){
                System.err.println(paramsTips[paramsNum++]);
            }
            return;
        }

        String dataDir = params[0];
        File dir = new File(dataDir);
        // 匹配模式 --r：正则匹配，--s：字符串匹配
        String matchMod = params[1];
        Function<String[],String> replaceProcessFunction = REPLACE_PROCESS_FUNCTION_MAP.get(matchMod);
        // 模式 --d：删除匹配部分，--r：替换匹配部分
        String renameMod = params[2];
        Function<String,String> replacementFunction = REPLACEMENT_FUNCTION_MAP.get(renameMod);

        if(!dir.exists()){
            System.err.println("目录 " + dataDir +" 不存在");
            return;
        } else if(dir.isFile()){
            System.err.println(dataDir +" 不是目录");
            return;
        } else if(null == replaceProcessFunction){
            System.err.println("匹配模式: " + matchMod + " 不存在");
            return;
        } else if(null == replacementFunction){
            System.err.println("重命名模式: " + renameMod + " 不存在");
            return;
        } else {
            System.out.println("目录 " + dataDir);
            System.out.println("匹配模式: " + matchMod);
            System.out.println("重命名模式: " + renameMod);
        }
        rename(replaceProcessFunction, replacementFunction, dir, patternMap);
    }

    public static void rename(Function<String[],String> matcher, Function<String,String> replace, File dir, Map<String, String> patternMap){
        DateTimeFormatter dtf = DTF;
        String dirAbsolutePath = dir.getAbsolutePath();
        String[] ps = new String[3];
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("./result.txt"), Charset.forName(CHARSET_NAME), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for(File file:dir.listFiles()){
                String fileName = file.getName();
                ps[0] = fileName;
                for(Map.Entry<String, String> patternEntry:patternMap.entrySet()){
                    ps[1] = patternEntry.getKey();
                    ps[2] = replace.apply(patternEntry.getValue());
                    String newFileName = matcher.apply(ps);
                    if(!fileName.equals(newFileName)){
                        String result = String.format("%s %s %s --> %s\r\n",
                                dtf.format(LocalDateTime.now()),
                                file.renameTo(new File(dirAbsolutePath + File.separator + newFileName)),
                                fileName,
                                newFileName
                            );
                        System.out.println(result);
                        bw.write(result);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("writeAllLinesUseNioWriter:" + e);
        }
    }

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
}
