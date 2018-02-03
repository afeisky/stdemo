/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afeistock;

import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author chaofei.wu
 */
public class Global {

    public static Jdbc jdbc=new Jdbc();
    public static Down dldata = new Down();
    public static String appRootDir = System.getProperty("user.dir");
    public static String workDir ="../../download"; //"Y:\\stdata";

    private static boolean _DEBUG=true;
    public Global() {
        String appRootDir = System.getProperty("user.dir");
    }

    public static boolean init(String args[]) {
        if (args.length > 0) {
            System.out.println(args[0]);
            if (args.length > 1) {
                Global.workDir = args[1];
            }
        }
        if (workDir.length()==0){
            workDir=appRootDir;
        }
        File f=Paths.get(appRootDir, "..","afei.log").toFile();
        if (!f.exists()){
            System.exit(1);
            return false;
        }
        jdbc.init();
        return true;
    }
    
    public static void logi(String str){
        if (_DEBUG) System.out.println(str);
    }
    public static void logi(Object obj){
         if (_DEBUG) System.out.println(obj.toString());
    }    
    public static void logd(String str){
        System.out.println(str);
    }   
    public static void logd(Object obj){
        System.out.println(obj.toString());
    }        
    public static void loge(String str){
        System.out.println(str);
    }
}
