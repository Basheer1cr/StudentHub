package service;
import model.*;
import util.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;

public class AuthService {
    private static AuthService instance;
    private List<User> users = new ArrayList<>();
    private AuthService(){ load(); }
    public static AuthService getInstance(){ if(instance==null) instance=new AuthService(); return instance; }

    public void load(){
        users.clear();
        // Default accounts - Always work
        users.add(new User("admin","admin123","ADMIN","", "Admin", "admin@studenthub.com"));
        users.add(new User("guest","guest","GUEST","", "Guest", "guest@studenthub.com"));
        try{
            Path p = Paths.get("users.json");
            if(Files.exists(p)){
                String content = Files.readString(p);
                // Simple parse: {"username":"...","password":"...","role":"...","studentId":"...","name":"...","email":"..."}
                String[] objs = content.split("\\},\\{");
                for(String o : objs){
                    o = o.replace("[","").replace("]","").replace("{","").replace("}","");
                    if(o.trim().isEmpty()) continue;
                    Map<String,String> m = new HashMap<>();
                    for(String part : o.split("\",\"")){
                        String[] kv = part.replace("\"","").split(":",2);
                        if(kv.length==2) m.put(kv[0].trim(), kv[1].trim());
                    }
                    if(m.containsKey("username")) users.add(new User(m.getOrDefault("username",""), m.getOrDefault("password",""), m.getOrDefault("role","STUDENT"), m.getOrDefault("studentId",""), m.getOrDefault("name",""), m.getOrDefault("email","")));
                }
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    public void save(){
        try{
            StringBuilder sb = new StringBuilder("[");
            boolean first=true;
            for(User u: users){
                // Don't save default admin/guest - they are hardcoded
                if(u.getUsername().equals("admin") || u.getUsername().equals("guest")) continue;
                if(!first) sb.append(",");
                first=false;
                sb.append("{\"username\":\"").append(JsonUtil.esc(u.getUsername())).append("\",");
                sb.append("\"password\":\"").append(JsonUtil.esc(u.getPassword())).append("\",");
                sb.append("\"role\":\"").append(u.getRole()).append("\",");
                sb.append("\"studentId\":\"").append(JsonUtil.esc(u.getStudentId())).append("\",");
                sb.append("\"name\":\"").append(JsonUtil.esc(u.getName())).append("\",");
                sb.append("\"email\":\"").append(JsonUtil.esc(u.getEmail())).append("\"}");
            }
            sb.append("]");
            Files.writeString(Paths.get("users.json"), sb.toString());
        }catch(Exception e){ e.printStackTrace(); }
    }

    // SIGNUP - Returns "OK" or error message
    public String signup(String name, String email, String username, String password, String phone, String course, String studentId){
        // Check duplicate username / email / studentId
        for(User u: users){
            if(u.getUsername().equalsIgnoreCase(username)) return "Username already taken";
            if(u.getEmail().equalsIgnoreCase(email)) return "Email already registered - Please Login";
            if(!studentId.isEmpty() && u.getStudentId().equals(studentId)) return "Student ID already registered";
        }
        // If studentId empty, auto-generate: 100 + count
        if(studentId==null || studentId.isEmpty()){
            studentId = String.valueOf(100 + users.size() + 1);
        }
        User nu = new User(username, password, "STUDENT", studentId, name, email);
        users.add(nu);
        save();
        // Also create Student profile automatically
        StudentService ss = StudentService.getInstance();
        if(!ss.findById(studentId).isPresent()){
            Student s = new Student(studentId, name, email, phone, course, "0", Visibility.PUBLIC, "");
            ss.add(s);
        }
        return "OK";
    }

    public Optional<User> login(String username, String password){
        // 1. Check real users + admin/guest
        for(User u: users){
            if((u.getUsername().equalsIgnoreCase(username) || u.getEmail().equalsIgnoreCase(username)) && u.getPassword().equals(password)){
                return Optional.of(u);
            }
        }
        // 2. Old Demo: Login with Student ID as username AND password = Student ID
        // So old students still work: 101 / 101
        StudentService ss = StudentService.getInstance();
        Optional<Student> s = ss.findById(username);
        if(s.isPresent() && password.equals(username)){
            return Optional.of(new User(username, password, "STUDENT", username, s.get().getName(), s.get().getEmail()));
        }
        return Optional.empty();
    }

    public List<User> getAllUsers(){ return users; }
}