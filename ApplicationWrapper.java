import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ApplicationWrapper {

    private boolean quit = false;
    private String currentDatabase;

    public void startApplication() {
        System.out.println("Welcome to the database application!");

        while (!quit) {
            String selectedOption = selectOptions();

            switch (selectedOption) {
                case "1":
                    System.out.println("Create");
                    break;
                case "2":
                    System.out.println("Open");
                    break;
                case "3":
                    System.out.println("Close");
                    break;
                case "4":
                    System.out.println("Display");
                    break;
                case "5":
                    System.out.println("Update");
                    break;
                case "6":
                    System.out.println("Report");
                    break;
                case "7":
                    System.out.println("Add");
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
        System.out.println("Please enter which operation number you would like to complete (or enter 'q' to quit):" + System.getProperty("line.separator"));
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
}