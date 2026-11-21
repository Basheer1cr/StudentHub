package model;
public abstract class Person {
    private String id;
    private String name;
    private String email;
    private String phone;
    public Person(String id, String name, String email, String phone){
        this.id=id; this.name=name; this.email=email; this.phone=phone;
    }
    public String getId(){ return id; } public void setId(String id){ this.id=id; }
    public String getName(){ return name; } public void setName(String n){ this.name=n; }
    public String getEmail(){ return email; } public void setEmail(String e){ this.email=e; }
    public String getPhone(){ return phone; } public void setPhone(String p){ this.phone=p; }
    public abstract String getDisplayInfo();
    public abstract boolean isPrivate();
}