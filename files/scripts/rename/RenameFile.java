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
                params[paramsNum++] = line.trim();
            }
            while(null != (line = br.readLine())){
                String lines[] = line.trim().split("-->", 2);
                if(lines.length>=2){
                    patternMap.put(lines[0].trim(), Optional.ofNullable(lines[1]).orElse("").trim());
                } else if(lines.length>=1){
                    patternMap.put(lines[0].trim(), "");
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

        String dirPath = params[0];
        // 匹配模式 --r：正则匹配，--s：字符串匹配
        String matchMod = params[1];
        // 模式 --d：删除匹配部分，--r：替换匹配部分
        String renameMod = params[2];
        File dir = new File(dirPath);
        if(!dir.exists()){
            System.err.println("目录 " + params[0] +" 不存在");
            return;
        }
        if(dir.isFile()){
            System.err.println(params[0] +" 不是目录");
            return;
        }

        Function<String[],String> replaceProcessFunction = REPLACE_PROCESS_FUNCTION_MAP.get(matchMod);
        if(null == replaceProcessFunction){
            System.err.println("匹配模式: " + matchMod + " 不存在");
            return;
        }
        Function<String,String> replacementFunction = REPLACEMENT_FUNCTION_MAP.get(renameMod);
        if(null == replacementFunction){
            System.err.println("重命名模式: " + renameMod + " 不存在");
            return;
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
}
