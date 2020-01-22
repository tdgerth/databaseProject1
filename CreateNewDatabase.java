import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class CreateNewDatabase {

    public String fileName;
    public File overflowFile;
    public File dataFile;
    public File configFile;
    
    CreateNewDatabase() {
        // Ask user to enter a file
        System.out.println("Please enter name of your .csv file without the extension: ");
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            this.fileName = inputReader.readLine();

            System.out.println(fileName);
            // Split file into 3 other files
        } catch (IOException e) {
            System.out.println(e);
        }

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(this.fileName + ".csv"));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] country = line.split(cvsSplitBy);

                for (int i = 0; i < 6; i++) {
                    System.out.print(country[i] + " ");
                }

                System.out.println();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.overflowFile = new File(this.fileName + ".config");
        this.configFile = new File(this.fileName + ".overflow");
        this.dataFile = new File(this.fileName + ".data");
    }
}