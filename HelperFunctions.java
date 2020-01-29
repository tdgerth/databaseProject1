import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HelperFunctions {
    public static String addWhitespacesToEnd(String input, int expectedLength) {
        return String.format("%-" + expectedLength + "s", input);
    }

    public static byte[] getInputDataBytes(int sizeOfArray) {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        byte [] userInputBytes = new byte[sizeOfArray];

        try {
            userInput = inputReader.readLine();

            userInput = HelperFunctions.addWhitespacesToEnd(userInput, sizeOfArray);

            for (int i = 0; i < sizeOfArray; i++) {
                userInputBytes[i] = (byte) userInput.charAt(i);
            }

            return userInputBytes;

        } catch (IOException e) {
            System.out.println(e);
        }

        return userInputBytes;
    }
}