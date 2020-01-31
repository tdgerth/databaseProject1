import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.lang.Object;

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
    public int binarySearch(RandomAccessFile Din, String id) throws IOException 
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
            return Middle;
        else if (result < 0)
            Low = Middle + 1;
        else
            High = Middle - 1;
    }
       return -1;
}




    // Either adding a new record to the overflow file or merging the overflow back into the normal records
    public void addRecord() {
        int numOverflowRecords = Integer.parseInt(getNumberOfRecords("overflow"));

        if (numOverflowRecords == 4) {
            System.out.println("Sort all records");
        } else {
            numOverflowRecords++;
            updateNumRecords("overflow", numOverflowRecords);

            String currentHighestRank = getHighestRank().stripTrailing();
            updateHighestRank(Integer.parseInt(currentHighestRank) + 1);

            System.out.println("Record Added...");
        }
    }
    // Displays record that is found using binary search, and uses company name as the key
    public void displayRecord(String companyName) {
        try {
            RandomAccessFile din = new RandomAccessFile(this.databaseName + ".data", "rws");
            int record = this.binarySearch(din, companyName.toUpperCase());
            if(record != -1){
                System.out.println(this.getRecord(din, record+1));
            } else {
                System.out.println("NOT FOUND");
            }
           
            din.close();
            

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    public void updateRecord(String companyName) {
        byte [] byteOption;
        String option = "";
        boolean quit = false;

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
 
         try {
            RandomAccessFile din = new RandomAccessFile(this.databaseName + ".data", "rws");
            int recordNumber = this.binarySearch(din, companyName.toUpperCase());
            System.out.println(recordNumber);
            if(recordNumber != -1){
                
            while(!quit){
                System.out.println("What would you like to change?");
                System.out.println("[1] Rank");
                System.out.println("[2] City");
                System.out.println("[3] State"); 
                System.out.println("[4] Zip Code");
                System.out.println("[5] Number of Employees");
                System.out.println("[6] done updating");
                option = inputReader.readLine();
            switch(option) {
                case "1":
                    System.out.println("Enter updated Rank");
                    option = inputReader.readLine();
                    option = String.format("%-5s", option);
                    din.getChannel().position((89 * (recordNumber+1)));
                    break;
                case "2":
                    System.out.println("Enter updated City");
                    option = inputReader.readLine();
                    option = String.format("%-20s", option);
                    din.getChannel().position((89 * (recordNumber+1))+45);
                    break;
                case "3":
                    System.out.println("Enter updated State Abbreviation");
                    option = inputReader.readLine();
                    option = String.format("%-6s", option);
                    din.getChannel().position((89 * (recordNumber+1))+65);
                    break;
                case "4":
                    System.out.println("Enter updated Zip Code");
                    option = inputReader.readLine();
                    option = String.format("%-6s", option);
                    din.getChannel().position((89 * (recordNumber+1))+71);
                    break;
                case "5":
                    System.out.println("Enter updated Number of Employees");
                    option = inputReader.readLine();
                    option = String.format("%-10s", option);
                    din.getChannel().position((89 * (recordNumber+1))+77);
                    break;
                case "6":
                    quit = true;
                    break;
                default:
                    System.out.println("That is not a valid option, please select a valid option");
                    break;
                
             }
            }
              byteOption = new byte[10];
              byteOption = option.getBytes();
              din.write(byteOption);
              din.close();   
            }  
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}