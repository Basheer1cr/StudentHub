package service;
import model.*;
import java.util.*;

public class ResumeScoreService {
    // Calculates Resume Score - Like ATS System
    public static int calculateScore(Student s, List<Project> allProjects){
        int score = 0;
        String details = "";

        // 1. Marks - 50 Marks
        try{
            int marks = Integer.parseInt(s.getMarks().replaceAll("[^0-9]",""));
            if(marks >= 90) score += 50;
            else if(marks >= 75) score += 40;
            else if(marks >= 60) score += 30;
            else if(marks >= 40) score += 20;
            else score += 10;
        }catch(Exception e){ score += 0; }

        // 2. Resume Uploaded - 20 Marks
        if(s.getResume() != null && !s.getResume().isEmpty()) score += 20;

        // 3. Has Project - 20 Marks
        boolean hasProject = false;
        for(Project p: allProjects){
            if(p.getStudentId().equals(s.getId())){ hasProject = true; break; }
        }
        if(hasProject) score += 20;

        // 4. Contact Complete - 10 Marks
        if(s.getEmail() != null && !s.getEmail().isEmpty() && s.getPhone() != null && !s.getPhone().isEmpty()) score += 10;

        return Math.min(score, 100);
    }

    public static String getGrade(int score){
        if(score >= 80) return "🟢 Excellent";
        if(score >= 50) return "🟡 Good";
        return "🔴 Needs Improvement";
    }

    public static String getTips(Student s, List<Project> allProjects){
        List<String> tips = new ArrayList<>();
        try{
            int marks = Integer.parseInt(s.getMarks().replaceAll("[^0-9]",""));
            if(marks < 75) tips.add("📚 Improve marks - Focus on studies");
        }catch(Exception e){ tips.add("📚 Add marks"); }
        if(s.getResume()==null || s.getResume().isEmpty()) tips.add("📄 Upload Resume - +20 Marks");
        boolean hasProject=false;
        for(Project p: allProjects){ if(p.getStudentId().equals(s.getId())) hasProject=true; }
        if(!hasProject) tips.add("📦 Upload a Project - +20 Marks");
        if(s.getEmail()==null || s.getEmail().isEmpty() || s.getPhone()==null || s.getPhone().isEmpty()) tips.add("📞 Add Email & Phone");
        if(tips.isEmpty()) return "✅ Perfect Profile! Keep it up.";
        return String.join(" | ", tips);
    }
}