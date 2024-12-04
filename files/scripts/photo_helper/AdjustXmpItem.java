//package com.common.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <b><code>AdjustXmpItem</code></b>
 * <p/>
 * <p>
 * <p/>
 * <b>Creation Time:</b> 2024/12/2 16:28
 *
 * @author yang xiong
 * @since cmgde2eas-sr-be 0.1.0
 */

// javac -encoding UTF-8 AdjustXmpItem.java
// java -Xmx128m -Xms128m -Dfile.encoding=UTF-8 AdjustXmpItem

public class AdjustXmpItem {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args){
        AdjustXmpItem adjustXmpItem = new AdjustXmpItem();
        adjustXmpItem.start();
    }

    public void start(){
        String pathName = "C:\\MyWorkspaces\\Pictures\\A7C2";
        double min = 2750.0;
        double max= 3200.0;
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

//            double exposureValue = max - step * i;
//            String exposureValueStr = String.format(exposureValue > 0 ? "+%.2f" : "%.2f", exposureValue);

            double exposureValue = min + step * i;
            String exposureValueStr = String.format("%d", Math.round(exposureValue));
//            String exposureValueStr = String.format(exposureValue > 0 ? "+%d" : "%d", Math.round(exposureValue));

//            String exposureValueStr = "Custom";

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
//                if(line.contains("crs:Exposure2012=")){
                if(line.contains("crs:Temperature=")){
//                if(line.contains("crs:Tint=")){
//                if(line.contains("crs:WhiteBalance=")){
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

        if(sb.length() > 0 && null != edit){
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
}
