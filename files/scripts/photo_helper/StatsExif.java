// package com.richstonedt.cmgde2eas.cs;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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

    public static final int CELL_SIZE = 40;

    public static void main(String[]args){
        StatsExif statsExif = new StatsExif();
        statsExif.start();
    }

    public void start(){
        String pathName = "E:\\temp\\pictures\\A7R5\\20241201\\RAW";
        File path = new File(pathName);
        List<String> fileNameList = Arrays.stream(
                path.list((dir, name) -> name.endsWith(".xmp"))
        ).sorted().collect(Collectors.toList());
        String lastThreeExif = "";
        String startFileName = "";
        String endFileName = "";
        for(String fileName:fileNameList){
            String threeExif = getThreeExif(pathName + File.separator + fileName, fileName);
            if(!lastThreeExif.equals(threeExif)){
                System.out.print(getCell(String.format("%s->%s", startFileName, endFileName)));
                System.out.println(getCell(lastThreeExif));

                lastThreeExif = threeExif;
                startFileName = fileName;
            }
            endFileName = fileName;
        }
        System.out.print(getCell(String.format("%s->%s", startFileName, endFileName)));
        System.out.println(getCell(lastThreeExif));
    }

    //   exif:FNumber="8/1"
    //   exif:ExposureTime="1/400"

    //   <exif:ISOSpeedRatings>
    //    <rdf:Seq>
    //     <rdf:li>800</rdf:li>
    //    </rdf:Seq>
    //   </exif:ISOSpeedRatings>

    public String getThreeExif(String filePath, String fileName){
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            String exposureTime = null;
            String fNumber = null;
            String iso = null;
            boolean findIso = false;
            while(null != (line = br.readLine())){
                if(line.contains("exif:ExposureTime=")){
                    exposureTime = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                    continue;
                }
                if(line.contains("exif:FNumber=")){
                    fNumber = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                    continue;
                }
                if(line.contains("<exif:ISOSpeedRatings>")){
                    findIso = true;
                    continue;
                }
                if(line.contains("</exif:ISOSpeedRatings>")){
                    findIso = false;
                    continue;
                }
                if(findIso && line.contains("<rdf:li>")){
                    iso = line.substring(line.indexOf(">") + 1, line.lastIndexOf("</"));
                    continue;
                }
            }
            return String.format("%s%sISO:%s", getCell("ExposureTime:"+exposureTime), getCell("FNumber:"+fNumber), iso);
        } catch (Exception e) {
            System.err.println("readAllLinesUseNioReader:"+filePath);
            e.printStackTrace();
        }
        return null;
    }

    public String getCell(String str){
        if(null == str){
            str = "";
        }
        if(str.length() < CELL_SIZE){
            byte[] originStrByte = str.getBytes(StandardCharsets.UTF_8);
            byte[] cellStrByte = new byte[CELL_SIZE];
            int i = 0;
            int originSize = originStrByte.length;
            while(i < originSize){
                cellStrByte[i] = originStrByte[i];
                i++;
            }
            while(i < CELL_SIZE){
                cellStrByte[i++] = ' ';
            }
            return new String(cellStrByte);
        }
        return str;
    }
}
