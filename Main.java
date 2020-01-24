public class Main {
    public static void main(String[] args) {
        DatabaseOperations dbOps = new DatabaseOperations();

        String database = dbOps.openDatabase();
        System.out.println(database);
    }
}