/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afeistock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Administrator
 */
public class Filex {

    public static void copyFile(String sourceFileName, String destFileName) {
        File source = new File(sourceFileName);
        File dest = new File(destFileName);
        Global.logd(sourceFileName+","+destFileName);
        try{
        FileUtils.copyFile(source, dest);
        }catch (IOException e){
            Global.loge(e.getMessage());
        }
    }
}
