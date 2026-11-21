package service;
import java.nio.file.*;
import java.io.IOException;
public class FileService {
    public static void saveFile(String folder, String fileName, byte[] bytes) throws IOException {
        try {
            Path dir = Paths.get(folder);
            if(!Files.exists(dir)) Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), bytes);
        } catch(IOException e){
            System.err.println("File save failed: "+e.getMessage());
            throw e;
        }
    }
    public static byte[] readFile(Path p) throws IOException { return Files.readAllBytes(p); }
}