import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;

public class CreateNewDatabase {

    public String fileName;


    CreateNewDatabase() {
        // Ask user to enter a file
        System.out.println("Please enter name of your .csv file: ");
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

            br = new BufferedReader(new FileReader(this.fileName));
            FileWriter fileWriter = new FileWriter("Fortune_500_HQ.data");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] country = line.split(cvsSplitBy);

                printWriter.printf("%-4s", country[0]);
                printWriter.printf("%-40s", country[1]);
                printWriter.printf("%-20s", country[2]);
                printWriter.printf("%-6s", country[3]);
                printWriter.printf("%-6s", country[4]);
                printWriter.printf("%-10s", country[5]);
                printWriter.printf("\n");
                System.out.printf("%-4s", country[0]);
                System.out.printf("%-40s", country[1]);
                System.out.printf("%-20s", country[2]);
                System.out.printf("%-6s", country[3]);
                System.out.printf("%-6s", country[4]);
                System.out.printf("%-10s", country[5]);
                System.out.printf("\n");
            }
            printWriter.close();

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
    }
}