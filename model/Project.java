package model;
import java.util.*;
public class Project {
    private int id;
    private String studentId;
    private String studentName;
    private String title;
    private String type;
    private String description;
    private Visibility visibility;
    private String date;
    private List<String> files = new ArrayList<>();
    public Project(int id,String sid,String sname,String title,String type,String desc,Visibility v,String date,List<String> files){
        this.id=id; this.studentId=sid; this.studentName=sname; this.title=title; this.type=type;
        this.description=desc; this.visibility=v; this.date=date; this.files=files;
    }
    public int getId(){return id;} public String getStudentId(){return studentId;}
    public String getStudentName(){return studentName;} public String getTitle(){return title;}
    public String getType(){return type;} public String getDescription(){return description;}
    public Visibility getVisibility(){return visibility;} public String getDate(){return date;}
    public List<String> getFiles(){return files;}
    public boolean isPrivate(){ return visibility==Visibility.PRIVATE; }
    @Override public String toString(){ return "Project{"+title+" by "+studentName+"}"; }
}