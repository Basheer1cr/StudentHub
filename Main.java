import com.sun.net.httpserver.*;
import model.*;
import service.*;
import util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        StudentService ss = StudentService.getInstance();
        ProjectService ps = ProjectService.getInstance();
        AuthService auth = AuthService.getInstance();
        int PORT = 8000;
        try { PORT = Integer.parseInt(System.getenv("PORT")); } catch(Exception e) {}
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/", ex->{ try{ serve(ex,"index.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/home", ex->{ try{ serve(ex,"index.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/index.html", ex->{ try{ serve(ex,"index.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/style.css", ex->{ try{ serve(ex,"style.css"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/login", ex->{ try{ serve(ex,"login.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/signup", ex->{ try{ serve(ex,"signup.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/add-student", ex->{ try{ serve(ex,"add-student.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/students-page", ex->{ try{ serve(ex,"students.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/add-project", ex->{ try{ serve(ex,"addproject.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/addproject", ex->{ try{ serve(ex,"addproject.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/projects-page", ex->{ try{ serve(ex,"projects.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/analytics", ex->{ try{ serve(ex,"analytics.html"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/profile", ex->{ try{ serve(ex,"profile.html"); }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/login", ex->{ try{
            Map<String,String> q = getParams(ex);
            Optional<User> u=auth.login(q.getOrDefault("username",""), q.getOrDefault("password",""));
            if(u.isPresent()){
                User user=u.get();
                String json="{\"success\":true,\"username\":\""+JsonUtil.esc(user.getUsername())+"\",\"role\":\""+user.getRole()+"\",\"studentId\":\""+JsonUtil.esc(user.getStudentId())+"\",\"name\":\""+JsonUtil.esc(user.getName())+"\"}";
                sendJson(ex,json);
            } else { sendJson(ex,"{\"success\":false,\"message\":\"Invalid username or password\"}"); }
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/signup", ex->{ try{
            Map<String,String> q = getParams(ex);
            String sid = q.getOrDefault("studentId","");
            if(sid.isEmpty()) sid = q.getOrDefault("username","");
            String result = auth.signup(q.getOrDefault("name",""), q.getOrDefault("email",""), q.getOrDefault("username",""), q.getOrDefault("password",""), q.getOrDefault("phone",""), q.getOrDefault("course","B.Tech"), sid);
            if(result.equals("OK")){
                Optional<User> u = auth.login(q.getOrDefault("username",""), q.getOrDefault("password",""));
                if(u.isPresent()){
                    User user=u.get();
                    sendJson(ex,"{\"success\":true,\"username\":\""+JsonUtil.esc(user.getUsername())+"\",\"role\":\""+user.getRole()+"\",\"studentId\":\""+JsonUtil.esc(user.getStudentId())+"\",\"name\":\""+JsonUtil.esc(user.getName())+"\"}");
                } else sendJson(ex,"{\"success\":true}");
            } else {
                sendJson(ex,"{\"success\":false,\"message\":\""+JsonUtil.esc(result)+"\"}");
            }
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/profile", ex->{ try{ handleProfile(ex, ss, ps); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/api/student", ex->{ try{ handleProfile(ex, ss, ps); }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/students", ex->{ try{
            Map<String,String> qp = queryToMap(ex.getRequestURI().getRawQuery());
            String q = qp.getOrDefault("q","");
            List<Student> list = ss.getAll();
            if(!q.isEmpty()){
                String lq = q.toLowerCase();
                List<Student> filtered = new ArrayList<>();
                for(Student s: list) if(s.getName().toLowerCase().contains(lq) || s.getId().toLowerCase().contains(lq) || s.getCourse().toLowerCase().contains(lq)) filtered.add(s);
                list = filtered;
            }
            StringBuilder sb=new StringBuilder("[");
            for(int i=0;i<list.size();i++){ Student s=list.get(i); if(i>0) sb.append(",");
                sb.append("{\"id\":\"").append(JsonUtil.esc(s.getId())).append("\",");
                sb.append("\"name\":\"").append(JsonUtil.esc(s.getName())).append("\",");
                sb.append("\"email\":\"").append(JsonUtil.esc(s.getEmail())).append("\",");
                sb.append("\"phone\":\"").append(JsonUtil.esc(s.getPhone())).append("\",");
                sb.append("\"course\":\"").append(JsonUtil.esc(s.getCourse())).append("\",");
                sb.append("\"marks\":\"").append(JsonUtil.esc(s.getMarks())).append("\",");
                sb.append("\"visibility\":\"").append(s.getVisibility().name().toLowerCase()).append("\",");
                sb.append("\"resume\":\"").append(JsonUtil.esc(s.getResume())).append("\"}");
            } sb.append("]"); sendJson(ex,sb.toString());
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/projects", ex->{ try{
            Map<String,String> qp = queryToMap(ex.getRequestURI().getRawQuery());
            String q = qp.getOrDefault("q","").toLowerCase();
            String type = qp.getOrDefault("type","");
            StringBuilder sb=new StringBuilder("["); List<Project> list=ps.getAll();
            boolean first=true;
            for(Project p: list){
                if(!type.isEmpty() && !p.getType().equalsIgnoreCase(type)) continue;
                if(!q.isEmpty()){
                    String hay = (p.getTitle()+" "+p.getStudentId()+" "+p.getStudentName()+" "+p.getType()+" "+p.getDescription()).toLowerCase();
                    if(!hay.contains(q)) continue;
                }
                if(!first) sb.append(","); first=false;
                sb.append("{\"id\":").append(p.getId()).append(",");
                sb.append("\"studentId\":\"").append(JsonUtil.esc(p.getStudentId())).append("\",");
                sb.append("\"studentName\":\"").append(JsonUtil.esc(p.getStudentName())).append("\",");
                sb.append("\"title\":\"").append(JsonUtil.esc(p.getTitle())).append("\",");
                sb.append("\"type\":\"").append(JsonUtil.esc(p.getType())).append("\",");
                sb.append("\"description\":\"").append(JsonUtil.esc(p.getDescription())).append("\",");
                sb.append("\"visibility\":\"").append(p.getVisibility().name().toLowerCase()).append("\",");
                sb.append("\"files\":[");
                for(int j=0;j<p.getFiles().size();j++){ if(j>0) sb.append(","); sb.append("\"").append(JsonUtil.esc(p.getFiles().get(j))).append("\""); }
                sb.append("],");
                sb.append("\"file\":\"").append(p.getFiles().size()>0?JsonUtil.esc(p.getFiles().get(0)):"").append("\"}");
            } sb.append("]"); sendJson(ex,sb.toString());
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/analytics", ex->{ try{
            List<Student> all=ss.getAll();
            Map<String,Integer> courseCount=new LinkedHashMap<>();
            Map<String,Integer> marksRange=new LinkedHashMap<>();
            marksRange.put("90-100",0); marksRange.put("75-89",0); marksRange.put("60-74",0); marksRange.put("Below 60",0);
            int totalMarks=0; int cntMarks=0;
            List<Student> sorted=new ArrayList<>(all);
            sorted.sort((a,b)->{ try{ return Integer.compare(Integer.parseInt(b.getMarks().replaceAll("[^0-9]","")), Integer.parseInt(a.getMarks().replaceAll("[^0-9]",""))); }catch(Exception e){ return 0; } });
            StringBuilder top=new StringBuilder("[");
            for(int i=0;i<Math.min(5,sorted.size());i++){ Student s=sorted.get(i); if(i>0) top.append(",");
                top.append("{\"name\":\"").append(JsonUtil.esc(s.getName())).append("\",\"marks\":").append(s.getMarks().replaceAll("[^0-9]","").isEmpty()?"0":s.getMarks().replaceAll("[^0-9]","")).append(",\"course\":\"").append(JsonUtil.esc(s.getCourse())).append("\"}");
            }
            top.append("]");
            for(Student s: all){
                courseCount.put(s.getCourse(), courseCount.getOrDefault(s.getCourse(),0)+1);
                try{ int m=Integer.parseInt(s.getMarks().replaceAll("[^0-9]","")); totalMarks+=m; cntMarks++;
                    if(m>=90) marksRange.put("90-100", marksRange.get("90-100")+1);
                    else if(m>=75) marksRange.put("75-89", marksRange.get("75-89")+1);
                    else if(m>=60) marksRange.put("60-74", marksRange.get("60-74")+1);
                    else marksRange.put("Below 60", marksRange.get("Below 60")+1);
                }catch(Exception e){}
            }
            double avg=cntMarks>0?(double)totalMarks/cntMarks:0;
            StringBuilder cb=new StringBuilder("{"); int idx=0; for(Map.Entry<String,Integer> e: courseCount.entrySet()){ if(idx++>0) cb.append(","); cb.append("\"").append(JsonUtil.esc(e.getKey())).append("\":").append(e.getValue()); } cb.append("}");
            StringBuilder mb=new StringBuilder("{"); idx=0; for(Map.Entry<String,Integer> e: marksRange.entrySet()){ if(idx++>0) mb.append(","); mb.append("\"").append(e.getKey()).append("\":").append(e.getValue()); } mb.append("}");
            Map<String,Integer> projCount=new LinkedHashMap<>();
            for(Project p: ps.getAll()) projCount.put(p.getType(), projCount.getOrDefault(p.getType(),0)+1);
            StringBuilder pb=new StringBuilder("{"); idx=0; for(Map.Entry<String,Integer> e: projCount.entrySet()){ if(idx++>0) pb.append(","); pb.append("\"").append(JsonUtil.esc(e.getKey())).append("\":").append(e.getValue()); } pb.append("}");
            String topperName = sorted.size()>0 ? sorted.get(0).getName() : "None";
            String topMarks = sorted.size()>0 ? sorted.get(0).getMarks().replaceAll("[^0-9]","") : "0";
            String dist = "{\"90+\":"+marksRange.get("90-100")+",\"75-89\":"+marksRange.get("75-89")+",\"60-74\":"+marksRange.get("60-74")+",\"Below 60\":"+marksRange.get("Below 60")+"}";
            String json="{\"total\":"+all.size()+",\"avgMarks\":"+String.format("%.1f",avg)+",\"course\":"+cb+",\"byCourse\":"+cb+",\"marksRange\":"+mb+",\"distribution\":"+dist+",\"projects\":"+pb+",\"projectsByType\":"+pb+",\"totalProjects\":"+ps.getAll().size()+",\"topper\":"+top+",\"topperName\":\""+JsonUtil.esc(topperName)+"\",\"topMarks\":\""+topMarks+"\",\"top5\":"+top+"}";
            sendJson(ex,json);
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/upload-student", ex->{ try{
            Map<String,Object> data=MultipartParser.parse(ex);
            Map<String,String> f=(Map<String,String>)data.get("fields");
            List<Map<String,Object>> files=(List<Map<String,Object>>)data.get("files");
            String resume=""; List<String> projFiles=new ArrayList<>();
            for(Map<String,Object> fl:files){
                String field=(String)fl.get("field"); String fn=(String)fl.get("filename");
                if(fn==null||fn.isEmpty()) continue;
                String safe=System.currentTimeMillis()+"_"+fn.replaceAll("[^a-zA-Z0-9._-]","_");
                if("resume".equals(field)){ resume=safe; FileService.saveFile("resumes",safe,(byte[])fl.get("bytes")); }
                else { FileService.saveFile("projects",safe,(byte[])fl.get("bytes")); projFiles.add(safe); }
            }
            String sid=f.getOrDefault("id","");
            Student s=new Student(sid,f.getOrDefault("name",""),f.getOrDefault("email",""),f.getOrDefault("phone",""),f.getOrDefault("course",""),f.getOrDefault("marks",""),Visibility.fromString(f.getOrDefault("visibility","public")),resume);
            ss.add(s);
            String pt=f.getOrDefault("projectTitle","");
            if(!pt.isEmpty()){
                Project p=new Project(ps.nextId(),s.getId(),s.getName(),pt,f.getOrDefault("projectType","Other"),f.getOrDefault("projectDesc",""),Visibility.fromString(f.getOrDefault("visibility","public")),new Date().toString(),projFiles);
                ps.add(p);
            }
            redirect(ex,"/students-page");
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/upload-project", ex->{ try{
            Map<String,Object> data=MultipartParser.parse(ex);
            Map<String,String> f=(Map<String,String>)data.get("fields");
            List<String> saved=new ArrayList<>();
            List<Map<String,Object>> files=(List<Map<String,Object>>)data.get("files");
            for(Map<String,Object> fl:files){ String fn=(String)fl.get("filename"); if(fn!=null&&!fn.isEmpty()){ String safe=System.currentTimeMillis()+"_"+fn.replaceAll("[^a-zA-Z0-9._-]","_"); FileService.saveFile("projects",safe,(byte[])fl.get("bytes")); saved.add(safe); } }
            Visibility vis=Visibility.PUBLIC; String sname="";
            Optional<Student> opt=ss.findById(f.getOrDefault("studentId",""));
            if(opt.isPresent()){ vis=opt.get().getVisibility(); sname=opt.get().getName(); }
            Project p=new Project(ps.nextId(),f.getOrDefault("studentId",""),sname,f.getOrDefault("title",""),f.getOrDefault("type",""),f.getOrDefault("description",""),vis,new Date().toString(),saved);
            ps.add(p); redirect(ex,"/projects-page");
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/download", ex->{ try{
            Map<String,String> q=queryToMap(ex.getRequestURI().getRawQuery()); String file=q.get("file");
            if(file!=null && file.startsWith("[")) file=file.replaceAll("[\\[\\]\"]","").split(",")[0];
            Path p1=Paths.get("projects/"+file); Path p2=Paths.get("resumes/"+file);
            Path p=Files.exists(p1)?p1:p2; if(!Files.exists(p)){ sendText(ex,"File not found"); return; }
            byte[] b=Files.readAllBytes(p);
            ex.getResponseHeaders().set("Content-Disposition","attachment; filename=\""+file+"\"");
            ex.sendResponseHeaders(200,b.length); ex.getResponseBody().write(b); ex.getResponseBody().close();
        }catch(Exception e){e.printStackTrace();} });
        server.createContext("/delete-project", ex->{ try{ int id=Integer.parseInt(queryToMap(ex.getRequestURI().getRawQuery()).getOrDefault("id","-1")); ps.delete(id); sendText(ex,"OK"); }catch(Exception e){e.printStackTrace();} });
        server.createContext("/delete-student", ex->{ try{ String id=queryToMap(ex.getRequestURI().getRawQuery()).getOrDefault("id","-1"); ss.delete(id); sendText(ex,"OK"); }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/stats", ex->{ try{
            long total=ss.getAll().size();
            long pub=ss.countPublic(); long pri=ss.countPrivate();
            long proj=ps.getAll().size();
            String topper="None"; String topMarks="";
            if(ss.getTopper().isPresent()){ topper=ss.getTopper().get().getName(); topMarks=ss.getTopper().get().getMarks(); }
            String json="{\"total\":"+total+",\"public\":"+pub+",\"private\":"+pri+",\"projects\":"+proj+",\"topper\":\""+JsonUtil.esc(topper)+"\",\"topMarks\":\""+JsonUtil.esc(topMarks)+"\"}";
            sendJson(ex,json);
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/search", ex->{ try{
            Map<String,String> q=queryToMap(ex.getRequestURI().getRawQuery());
            List<Student> list=ss.search(q.getOrDefault("q",""), q.getOrDefault("course",""), q.getOrDefault("sort",""));
            StringBuilder sb=new StringBuilder("[");
            for(int i=0;i<list.size();i++){ Student s=list.get(i); if(i>0) sb.append(",");
                sb.append("{\"id\":\"").append(JsonUtil.esc(s.getId())).append("\",");
                sb.append("\"name\":\"").append(JsonUtil.esc(s.getName())).append("\",");
                sb.append("\"email\":\"").append(JsonUtil.esc(s.getEmail())).append("\",");
                sb.append("\"phone\":\"").append(JsonUtil.esc(s.getPhone())).append("\",");
                sb.append("\"course\":\"").append(JsonUtil.esc(s.getCourse())).append("\",");
                sb.append("\"marks\":\"").append(JsonUtil.esc(s.getMarks())).append("\",");
                sb.append("\"visibility\":\"").append(s.getVisibility().name().toLowerCase()).append("\",");
                sb.append("\"resume\":\"").append(JsonUtil.esc(s.getResume())).append("\"}");
            } sb.append("]"); sendJson(ex,sb.toString());
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/update-student", ex->{ try{
            Map<String,Object> data=MultipartParser.parse(ex);
            Map<String,String> f=(Map<String,String>)data.get("fields");
            List<Map<String,Object>> files=(List<Map<String,Object>>)data.get("files");
            String resume=f.getOrDefault("oldResume","");
            for(Map<String,Object> fl:files){ String fn=(String)fl.get("filename"); if(fn!=null&&!fn.isEmpty()){ String safe=System.currentTimeMillis()+"_"+fn.replaceAll("[^a-zA-Z0-9._-]","_"); FileService.saveFile("resumes",safe,(byte[])fl.get("bytes")); resume=safe; } }
            Student s=new Student(f.getOrDefault("id",""),f.getOrDefault("name",""),f.getOrDefault("email",""),f.getOrDefault("phone",""),f.getOrDefault("course",""),f.getOrDefault("marks",""),Visibility.fromString(f.getOrDefault("visibility","public")),resume);
            ss.update(s); redirect(ex,"/students-page");
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/export-csv", ex->{ try{
            StringBuilder csv=new StringBuilder("ID,Name,Email,Phone,Course,Marks,Visibility\n");
            for(Student s: ss.getAll()) csv.append(s.getId()).append(",").append(s.getName()).append(",").append(s.getEmail()).append(",").append(s.getPhone()).append(",").append(s.getCourse()).append(",").append(s.getMarks()).append(",").append(s.getVisibility()).append("\n");
            byte[] b=csv.toString().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Disposition","attachment; filename=\"students.csv\"");
            ex.getResponseHeaders().set("Content-Type","text/csv");
            ex.sendResponseHeaders(200,b.length); ex.getResponseBody().write(b); ex.getResponseBody().close();
        }catch(Exception e){e.printStackTrace();} });

        server.createContext("/api/score", ex->{ try{
            Map<String,String> q=queryToMap(ex.getRequestURI().getRawQuery());
            String id=q.getOrDefault("id","");
            Optional<Student> opt=ss.findById(id);
            if(!opt.isPresent()){ sendJson(ex,"{\"score\":0,\"grade\":\"Not Found\",\"tips\":\"Student not found\"}"); return; }
            Student s=opt.get();
            int score=ResumeScoreService.calculateScore(s, ps.getAll());
            String grade=ResumeScoreService.getGrade(score);
            String tips=ResumeScoreService.getTips(s, ps.getAll());
            String json="{\"score\":"+score+",\"grade\":\""+JsonUtil.esc(grade)+"\",\"tips\":\""+JsonUtil.esc(tips)+"\"}";
            sendJson(ex,json);
        }catch(Exception e){e.printStackTrace();} });

        server.start(); System.out.println("Server running on PORT " + PORT + " at http://localhost:" + PORT);
    }

    static void handleProfile(HttpExchange ex, StudentService ss, ProjectService ps) throws Exception {
        Map<String,String> q=queryToMap(ex.getRequestURI().getRawQuery());
        String id=q.getOrDefault("id","");
        Optional<Student> opt=ss.findById(id);
        if(!opt.isPresent()){ sendJson(ex,"{\"found\":false}"); return; }
        Student s=opt.get();
        int score=ResumeScoreService.calculateScore(s, ps.getAll());
        String grade=ResumeScoreService.getGrade(score);
        String tips=ResumeScoreService.getTips(s, ps.getAll());
        StringBuilder pj=new StringBuilder("[");
        List<Project> myProj=new ArrayList<>();
        for(Project p: ps.getAll()) if(p.getStudentId().equals(id)) myProj.add(p);
        for(int i=0;i<myProj.size();i++){ Project p=myProj.get(i); if(i>0) pj.append(",");
            pj.append("{\"id\":").append(p.getId()).append(",");
            pj.append("\"title\":\"").append(JsonUtil.esc(p.getTitle())).append("\",");
            pj.append("\"type\":\"").append(JsonUtil.esc(p.getType())).append("\",");
            pj.append("\"description\":\"").append(JsonUtil.esc(p.getDescription())).append("\",");
            pj.append("\"files\":[");
            for(int j=0;j<p.getFiles().size();j++){ if(j>0) pj.append(","); pj.append("\"").append(JsonUtil.esc(p.getFiles().get(j))).append("\""); }
            pj.append("]}");
        }
        pj.append("]");
        String json="{\"found\":true,\"id\":\""+JsonUtil.esc(s.getId())+"\",\"name\":\""+JsonUtil.esc(s.getName())+"\",\"email\":\""+JsonUtil.esc(s.getEmail())+"\",\"phone\":\""+JsonUtil.esc(s.getPhone())+"\",\"course\":\""+JsonUtil.esc(s.getCourse())+"\",\"marks\":\""+JsonUtil.esc(s.getMarks())+"\",\"visibility\":\""+s.getVisibility().name().toLowerCase()+"\",\"resume\":\""+JsonUtil.esc(s.getResume())+"\",\"score\":"+score+",\"grade\":\""+JsonUtil.esc(grade)+"\",\"tips\":\""+JsonUtil.esc(tips)+"\",\"projects\":"+pj+"}";
        sendJson(ex,json);
    }

    static Map<String,String> getParams(HttpExchange ex) throws Exception {
        Map<String,String> m = queryToMap(ex.getRequestURI().getRawQuery());
        if(ex.getRequestMethod().equalsIgnoreCase("POST") || ex.getRequestMethod().equalsIgnoreCase("PUT")){
            InputStream is = ex.getRequestBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096]; int n;
            while((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            String body = baos.toString(StandardCharsets.UTF_8.name()).trim();
            if(!body.isEmpty()){
                if(body.startsWith("{")){
                    String inner = body.substring(1, body.lastIndexOf("}"));
                    List<String> parts = new ArrayList<>();
                    boolean inQ=false; StringBuilder cur=new StringBuilder();
                    for(char c: inner.toCharArray()){ if(c=='"') inQ=!inQ; if(c==',' && !inQ){ parts.add(cur.toString()); cur.setLength(0); } else cur.append(c); }
                    if(cur.length()>0) parts.add(cur.toString());
                    for(String p: parts){
                        int colon = p.indexOf(":");
                        if(colon>0){
                            String k = p.substring(0, colon).trim().replaceAll("\"","").trim();
                            String v = p.substring(colon+1).trim().replaceAll("^\"|\"$","").trim();
                            v = v.replace("\\\"","\"");
                            if(!k.isEmpty()) m.put(k, v);
                        }
                    }
                } else {
                    Map<String,String> fm = queryToMap(body);
                    m.putAll(fm);
                }
            }
        }
        return m;
    }

    static void serve(HttpExchange ex,String file) throws IOException {
        Path p=Paths.get(file); if(!Files.exists(p)){ sendText(ex,"File not found: "+file); return; }
        byte[] b=Files.readAllBytes(p);
        if(file.endsWith(".css")) ex.getResponseHeaders().set("Content-Type","text/css");
        else ex.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");
        ex.sendResponseHeaders(200,b.length); ex.getResponseBody().write(b); ex.getResponseBody().close();
    }
    static void sendJson(HttpExchange ex,String j) throws IOException { byte[] b=j.getBytes(StandardCharsets.UTF_8); ex.getResponseHeaders().set("Content-Type","application/json"); ex.sendResponseHeaders(200,b.length); ex.getResponseBody().write(b); ex.getResponseBody().close(); }
    static void sendText(HttpExchange ex,String t) throws IOException { byte[] b=t.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(200,b.length); ex.getResponseBody().write(b); ex.getResponseBody().close(); }
    static void redirect(HttpExchange ex,String loc) throws IOException { ex.getResponseHeaders().set("Location",loc); ex.sendResponseHeaders(302,-1); ex.close(); }
    static Map<String,String> queryToMap(String q){ Map<String,String> m=new HashMap<>(); if(q==null) return m; for(String pair:q.split("&")){ String[] kv=pair.split("=",2); if(kv.length==2) try{ m.put(URLDecoder.decode(kv[0],"UTF-8"),URLDecoder.decode(kv[1],"UTF-8")); }catch(Exception e){} } return m; }
}