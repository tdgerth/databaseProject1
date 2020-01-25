import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;

public class CreateNewDatabase {

    public String databaseName;
    public boolean validDatabaseName = true;

    private DatabaseOperations dbOps;

    public File overflowFile;
    public File dataFile;
    public File configFile;

    public byte[] currentRecord = new byte[89];
    
    // Creating a new database with all three necessary files and instantiating them
    CreateNewDatabase() {
        inputDatabaseName();

        if (this.validDatabaseName == true) {

            this.dbOps = new DatabaseOperations(this.databaseName);

            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";
    
            try {
                this.dataFile = new File(this.databaseName + ".data");
                this.dataFile.createNewFile();
    
                FileWriter dataFileWriter = new FileWriter(this.dataFile.toString());
                PrintWriter dataFilePrinter = new PrintWriter(dataFileWriter);
    
                createConfigFile();
    
                br = new BufferedReader(new FileReader(this.databaseName + ".csv"));
    
                // Saving the number of lines in the csv file to calculate the number of records
                int numLines = -1;
    
                while ((line = br.readLine()) != null) {
    
                    // Each record will contain 89 bytes on windows and 88 on linux
                    String[] record = line.split(cvsSplitBy);
    
                    dataFilePrinter.printf("%-5s", record[0]);
                    dataFilePrinter.printf("%-40s", record[1]);
                    dataFilePrinter.printf("%-20s", record[2]);
                    dataFilePrinter.printf("%-6s", record[3]);
                    dataFilePrinter.printf("%-6s", record[4]);
                    dataFilePrinter.printf("%-10s", record[5]);
                    dataFilePrinter.printf(System.getProperty("line.separator"));
    
                    numLines++;
                }
                dataFilePrinter.close();
    
                this.dbOps.updateNumRecords("normal", numLines);
                this.dbOps.updateNumRecords("overflow", 0);
                this.dbOps.updateHighestRank(numLines);
                createOverflowFile();
    
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    // Creating the config file for the database
    public void createConfigFile() {
        try {
            this.configFile = new File(this.databaseName + ".config");
            this.configFile.createNewFile();

            FileWriter configFileWriter = new FileWriter(this.configFile.toString());
            PrintWriter configFilePrinter = new PrintWriter(configFileWriter);

            configFilePrinter.printf("%-10s", "RANK,5");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-45s", "NAME,40");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-25s", "CITY,20");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-12s", "STATE,6");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-10s", "ZIP,6");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-20s", "EMPLOYEES,10");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-13s", "RECORDS,");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-18s", "OVERFLOW-RECORDS,");
            configFilePrinter.print(System.getProperty("line.separator"));
            configFilePrinter.printf("%-18s", "HIGHEST-RANK,");

            configFilePrinter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Creating the overflow file for the database
    public void createOverflowFile() {
        try {
            this.overflowFile = new File(this.databaseName + ".overflow");
            this.overflowFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Asking the user for the name of the database
    public void inputDatabaseName() {
        // Ask user to enter a file
        System.out.println("Please enter name of your .csv file without the extension: ");
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            this.databaseName = inputReader.readLine();
        } catch (IOException e) {
            System.out.println(e);
            this.validDatabaseName = false;
        }
    }
}