import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ApplicationWrapper {

    private boolean quit = false;
    private String currentDatabase;
    private DatabaseOperations dbOps;

    public void startApplication() {
        System.out.println("Welcome to the database application!");

        while (!quit) {
            String selectedOption = selectOptions();

            switch (selectedOption) {
                case "1":
                    CreateNewDatabase newDb = new CreateNewDatabase();
                    if (newDb.validDatabaseName != true) {
                        System.out.println("Invalid database file. Please try again!");
                    }
                    break;
                case "2":
                    if  (this.currentDatabase != null) {
                        System.out.println("Please close the existing database '" + this.currentDatabase + "' first.");
                    } else {
                        ArrayList<String> databases = checkForDatabases();
                        if (databases.size() == 0) {
                            System.out.println("There are no existing databases. Please create one.");
                        } else {
                            System.out.println("The existing databases are listed below. Please enter which one you would like to open");
                            for (String db : databases) {
                                System.out.println("\t" + db);
                            }
                        }

                        inputDatabaseToOpen(databases);
                        System.out.println("Opening database...");
                    }
                    break;
                case "3":
                    System.out.println("Close");
                    if (this.currentDatabase == null) {
                        System.out.println("There is no database currently open.");
                    } else {
                        System.out.println("Closing database...");
                        this.currentDatabase = null;
                    }
                    break;
                case "4":
                    System.out.println("Display");
                    System.out.println("Please enter the name of the company you want to display. ");
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

                    String companyName = "";

                    try {
                        companyName = inputReader.readLine();
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }

                    dbOps.displayRecord(companyName);
                    break;
                case "5":
                    System.out.println("Update");
                    break;
                case "6":
                    System.out.println("Report");
                    break;
                case "7":
                    System.out.println("Add");
                    if (this.currentDatabase == null) {
                        System.out.println("Please open a database first to add the record to.");
                    } else {
                        dbOps.addRecord();
                    }
                    break;
                case "8":
                    System.out.println("Delete");
                    break;
                case "q":
                    this.quit = true;
                    break;
                default:
                    System.out.println("Invalid option selected. Please try again!");
                    break;
            }
        }
    }

    private String selectOptions() {
        System.out.println(System.getProperty("line.separator") + "Please enter which operation number you would like to complete (or enter 'q' to quit):" + System.getProperty("line.separator"));
        System.out.println("\t[1] Create Database");
        System.out.println("\t[2] Open Database");
        System.out.println("\t[3] Close Database");
        System.out.println("\t[4] Display Record");
        System.out.println("\t[5] Update Record");
        System.out.println("\t[6] Create Report");
        System.out.println("\t[7] Add Record");
        System.out.println("\t[8] Delete Record" + System.getProperty("line.separator"));
        System.out.print("Selection: ");

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        String selectedOption = "";

        try {
            selectedOption = inputReader.readLine();
        } catch (IOException ex) {
            System.out.println(ex);
        }

        return selectedOption;
    }

    private ArrayList<String> checkForDatabases() {
        File dir = new File(".");

        File[] files = dir.listFiles(new FilenameFilter(){
        
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".data");
            }
        });

        ArrayList<String> databases = new ArrayList<>();

        for (File file : files) {
            String fileNameNoExtension = file.getName().substring(0, file.getName().length() - 5);

            File checkConfigFile = new File(fileNameNoExtension + ".config");
            File checkOverflowFile = new File(fileNameNoExtension + ".overflow");

            if (checkConfigFile.exists() && checkOverflowFile.exists()) {
                databases.add(fileNameNoExtension);
            }
        }

        return databases;
    }

    private void inputDatabaseToOpen(ArrayList<String> validDatabases) {
        // Ask user to enter a database
        System.out.print("Database Selection: ");
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String databaseSelection = inputReader.readLine();

            for (String db : validDatabases) {
                if (databaseSelection.equals(db)) {
                    this.currentDatabase = db;
                    this.dbOps = new DatabaseOperations(db);
                    break;
                }
            }

            if (this.currentDatabase == null) {
                System.out.println("Invalid database selection");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}