import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PinNames {

    public static List<String> getPinNames(String filePath) {
        List<String> pinNames = new ArrayList<>();
        Path pinNamesFile = Paths.get(filePath);

        try {

            pinNames = Files.readAllLines(pinNamesFile);

        } catch (NoSuchFileException e) {
            System.out.println("Files not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pinNames;
    }

    public static HashMap<String, Boolean> compare(List<String> originalList, List<String> compareToList){
        HashMap<String, Boolean> storage = new LinkedHashMap<>();
        for(String name : originalList){
            storage.put(name, false);
        }

        for(String name : compareToList){
            storage.put(name, storage.containsKey(name));
        }

        return storage;
    }
}
