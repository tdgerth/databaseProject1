
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

public class DatabaseOperations {
    public String databaseName;

    DatabaseOperations(String databaseNameIn) {
        this.databaseName = databaseNameIn;
    }

    // ********************* GETTERS ***********************************************************

    // Getting the number of records from the config file
    public String getNumberOfRecords(String recordType) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".config", "rws");

            if (recordType.equals("normal")) {
                // Reading in the number of normal records from the .config file starting from offset 142 bytes (where the number for RECORDS begins)
                raf.getChannel().position(64);
                byte[] numRecords = new byte[5];
        
                raf.read(numRecords, 0, 5);
                raf.close();
                return new String(numRecords).trim();
            } else {
                // Reading in the number of overflow records from the .config file starting from offset 166 (there the number for OVERFLOW-RECORDS begins)
                raf.getChannel().position(88);
                byte[] numOverflowRecords = new byte[1];
        
                raf.read(numOverflowRecords, 0, 1);
                raf.close();
                return new String(numOverflowRecords).trim();
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

            recordFile.getChannel().position(Constants.NUM_BYTES_LINUX_RECORD * recordNumber);
            byte [] record = new byte[Constants.NUM_BYTES_LINUX_RECORD];
    
            recordFile.read(record, 0, Constants.NUM_BYTES_LINUX_RECORD);
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

            recordFile.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * recordNumber) + 5);
            byte [] record = new byte[40];
    
            recordFile.read(record, 0, 40);
            recordFile.close();
            return new String(record).trim();
        } catch (IOException ex) {
            ex.printStackTrace();
            return new String("Error");
        }
    }

    // Retrieving the highest overall rank so it can be incremented for new records
    public String getHighestRank() {
        try {
            FileInputStream fis = new FileInputStream(this.databaseName + ".config");

            // Reading in the number of normal records from the .config file starting from offset 104 bytes (where the number for HIGHEST-RANK begins)
            fis.getChannel().position(104);
            byte[] highestRank = new byte[5];
    
            fis.read(highestRank, 0, 5);
            fis.close();
            return new String(highestRank).trim();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error";
        }
    }

    /*Get record number n-th (from 1 to 4360) */
    // Provided by Dr. Gauch
    public String getRecord(String recordType, RandomAccessFile Din, int recordNum) throws IOException 
    {
        String record = "NOT_FOUND";
        int NUM_RECORDS = Integer.parseInt(getNumberOfRecords(recordType));
        if ((recordNum >=0) && (recordNum <= NUM_RECORDS - 1))
        {
            Din.seek(0); // return to the top fo the file
            Din.skipBytes(Constants.NUM_BYTES_LINUX_RECORD * recordNum);
            record = Din.readLine();
        }
        return record;
    }

    // ********************** END GETTERS ************************************************************



    // ********************** UPDATERS ***************************************************************

    // Updating the number of records in the config file
    public void updateNumRecords(String recordType, int numRecordsIn) {
        try {
            // Writing the updated number of records back to the config file
            RandomAccessFile raf = new RandomAccessFile(this.databaseName + ".config", "rws");

            if (recordType.equals("normal")) {
                String currentNumRecords = HelperFunctions.addWhitespacesToEnd(Integer.toString(numRecordsIn), 5);

                raf.getChannel().position(64);
                raf.write(currentNumRecords.getBytes());
                raf.close();
            } else {
                String currentNumRecords = Integer.toString(numRecordsIn);
                raf.getChannel().position(88);
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
                raf.getChannel().position(Constants.NUM_BYTES_LINUX_RECORD * recordNumber);
                raf.write(record);
                raf.close();
            }
  
        } catch (IOException ex) {
            ex.printStackTrace();
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

            raf.getChannel().position(104);
            raf.write(highestRank);
            raf.close();
    
        } catch (IOException ex) {
            ex.printStackTrace();
        }  
    }

    //update all parts of record except for key
    public void updateRecord(String companyName) {
        String option = "";
        boolean quit = false;
        byte [] numEmplyees = new byte[Constants.NUM_BYTES_COMPANY_EMPLOYEES];
        byte [] cityName = new byte[Constants.NUM_BYTES_COMPANY_CITY];
        byte [] rank = new byte[Constants.NUM_BYTES_RANK];
        byte [] state = new byte[Constants.NUM_BYTES_COMPANY_STATE];
        byte [] zip = new byte[Constants.NUM_BYTES_COMPANY_ZIP];

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
 
         try {
            RandomAccessFile din = new RandomAccessFile(this.databaseName + ".data", "rws");
            RandomAccessFile oin = new RandomAccessFile(this.databaseName + ".overflow", "rws");

            //find company to update
            String recordLocation = "normal";
            int recordNumber = this.binarySearch(din, companyName.toUpperCase());

            if (recordNumber == -1) {
                int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));

                for (int i = 0; i < numOverflowRecords; i++) {
                    String record = getRecord("overflow", oin, i);
                    String recordName = record.substring(5,45);
                    recordName = recordName.trim();

                    if (companyName.toUpperCase().equals(recordName)) {
                        recordNumber = i;
                        recordLocation = "overflow";
                        break;
                    }
                }
            }

            //if company found
            if(recordNumber != -1){
                //untill the user wants to stop updating
                while(!quit){
                    System.out.println("What would you like to change?");
                    System.out.println("[1] Rank");
                    System.out.println("[2] City");
                    System.out.println("[3] State"); 
                    System.out.println("[4] Zip Code");
                    System.out.println("[5] Number of Employees");
                    System.out.println("[6] done updating");
                    option = inputReader.readLine();

                    byte [] update;
                    switch(option) {
                        case "1":
                            System.out.println("Enter updated Rank");
                            update = HelperFunctions.getInputDataBytes(5);
                            // option = inputReader.readLine();
                            //format input to fixed length
                            
                            // option = String.format("%-5s", option.toUpperCase());
                            // rank = option.getBytes();
                            if (recordLocation.equals("normal")) {
                                din.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber)));
                                //replace current rank with new rank
                                din.write(update);     
                            } else {
                                oin.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber)));
                                //replace current rank with new rank
                                oin.write(update); 
                            }
                            break;
                        case "2":
                            System.out.println("Enter updated City");
                            update = HelperFunctions.getInputDataBytes(20);
                            // option = inputReader.readLine();
                            // option = String.format("%-20s", option.toUpperCase());
                            // cityName = option.getBytes();
                            if (recordLocation.equals("normal")) {
                                din.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+45);
                                din.write(update);   
                            } else {
                                oin.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+45);
                                oin.write(update);   
                            }                       
                            break;
                        case "3":
                            System.out.println("Enter updated State Abbreviation");
                            // option = inputReader.readLine();
                            // option = String.format("%-3s", option.toUpperCase());
                            // state = option.getBytes();
                            update = HelperFunctions.getInputDataBytes(3);
                            if (recordLocation.equals("normal")) {
                                din.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+65);
                                din.write(update);
                            } else {
                                oin.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+65);
                                oin.write(update);
                            }
                            break;
                        case "4":
                            System.out.println("Enter updated Zip Code");
                            // option = inputReader.readLine();
                            // option = String.format("%-6s", option.toUpperCase());
                            // zip = option.getBytes();
                            update = HelperFunctions.getInputDataBytes(6);
                            if (recordLocation.equals("normal")) {
                                din.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+68);
                                din.write(update);
                            } else {
                                oin.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+68);
                                oin.write(update);
                            }
                            break;
                        case "5":
                            System.out.println("Enter updated Number of Employees");
                            // option = inputReader.readLine();
                            // option = String.format("%-10s", option.toUpperCase());
                            // numEmplyees = option.getBytes();
                            update = HelperFunctions.getInputDataBytes(10);
                            if (recordLocation.equals("normal")) {
                                din.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+74);
                                din.write(update);
                            } else {
                                oin.getChannel().position((Constants.NUM_BYTES_LINUX_RECORD * (recordNumber))+74);
                                oin.write(update);   
                            }
                            break;
                        case "6":
                            quit = true;
                            din.close();
                            oin.close();
                            break;
                        default:
                            System.out.println("That is not a valid option, please select a valid option");
                            break;    
                    }
                }
            }
            //if not found, let the user know
            else{
                System.out.println("NOT FOUND");
                return;
            }  
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ************************************ END UPDATERS *******************************************************************************************

    // ************************************ BINARY SEARCHES ***************************************************************************************

    /*Binary Search record id */
    // Provided by Dr. Gauch
    public int binarySearch(RandomAccessFile Din, String id) throws IOException 
    {
        int Low = 0;
        int NUM_RECORDS = Integer.parseInt(getNumberOfRecords("normal"));;
        int High = NUM_RECORDS-1;
        int Middle = 0;
        String MiddleId;
        String record = "";
        boolean Found = false;

            
        while (!Found && (High >= Low)) 
        {
            Middle = (High+Low) / 2;
            record = getRecord("normal", Din, Middle+1);
            MiddleId = record.substring(5,45);
            MiddleId = MiddleId.trim();
            int result = MiddleId.compareTo(id);

            if (result == 0) {
                // ids match
                return Middle;
            } 
            else if (result < 0) {
                Low = Middle + 1;
            }
            else {
                High = Middle - 1;
            }
        }

        if (Low > High) {
            record = getRecord("normal", Din, Middle);
            // if (record != "") {
                MiddleId = record.substring(5, 45).trim().toUpperCase();
                if (MiddleId.compareTo(id) == 0) {
                    return Middle;
                }
            // }
        }

        return -1;
    }

    // Binary search to find the closest existing record alphabetically to the overflow record
    private int binarySearchToFindClosest(String overflowName, int bottomRecord, int topRecord) {
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

    // ***************************** END BINARY SEARCHES *******************************************************************************


    // Deleting a record by changing its primary key to missing - the record itself will be removed during merges
    public void deleteRecord() {
        try {
            System.out.println("Please enter the name of the company you wish to delete");
            String companyName = HelperFunctions.getInputDataString();
            RandomAccessFile rafData = new RandomAccessFile(this.databaseName + ".data", "rws");
            int deleteLocation = binarySearch(rafData, companyName);

            if (deleteLocation != -1) {

                deleteLocation = binarySearchToFindClosest(companyName, 0, Integer.parseInt(getNumberOfRecords("normal")));
                // if (deleteLocation == 0) {
                    rafData.getChannel().position(Constants.NUM_BYTES_LINUX_RECORD * deleteLocation + Constants.NUM_BYTES_RANK);
                // } else {
                //     rafData.getChannel().position(Constants.NUM_BYTES_LINUX_RECORD * (deleteLocation + 1) + Constants.NUM_BYTES_RANK);
                // }

                rafData.write(HelperFunctions.addWhitespacesToEnd("MISSING-RECORD", Constants.NUM_BYTES_COMPANY_NAME).getBytes(), 0, Constants.NUM_BYTES_COMPANY_NAME);
                rafData.close();

                System.out.println("Deleting Record...");

                // Have to remove the record entirely on a delete, otherwise it will mess up binary search by not being sorted with the rest 
                removeDeletedRecord();
                updateNumRecords("normal", Integer.parseInt(getNumberOfRecords("normal")) - 1);

            // Check overflow file if not found in normal data file
            } else {
                RandomAccessFile rafOverflow = new RandomAccessFile(this.databaseName + ".overflow", "rws");
                boolean recordInOverflow = false;
                int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));
                int recordPosition = -1;

                for (int i = 0; i < numOverflowRecords; i++) {
                    String record = getRecord("overflow", rafOverflow, i);
                    String recordName = record.substring(5,45);
                    recordName = recordName.trim();

                    if (companyName.equals(recordName)) {
                        recordInOverflow = true;
                        recordPosition = i;
                        break;
                    }
                }

                // If found in overflow file, set that company name to MISSING-RECORD
                if (recordInOverflow == true) {
                    rafOverflow.getChannel().position(Constants.NUM_BYTES_LINUX_RECORD * recordPosition + Constants.NUM_BYTES_RANK);
                    rafOverflow.write(HelperFunctions.addWhitespacesToEnd("MISSING-RECORD", Constants.NUM_BYTES_COMPANY_NAME).getBytes(), 0, Constants.NUM_BYTES_COMPANY_NAME);
                    rafOverflow.close();
                    System.out.println("Deleting Record...");
                } else {
                    System.out.println("That record does not exist in this database");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Either adding a new record to the overflow file or merging the overflow back into the normal records
    public void addRecord() {
        int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));

        int rankForNewRecord = Integer.parseInt(getHighestRank()) + 1;

        byte [] companyNameBytes = new byte[Constants.NUM_BYTES_COMPANY_NAME];
        byte [] companyCityBytes = new byte[Constants.NUM_BYTES_COMPANY_CITY];
        byte [] companyStateBytes = new byte[Constants.NUM_BYTES_COMPANY_STATE];
        byte [] companyZipCodeBytes = new byte[Constants.NUM_BYTES_COMPANY_ZIP];
        byte [] companyEmployeesBytes = new byte[Constants.NUM_BYTES_COMPANY_EMPLOYEES];

        // User input for each of the fields ******************
        byte [] companyRankBytes = HelperFunctions.addWhitespacesToEnd(Integer.toString(rankForNewRecord), Constants.NUM_BYTES_RANK).getBytes();

        // Input for name of company
        System.out.println("Please enter the name of the company (truncated at " + Constants.NUM_BYTES_COMPANY_NAME + " characters): ");
        companyNameBytes = HelperFunctions.getInputDataBytes(Constants.NUM_BYTES_COMPANY_NAME);

        // Input for city the company is located in
        System.out.println("Please enter the city the company is located in (truncated at " + Constants.NUM_BYTES_COMPANY_CITY + " characters): ");
        companyCityBytes = HelperFunctions.getInputDataBytes(Constants.NUM_BYTES_COMPANY_CITY);

        // Input for state the company is located in
        System.out.println("Please enter the state the company is located in (truncated at " + Constants.NUM_BYTES_COMPANY_STATE + " characters): ");
        companyStateBytes = HelperFunctions.getInputDataBytes(Constants.NUM_BYTES_COMPANY_STATE);

        // Input for zip code of company
        System.out.println("Please enter the zip code of the company (truncated at " + Constants.NUM_BYTES_COMPANY_ZIP + " characters): ");
        companyZipCodeBytes = HelperFunctions.getInputDataBytes(Constants.NUM_BYTES_COMPANY_ZIP);

        // Input for number of employees of company
        System.out.println("Please enter the company's number of employees (truncated at " + Constants.NUM_BYTES_COMPANY_EMPLOYEES + " charactes): ");
        companyEmployeesBytes = HelperFunctions.getInputDataBytes(Constants.NUM_BYTES_COMPANY_EMPLOYEES);

        byte [] newLineBytes = System.getProperty("line.separator").getBytes();

        // End user input ************************************

        // Add bytes to overflow file
        addRecordToOverflow(companyRankBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD);
        addRecordToOverflow(companyNameBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD + 5);
        addRecordToOverflow(companyCityBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD + 45);
        addRecordToOverflow(companyStateBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD + 65);
        addRecordToOverflow(companyZipCodeBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD + 68);
        addRecordToOverflow(companyEmployeesBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD + 74);
        addRecordToOverflow(newLineBytes, numOverflowRecords * Constants.NUM_BYTES_LINUX_RECORD + 84);

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

        String currentHighestRank = getHighestRank().trim();
        updateHighestRank(Integer.parseInt(currentHighestRank) + 1);

        System.out.println("Record Added...");
    }

    // Writing a record into the overflow file
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
            RandomAccessFile oin = new RandomAccessFile(this.databaseName + ".overflow", "rws");
            int record = this.binarySearch(din, companyName.trim().toUpperCase());
            String recordLocation = "normal";

            if (record == -1) {
                int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));

                for (int i = 0; i < numOverflowRecords; i++) {
                    String overflowRecord = getRecord("overflow", oin, i);
                    String recordName = overflowRecord.substring(5,45);
                    recordName = recordName.trim();

                    if (companyName.toUpperCase().equals(recordName)) {
                        record = i;
                        recordLocation = "overflow";
                        break;
                    }
                }
            }

            //if company is found display the company
            if(record != -1){
                if (recordLocation.equals("normal")) {
                    if (record == 0) {
                        System.out.println(HelperFunctions.displayReadableRecord(this.getRecord("normal", din, record)));
                    } else {
                        System.out.println(HelperFunctions.displayReadableRecord(this.getRecord("normal", din, record + 1)));
                    }
                } else {
                    System.out.println(HelperFunctions.displayReadableRecord(this.getRecord("overflow", oin, record)));
                }
            } 
            //if not, let the user know
            else {
                System.out.println("NOT FOUND");
            }
            
            din.close();
            oin.close();
            

        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }

    // Finding where the overflow records go in the database and adding them in
    private void mergeSortRecords(int numOverflowRecords) {

        for (int i = 0; i < numOverflowRecords; i++) {
            byte [] overflowRecord = getRecordBytes("overflow", i);
            String overflowRecordCompanyName = getRecordCompanyName("overflow", i);

            if (!overflowRecordCompanyName.equals("MISSING-RECORD")) {
                // Find the closest record alphabetically so the overflow record can be merged
                int closest = binarySearchToFindClosest(overflowRecordCompanyName, 0, Integer.parseInt(getNumberOfRecords("normal")) - 1);
    
                if (overflowRecordCompanyName.compareTo(getRecordCompanyName("normal", closest)) < 1) {
                    byte [] temp = getRecordBytes("normal", closest);
                    updateRecords("normal", overflowRecord, closest);
                    rearrangeRestOfDatabase(temp, closest + 1);
                } else {
                    rearrangeRestOfDatabase(overflowRecord, closest + 1);
                }
            }
        }
    }

    // Linearly shifting the rest of the database down 1 once the insert is done
    private void rearrangeRestOfDatabase(byte[] startRecord, int start) {
        int totalRecords = Integer.parseInt(getNumberOfRecords("normal"));
        if (start == totalRecords) {
            updateRecords("normal", startRecord, start);
            updateNumRecords("normal", totalRecords + 1);
        } else if (start >= 0 && start <= totalRecords + 1) {
            int i;
            for (i = start; i <= totalRecords; i++) {
                byte [] temp = getRecordBytes("normal", i);
                updateRecords("normal", startRecord, i);
                startRecord = temp;
            }
            totalRecords++;
            updateNumRecords("normal", totalRecords);
        }
    }

    private void removeDeletedRecord() {
        try {
            File oldDB = new File(this.databaseName + ".data");
            File newDB = new File("temp.data");
            RandomAccessFile rafTemp = new RandomAccessFile(newDB, "rws");
            RandomAccessFile rafData = new RandomAccessFile(oldDB, "rws");
            int totalRecords = Integer.parseInt(getNumberOfRecords("normal"));

            int tempLineToWriteTo = 0;
    
            for (int i = 0; i < totalRecords; i++) {
                byte [] dataRecord = getRecordBytes("normal", i);
                if (!getRecordCompanyName("normal", i).equals("MISSING-RECORD")) {
                    rafTemp.getChannel().position(Constants.NUM_BYTES_LINUX_RECORD * tempLineToWriteTo);
                    rafTemp.write(dataRecord, 0, Constants.NUM_BYTES_LINUX_RECORD);
                } else {
                    tempLineToWriteTo--;
                }
                tempLineToWriteTo++;
            }
            rafData.close();
            oldDB.delete();
            rafTemp.close();

            File file = new File(this.databaseName + ".data");

            newDB.renameTo(file);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void createReport(){
        int numRecords = Integer.parseInt(getNumberOfRecords("normal"));

        try {
            BufferedReader br = new BufferedReader(new FileReader(this.databaseName + ".data"));
            FileWriter report = new FileWriter( "report.txt");

            if (numRecords < 10) {
                for(int i = 1; i < numRecords ; i++) {
                    report.write(HelperFunctions.displayReadableRecord(br.readLine()) + System.getProperty("line.separator"));
                } 
            } else {
                for(int i = 1; i < 11 ; i++) {
                    report.write(HelperFunctions.displayReadableRecord(br.readLine()) + System.getProperty("line.separator"));
                }  
            }
            report.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("report.txt created");
    }
}