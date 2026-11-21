package service;
import model.*;
import java.util.*;
import java.util.stream.Collectors;
public class StudentService {
    private static StudentService instance;
    private List<Student> students = new ArrayList<>();
    private int autoId = 1;
    private StudentService(){}
    public static synchronized StudentService getInstance(){
        if(instance==null) instance=new StudentService();
        return instance;
    }
    public synchronized void add(Student s){
        if(s.getId()==null||s.getId().isEmpty()) s.setId(String.valueOf(autoId++));
        students.add(s);
    }
    public synchronized void update(Student updated){
        for(int i=0;i<students.size();i++){
            if(students.get(i).getId().equals(updated.getId())){
                students.set(i, updated);
                break;
            }
        }
    }
    public List<Student> getAll(){ return new ArrayList<>(students); }
    public List<Student> getPublicOnly(){ return students.stream().filter(s->!s.isPrivate()).collect(Collectors.toList()); }
    public Optional<Student> findById(String id){ return students.stream().filter(s->s.getId().equals(id)).findFirst(); }
    public synchronized void delete(String id){ students.removeIf(s->s.getId().equals(id)); }

    // NEW - For Dashboard + Search
    public long countPublic(){ return students.stream().filter(s->!s.isPrivate()).count(); }
    public long countPrivate(){ return students.stream().filter(s->s.isPrivate()).count(); }
    public Optional<Student> getTopper(){
        return students.stream().filter(s->!s.getMarks().isEmpty()).max((a,b)->{
            try{ return Integer.compare(Integer.parseInt(a.getMarks().replaceAll("[^0-9]","")), Integer.parseInt(b.getMarks().replaceAll("[^0-9]",""))); }catch(Exception e){ return 0; }
        });
    }
    public List<Student> search(String keyword, String course, String sort){
        List<Student> result = students.stream()
            .filter(s-> keyword==null || keyword.isEmpty() || s.getName().toLowerCase().contains(keyword.toLowerCase()) || s.getId().contains(keyword))
            .filter(s-> course==null || course.isEmpty() || course.equals("All") || s.getCourse().toLowerCase().contains(course.toLowerCase()))
            .collect(Collectors.toList());
        if("marks".equals(sort)) result.sort((a,b)->{
            try{ return Integer.compare(Integer.parseInt(b.getMarks().replaceAll("[^0-9]","")), Integer.parseInt(a.getMarks().replaceAll("[^0-9]",""))); }catch(Exception e){ return 0; }
        });
        if("name".equals(sort)) result.sort((a,b)-> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }
}