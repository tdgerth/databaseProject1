import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class DatabaseOperations {
    public String databaseName;

    DatabaseOperations(String databaseNameIn) {
        this.databaseName = databaseNameIn;
    }

    // Getting the number of records from the config file
    public String getNumberOfRecords(String recordType) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".config", "rws");

            if (recordType.equals("normal")) {
                // Reading in the number of normal records from the .config file starting from offset 142 bytes (where the number for RECORDS begins)
                raf.getChannel().position(142);
                byte[] numRecords = new byte[5];
        
                raf.read(numRecords, 0, 5);
                raf.close();
                return new String(numRecords).stripTrailing();
            } else {
                // Reading in the number of overflow records from the .config file starting from offset 166 (there the number for OVERFLOW-RECORDS begins)
                raf.getChannel().position(166);
                byte[] numOverflowRecords = new byte[1];
        
                raf.read(numOverflowRecords, 0, 1);
                raf.close();
                return new String(numOverflowRecords).stripTrailing();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error";
        }
    }

    public byte [] getRecordBytes(String recordType, int recordNumber) {
        try {
            RandomAccessFile recordFile;

            if (recordType == "overflow") {
                recordFile = new RandomAccessFile(this.databaseName + ".overflow", "rws");
            } else {
                recordFile = new RandomAccessFile(this.databaseName + ".data", "rws");
            }

            recordFile.getChannel().position(89 * recordNumber);
            byte [] record = new byte[89];
    
            recordFile.read(record, 0, 89);
            recordFile.close();
            return record;
        } catch (IOException ex) {
            ex.printStackTrace();
            return new byte [10];
        }
    }

    public String getRecordCompanyName(String recordType, int recordNumber) {
        try {
            RandomAccessFile recordFile;

            if (recordType == "overflow") {
                recordFile = new RandomAccessFile(this.databaseName + ".overflow", "rws");
            } else {
                recordFile = new RandomAccessFile(this.databaseName + ".data", "rws");
            }

            recordFile.getChannel().position((89 * recordNumber) + 5);
            byte [] record = new byte[40];
    
            recordFile.read(record, 0, 40);
            recordFile.close();
            return new String(record).stripTrailing();
        } catch (IOException ex) {
            ex.printStackTrace();
            return new String("Error");
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

    public void updateRecords(String recordType, byte [] record, int recordNumber) {
        try {
            // Writing the updated number of records back to the config file
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".data", "rws");

            if (recordType.equals("normal")) {
                raf.getChannel().position(89 * recordNumber);
                raf.write(record);
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

        /*Get record number n-th (from 1 to 4360) */
        //public static String getRecord(RandomAccessFile Din, int recordNum) throws IOException 
        // Provided by Dr. Gauch
        public String getRecord(RandomAccessFile Din, int recordNum) throws IOException 
        {
        String record = "NOT_FOUND";
        int NUM_RECORDS = Integer.parseInt(getNumberOfRecords("normal"));
            if ((recordNum >=1) && (recordNum <= NUM_RECORDS))
            {
                Din.seek(0); // return to the top fo the file
                Din.skipBytes(recordNum * 89);
                record = Din.readLine();
            }
            return record;
        }

        /*Binary Search record id */
        // Provided by Dr. Gauch
        public String binarySearch(RandomAccessFile Din, String id) throws IOException 
        {
        int Low = 0;
        int NUM_RECORDS = Integer.parseInt(getNumberOfRecords("normal"));;
        int High = NUM_RECORDS-1;
        int Middle;
        String MiddleId;
        String record = "";
        boolean Found = false;

            
        while (!Found && (High >= Low)) 
        {
            Middle = (High+Low) / 2;
            record = getRecord(Din, Middle+1);
            MiddleId = record.substring(5,45);
            MiddleId = MiddleId.trim();
            int result = MiddleId.compareTo(id);
            if (result == 0)   // ids match
                Found = true;
            else if (result < 0)
                Low = Middle + 1;
            else
                High = Middle - 1;
        }
        if (Found)
        return record;
        else
        return "NOT_FOUND";
    }

    // Either adding a new record to the overflow file or merging the overflow back into the normal records
    public void addRecord() {
        int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));

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
        companyNameBytes = HelperFunctions.getInputDataBytes(40);

        // Input for city the company is located in
        System.out.println("Please enter the city the company is located in (truncated at 20 characters): ");
        companyCityBytes = HelperFunctions.getInputDataBytes(20);

        // Input for state the company is located in
        System.out.println("Please enter the state the company is located in (truncated at 6 characters): ");
        companyStateBytes = HelperFunctions.getInputDataBytes(6);

        // Input for zip code of company
        System.out.println("Please enter the zip code of the company (truncated at 6 characters): ");
        companyZipCodeBytes = HelperFunctions.getInputDataBytes(6);

        // Input for number of employees of company
        System.out.println("Please enter the comapny's number of employees (truncated at 10 charactes): ");
        companyEmployeesBytes = HelperFunctions.getInputDataBytes(10);

        byte [] newLineBytes = System.getProperty("line.separator").getBytes();

        // End user input ************************************

        // Add bytes to overflow file
        addRecordToOverflow(companyRankBytes, numOverflowRecords * 89);
        addRecordToOverflow(companyNameBytes, numOverflowRecords * 89 + 5);
        addRecordToOverflow(companyCityBytes, numOverflowRecords * 89 + 45);
        addRecordToOverflow(companyStateBytes, numOverflowRecords * 89 + 65);
        addRecordToOverflow(companyZipCodeBytes, numOverflowRecords * 89 + 71);
        addRecordToOverflow(companyEmployeesBytes, numOverflowRecords * 89 + 77);
        addRecordToOverflow(newLineBytes, numOverflowRecords * 89 + 87);

        numOverflowRecords++;

        if (numOverflowRecords > 4) {
            mergeSortRecords(numOverflowRecords);
            updateNumRecords("overflow", 0);

            try {
                PrintWriter clearOverflow = new PrintWriter(this.databaseName + ".overflow");
                clearOverflow.write("");
                clearOverflow.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            updateNumRecords("overflow", numOverflowRecords);
        }

        String currentHighestRank = getHighestRank().stripTrailing();
        updateHighestRank(Integer.parseInt(currentHighestRank) + 1);

        System.out.println("Record Added...");
    }

    private void addRecordToOverflow(byte [] input, int startLocation) {
        try {
            // Writing a field to the overflow file
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".overflow", "rws");

            raf.getChannel().position(startLocation);
            raf.write(input);
            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Displays record that is found using binary search, and uses company name as the key
    public void displayRecord(String companyName) {
        try {
            RandomAccessFile din = new RandomAccessFile(this.databaseName + ".data", "rws");
            System.out.println(this.binarySearch(din, companyName.toUpperCase()));
            din.close();
            

        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }
    private void mergeSortRecords(int numOverflowRecords) {

        for (int i = 0; i < numOverflowRecords; i++) {
            byte [] overflowRecord = getRecordBytes("overflow", i);
            String overflowRecordCompanyName = getRecordCompanyName("overflow", i);

            // Find the closest record alphabetically so the overflow record can be merged
            int closest = binarySearchToFindClosest(overflowRecordCompanyName, 1, Integer.parseInt(getNumberOfRecords("normal")));

            if (overflowRecordCompanyName.compareTo(getRecordCompanyName("normal", closest)) < 1) {
                byte [] temp = getRecordBytes("normal", closest);
                updateRecords("normal", overflowRecord, closest);
                rearrangeRestOfDatabase(temp, closest + 1);
            } else {
                rearrangeRestOfDatabase(overflowRecord, closest + 1);
            }
        }
    }

    // Binary search to find the closest existing record alphabetically to the overflow record
    private int binarySearchToFindClosest(String overflowName, int bottomRecord, int topRecord) {
        if (bottomRecord == 0) {
            return 1;
        }
        // Base condition for only having two records left to choose from
        if (topRecord - bottomRecord == 1) {
            String topRecordName = getRecordCompanyName("normal", topRecord);

            // If the name of the overflow company comes after the company name of the top record, then the top record is the closest
            if (overflowName.compareTo(topRecordName) >= 1) {
                return topRecord;
            // Otherwise, either the top and bottom records are equal length away, or the bottom record is closer, so return the bottom record
            } else {
                return bottomRecord;
            }
        // Base condition for having only 1 record left means that that record is the closest
        } else if (topRecord == bottomRecord) {
            return topRecord;
        }

        int middle = (topRecord + bottomRecord) / 2;
        String nameOfNormalRecord = getRecordCompanyName("normal", middle);
        int alphabeticalOrder = overflowName.compareTo(nameOfNormalRecord);
        if (alphabeticalOrder == 0) {
            return middle;
        } else if (alphabeticalOrder < 0) {
            return binarySearchToFindClosest(overflowName, bottomRecord, middle);
        } else {
            return binarySearchToFindClosest(overflowName, middle + 1, topRecord);
        }        
    }

    private void rearrangeRestOfDatabase(byte[] startRecord, int start) {
        int totalRecords = Integer.parseInt(getNumberOfRecords("normal"));
        if (start == totalRecords) {
            updateRecords("normal", startRecord, start);
            updateNumRecords("normal", totalRecords);
        } else if (start >= 1 && start <= totalRecords + 1) {
            int i;
            for (i = start; i <= totalRecords; i++) {
                byte [] temp = getRecordBytes("normal", i);
                updateRecords("normal", startRecord, i);
                startRecord = temp;
            }
            updateRecords("normal", startRecord, i);
            totalRecords++;
            updateNumRecords("normal", totalRecords);
        }
    }
}