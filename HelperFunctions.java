import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HelperFunctions {
    public static String addWhitespacesToEnd(String input, int expectedLength) {
        return String.format("%-" + expectedLength + "s", input);
    }
}