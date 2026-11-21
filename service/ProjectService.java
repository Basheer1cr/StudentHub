package service;
import model.*;
import util.FileStorage;
import java.util.*;
import java.util.stream.Collectors;
public class ProjectService {
    private static ProjectService instance;
    private List<Project> projects = new ArrayList<>();
    private int autoId = 1;
    private ProjectService(){
        projects = FileStorage.loadProjects();
        for(Project p: projects){ if(p.getId()>=autoId) autoId=p.getId()+1; }
        System.out.println("Loaded "+projects.size()+" projects from file");
    }
    public static synchronized ProjectService getInstance(){ if(instance==null) instance=new ProjectService(); return instance; }
    public synchronized void add(Project p){ projects.add(p); FileStorage.saveProjects(projects); }
    public synchronized int nextId(){ return autoId++; }
    public List<Project> getAll(){ return new ArrayList<>(projects); }
    public List<Project> getPublicOnly(){ return projects.stream().filter(p->!p.isPrivate()).collect(Collectors.toList()); }
    public synchronized void delete(int id){ projects.removeIf(p->p.getId()==id); FileStorage.saveProjects(projects); }
}