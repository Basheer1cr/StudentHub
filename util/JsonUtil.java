package util;
public interface JsonUtil {
    static String esc(String s){
        if(s==null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");
    }
}