import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class DatabaseOperations {
    public String databaseName;

    DatabaseOperations(String databaseNameIn) {
        this.databaseName = databaseNameIn;
    }

    // Getting the number of records from the config file
    public String getNumberOfRecords(String recordType) {
        try {
            FileInputStream fis = new FileInputStream(this.databaseName + ".config");

            if (recordType.equals("normal")) {
                // Reading in the number of normal records from the .config file starting from offset 142 bytes (where the number for RECORDS begins)
                fis.getChannel().position(142);
                byte[] numRecords = new byte[5];
        
                fis.read(numRecords, 0, 5);
                fis.close();
                return new String(numRecords).stripTrailing();
            } else {
                // Reading in the number of overflow records from the .config file starting from offset 166 (there the number for OVERFLOW-RECORDS begins)
                fis.getChannel().position(166);
                byte[] numOverflowRecords = new byte[1];
        
                fis.read(numOverflowRecords, 0, 1);
                fis.close();
                return new String(numOverflowRecords).stripTrailing();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error";
        }
    }

    // Updating the number of records in the config file
    public void updateNumRecords(String recordType, int numRecordsIn) {
        try {
            // Writing the updated number of records back to the config file
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".config", "rws");

            if (recordType.equals("normal")) {
                String currentNumRecords = HelperFunctions.addWhitespacesToEnd(Integer.toString(numRecordsIn), 5);

                raf.getChannel().position(142);
                raf.write(currentNumRecords.getBytes());
                raf.close();
            } else {
                String currentNumRecords = Integer.toString(numRecordsIn);
                raf.getChannel().position(166);
                raf.write(currentNumRecords.getBytes());
                raf.close();
            }
  
        } catch (IOException ex) {
            ex.printStackTrace();
        }  
    }

    // Retrieving the highest overall rank so it can be incremented for new records
    public String getHighestRank() {
        try {
            FileInputStream fis = new FileInputStream(this.databaseName + ".config");

            // Reading in the number of normal records from the .config file starting from offset 182 bytes (where the number for HIGHEST-RANK begins)
            fis.getChannel().position(182);
            byte[] highestRank = new byte[5];
    
            fis.read(highestRank, 0, 5);
            fis.close();
            return new String(highestRank).stripTrailing();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error";
        }
    }

    // Updating the highest rank so ranks aren't duplicated when records are deleted
    public void updateHighestRank(int newHighestRank) {
        try {
            byte[] highestRank = new byte[5];

            // Updating the number of records (adding whitespace to end, so thee entire byte array is filled)
            String newHighestRankString = HelperFunctions.addWhitespacesToEnd(Integer.toString(newHighestRank), 5);

            byte [] newHighestRankBytes = newHighestRankString.getBytes();

            for (int i = 0; i < newHighestRankBytes.length; i++) {
                // Truncating if the characters for newHighestRankString exceed the allocated bytes for HIGHEST-RANK
                if (i == 5) {
                    break;
                }
                highestRank[i] = newHighestRankBytes[i];
            }

            // Writing the updated number of records back to the config file
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".config", "rws");

            raf.getChannel().position(182);
            raf.write(highestRank);
            raf.close();
  
        } catch (IOException ex) {
            ex.printStackTrace();
        }  
    }

    // Either adding a new record to the overflow file or merging the overflow back into the normal records
    public void addRecord() {
        int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));

        if (numOverflowRecords == 4) {
            System.out.println("Sort all records");
        } else {

            System.out.println(getHighestRank());

            int rankForNewRecord = Integer.parseInt(getHighestRank()) + 1;

            byte [] companyNameBytes = new byte[40];
            byte [] companyCityBytes = new byte[20];
            byte [] companyStateBytes = new byte[6];
            byte [] companyZipCodeBytes = new byte[6];
            byte [] companyEmployeesBytes = new byte[10];

            // User input for each of the fields ******************

            byte [] companyRankBytes = HelperFunctions.addWhitespacesToEnd(Integer.toString(rankForNewRecord), 5).getBytes();

            // Input for name of company
            System.out.println("Please enter the name of the company (truncated at 40 characters): ");
            companyNameBytes = HelperFunctions.getInputData(40);

            // Input for city the company is located in
            System.out.println("Please enter the city the company is located in (truncated at 20 characters): ");
            companyCityBytes = HelperFunctions.getInputData(20);

            // Input for state the company is located in
            System.out.println("Please enter the state the company is located in (truncated at 6 characters): ");
            companyStateBytes = HelperFunctions.getInputData(6);

            // Input for zip code of company
            System.out.println("Please enter the zip code of the company (truncated at 6 characters): ");
            companyZipCodeBytes = HelperFunctions.getInputData(6);

            // Input for number of employees of company
            System.out.println("Please enter the comapny's number of employees (truncated at 10 charactes): ");
            companyEmployeesBytes = HelperFunctions.getInputData(10);

            byte [] newLineBytes = new String("\n").getBytes();

            // End user input ************************************

            // Add bytes to overflow file
            addRecordToOverflow(companyRankBytes, numOverflowRecords * 88);
            addRecordToOverflow(companyNameBytes, numOverflowRecords * 88 + 5);
            addRecordToOverflow(companyCityBytes, numOverflowRecords * 88 + 45);
            addRecordToOverflow(companyStateBytes, numOverflowRecords * 88 + 65);
            addRecordToOverflow(companyZipCodeBytes, numOverflowRecords * 88 + 71);
            addRecordToOverflow(companyEmployeesBytes, numOverflowRecords * 88 + 77);
            addRecordToOverflow(newLineBytes, numOverflowRecords * 88 + 87);

            numOverflowRecords++;
            updateNumRecords("overflow", numOverflowRecords);

            String currentHighestRank = getHighestRank().stripTrailing();
            updateHighestRank(Integer.parseInt(currentHighestRank) + 1);

            System.out.println("Record Added...");
        }
    }

    private void addRecordToOverflow(byte [] input, int startLocation) {
        try {
            // Writing a field to the overflow file
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".overflow", "rws");

            raf.getChannel().position(startLocation);
            raf.write(input);
            raf.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}