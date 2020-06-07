package obscurum.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {
    @Test
    public void givenPositiveNumbers_whenGettingNumberOfDigits_thenReturnsCorrectNumber() {
        assertEquals(1, Util.getNumberOfDigits(3));
        assertEquals(2, Util.getNumberOfDigits(14));
        assertEquals(3, Util.getNumberOfDigits(165));
        assertEquals(4, Util.getNumberOfDigits(1695));
        assertEquals(5, Util.getNumberOfDigits(12315));
        assertEquals(6, Util.getNumberOfDigits(208213));
        assertEquals(7, Util.getNumberOfDigits(5000000));
        assertEquals(8, Util.getNumberOfDigits(30932801));
        assertEquals(9, Util.getNumberOfDigits(560592013));
        assertEquals(10, Util.getNumberOfDigits(2059839100));
    }

    @Test
    public void givenNegativeNumbers_whenGettingNumberOfDigits_thenReturnsCorrectNumber() {
        assertEquals(1, Util.getNumberOfDigits(-3));
        assertEquals(2, Util.getNumberOfDigits(-64));
        assertEquals(3, Util.getNumberOfDigits(-201));
        assertEquals(4, Util.getNumberOfDigits(-9125));
        assertEquals(5, Util.getNumberOfDigits(-32115));
        assertEquals(6, Util.getNumberOfDigits(-208213));
        assertEquals(7, Util.getNumberOfDigits(-5553000));
        assertEquals(8, Util.getNumberOfDigits(-13092801));
        assertEquals(9, Util.getNumberOfDigits(-656059201));
        assertEquals(10, Util.getNumberOfDigits(-2059839130));
    }

    @Test
    public void givenZero_whenGettingNumberOfDigits_thenReturnsCorrectNumber() {
        assertEquals(1, Util.getNumberOfDigits(0));
    }

    @Test
    public void givenListWithElements_whenGettingRandomElement_thenReturnsElementFromList() {
        List<Integer> elements = Arrays.asList(10, 15, 23, 19, 1, 1, 8, 3, 2, 5, 34);

        for (int i = 0; i < 100; i++) {
            assertTrue(elements.contains(Util.pickRandomElement(elements)));
        }
    }

    @Test
    public void givenEmptyList_whenGettingRandomElement_thenReturnsNull() {
        assertNull(Util.pickRandomElement(Collections.emptyList()));
    }
}
