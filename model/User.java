package model;
public class User {
    private String username; // Email or Username
    private String password;
    private String role; // ADMIN, STUDENT, GUEST
    private String studentId;
    private String name;
    private String email;
    public User(){}
    public User(String username, String password, String role, String studentId, String name, String email){
        this.username=username; this.password=password; this.role=role; this.studentId=studentId; this.name=name; this.email=email;
    }
    public String getUsername(){ return username; }
    public String getPassword(){ return password; }
    public String getRole(){ return role; }
    public String getStudentId(){ return studentId; }
    public String getName(){ return name; }
    public String getEmail(){ return email; }
    public void setUsername(String u){ username=u; }
    public void setPassword(String p){ password=p; }
    public void setRole(String r){ role=r; }
    public void setStudentId(String s){ studentId=s; }
    public void setName(String n){ name=n; }
    public void setEmail(String e){ email=e; }
}