import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;

public class CreateNewDatabase {

    public String fileName;

    public File overflowFile;
    public File dataFile;
    public File configFile;

    public byte[] currentRecord = new byte[89];
    
    CreateNewDatabase(String csvFileName) {
        this.fileName = csvFileName;

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            this.dataFile = new File(this.fileName + ".data");
            this.dataFile.createNewFile();

            FileWriter dataFileWriter = new FileWriter(this.dataFile.toString());
            PrintWriter dataFilePrinter = new PrintWriter(dataFileWriter);

            createConfigFile();

            br = new BufferedReader(new FileReader(this.fileName + ".csv"));

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

                // System.out.printf("%-5s", record[0]);
                // System.out.printf("%-40s", record[1]);
                // System.out.printf("%-20s", record[2]);
                // System.out.printf("%-6s", record[3]);
                // System.out.printf("%-6s", record[4]);
                // System.out.printf("%-10s", record[5]);
                // System.out.printf(System.getProperty("line.separator"));

                numLines++;
            }
            dataFilePrinter.close();

            updateNumRecords(numLines);
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

    // Creating the config file for the database
    public void createConfigFile() {
        try {
            this.configFile = new File(this.fileName + ".config");
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
            configFilePrinter.printf("%-13s", "RECORDS,0");

            configFilePrinter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Creating the overflow file for the database
    public void createOverflowFile() {
        try {
            this.overflowFile = new File(this.fileName + ".overflow");
            this.overflowFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Getting the number of records from the config file
    public String getNumberOfRecords() {
        // Reading in the number of records from the .config file starting from offset 142 bytes (where the number for RECORDS begins)
        FileInputStream fis = new FileInputStream(this.fileName + ".config");
        fis.getChannel().position(142);
        byte[] numRecords = new byte[5];

        fis.read(numRecords, 0, 5);
        fis.close();

        return new String(numRecords);
    }

    // Updating the number of records in the config file
    public void updateNumRecords(int numRecordsIn) {
        try {
            String recordsToString = getNumberOfRecords();

            // Updating the number of records
            System.out.println(recordsToString); 
            recordsToString = Integer.toString(numRecordsIn);            

            // Writing the updated number of records back to the config file
            FileOutputStream fos = new FileOutputStream(this.fileName + ".config");
            fos.getChannel().position(133);
            fos.write(recordsToString.getBytes());
            fos.close();
  
        } catch (IOException ex) {
            ex.printStackTrace();
        }  
    }
}