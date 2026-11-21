package model;
public class Student extends Person {
    private String course;
    private String marks;
    private Visibility visibility;
    private String resume;
    public Student(String id, String name, String email, String phone, String course, String marks, Visibility v, String resume){
        super(id,name,email,phone);
        this.course=course; this.marks=marks; this.visibility=v; this.resume=resume;
    }
    public Student(String id, String name){ this(id,name,"","","","",Visibility.PUBLIC,""); }
    public String getCourse(){ return course; } public void setCourse(String c){ this.course=c; }
    public String getMarks(){ return marks; } public void setMarks(String m){ this.marks=m; }
    public Visibility getVisibility(){ return visibility; } public void setVisibility(Visibility v){ this.visibility=v; }
    public String getResume(){ return resume; } public void setResume(String r){ this.resume=r; }
    @Override public String getDisplayInfo(){ return getName()+" ("+getId()+") - "+course; }
    @Override public boolean isPrivate(){ return visibility==Visibility.PRIVATE; }
    @Override public String toString(){ return "Student{"+getDisplayInfo()+", visibility="+visibility+"}"; }
}