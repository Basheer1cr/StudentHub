package util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.sun.net.httpserver.HttpExchange;
public class MultipartParser {
    public static Map<String,Object> parse(HttpExchange ex) throws IOException {
        String ct = ex.getRequestHeaders().getFirst("Content-Type");
        String boundary = "--" + ct.split("boundary=")[1];
        byte[] body = ex.getRequestBody().readAllBytes();
        byte[] bnd = boundary.getBytes(StandardCharsets.ISO_8859_1);
        Map<String,String> fields = new HashMap<>();
        List<Map<String,Object>> files = new ArrayList<>();
        int pos=0;
        while(true){
            int start=indexOf(body,bnd,pos); if(start<0) break;
            int hs=start+bnd.length+2; if(hs>=body.length) break;
            int he=indexOf(body,"\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1),hs); if(he<0) break;
            String header=new String(body,hs,he-hs,StandardCharsets.ISO_8859_1);
            int cs=he+4; int next=indexOf(body,bnd,cs); if(next<0) break;
            int ce=next-2; if(ce<cs) ce=cs;
            String name=null,filename=null;
            for(String tok:header.split(";")){tok=tok.trim(); if(tok.startsWith("name=")) name=tok.substring(6,tok.length()-1); if(tok.startsWith("filename=")) filename=tok.substring(10,tok.length()-1);}
            byte[] cb=Arrays.copyOfRange(body,cs,ce);
            if(filename!=null && !filename.isEmpty()){ Map<String,Object> f=new HashMap<>(); f.put("field",name); f.put("filename",filename); f.put("bytes",cb); files.add(f); }
            else if(name!=null) fields.put(name,new String(cb,StandardCharsets.UTF_8));
            pos=next;
        }
        Map<String,Object> res=new HashMap<>(); res.put("fields",fields); res.put("files",files); return res;
    }
    private static int indexOf(byte[] d,byte[] p,int from){
        outer:for(int i=from;i<=d.length-p.length;i++){ for(int j=0;j<p.length;j++) if(d[i+j]!=p[j]) continue outer; return i; } return -1;
    }
}