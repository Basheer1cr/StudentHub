package util;
import model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileStorage {
    // Save Students to file - So data never lost
    public static synchronized void saveStudents(List<Student> list){
        try{
            Files.createDirectories(Paths.get("data"));
            StringBuilder sb=new StringBuilder();
            for(Student s: list){
                // Use | as separator, replace | in data to avoid break
                sb.append(s.getId()).append("|")
                  .append(s.getName().replace("|"," ")).append("|")
                  .append(s.getEmail().replace("|"," ")).append("|")
                  .append(s.getPhone().replace("|"," ")).append("|")
                  .append(s.getCourse().replace("|"," ")).append("|")
                  .append(s.getMarks().replace("|"," ")).append("|")
                  .append(s.getVisibility().name()).append("|")
                  .append(s.getResume().replace("|"," ")).append("\n");
            }
            Files.write(Paths.get("data/students.txt"), sb.toString().getBytes());
        }catch(Exception e){ System.out.println("Save failed: "+e.getMessage()); }
    }

    public static List<Student> loadStudents(){
        List<Student> list=new ArrayList<>();
        try{
            Path p=Paths.get("data/students.txt");
            if(!Files.exists(p)) return list;
            for(String line: Files.readAllLines(p)){
                if(line.trim().isEmpty()) continue;
                String[] a=line.split("\\|", -1);
                if(a.length>=8){
                    list.add(new Student(a[0],a[1],a[2],a[3],a[4],a[5],Visibility.valueOf(a[6]),a[7]));
                }
            }
        }catch(Exception e){ System.out.println("Load failed: "+e.getMessage()); }
        return list;
    }

    public static synchronized void saveProjects(List<Project> list){
        try{
            Files.createDirectories(Paths.get("data"));
            StringBuilder sb=new StringBuilder();
            for(Project pr: list){
                sb.append(pr.getId()).append("|")
                  .append(pr.getStudentId()).append("|")
                  .append(pr.getStudentName().replace("|"," ")).append("|")
                  .append(pr.getTitle().replace("|"," ")).append("|")
                  .append(pr.getType().replace("|"," ")).append("|")
                  .append(pr.getDescription().replace("|"," ")).append("|")
                  .append(pr.getVisibility().name()).append("|")
                  .append(pr.getDate().replace("|"," ")).append("|")
                  .append(String.join(",", pr.getFiles())).append("\n");
            }
            Files.write(Paths.get("data/projects.txt"), sb.toString().getBytes());
        }catch(Exception e){ System.out.println("Save projects failed: "+e.getMessage()); }
    }

    public static List<Project> loadProjects(){
        List<Project> list=new ArrayList<>();
        try{
            Path p=Paths.get("data/projects.txt");
            if(!Files.exists(p)) return list;
            for(String line: Files.readAllLines(p)){
                if(line.trim().isEmpty()) continue;
                String[] a=line.split("\\|", -1);
                if(a.length>=9){
                    List<String> files=new ArrayList<>();
                    if(!a[8].isEmpty()) files.addAll(Arrays.asList(a[8].split(",")));
                    list.add(new Project(Integer.parseInt(a[0]),a[1],a[2],a[3],a[4],a[5],Visibility.valueOf(a[6]),a[7],files));
                }
            }
        }catch(Exception e){ System.out.println("Load projects failed: "+e.getMessage()); }
        return list;
    }
}