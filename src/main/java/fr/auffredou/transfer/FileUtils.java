package fr.auffredou.transfer;

public class FileUtils {
    public static String parentDirectory(String path){
        int pos = path.lastIndexOf("/");
        if(pos <= 0){
            return "/";
        }else{
            return path.substring(0,pos);
        }
    }

    public static String[] directoryChain(String path){
        return parentDirectory(path).split("/");
    }
}
