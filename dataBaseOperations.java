import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class DatabaseOperations {
    public String fileName;

    public String openDatabase() {
        // Ask user to enter a file
        System.out.println("Please enter name of your .csv file without the extension: ");
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            this.fileName = inputReader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }

        // Only create a new database if it doesn't already exist
        File csvFile = new File(this.fileName + ".data");
        if (csvFile.exists()) {
            System.out.println("Do stuff");
            return this.fileName;
        } else {
            new CreateNewDatabase(this.fileName);
            return this.fileName;
        }
    }
}