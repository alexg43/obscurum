package obscurum.util;

import java.util.List;
import java.util.Random;

public class Util {
    private static final Random random = new Random();

    public static int getNumberOfDigits(long n) {
        int numberOfDigits = 0;

        do {
            numberOfDigits++;
            n /= 10;
        } while (n != 0);

        return numberOfDigits;
    }

    public static <E> E pickRandomElement(List<E> list) {
        return list.isEmpty() ? null : list.get(random.nextInt(list.size()));
    }
}
