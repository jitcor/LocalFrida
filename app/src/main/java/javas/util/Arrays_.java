package javas.util;

import java9.util.Spliterator;
import java9.util.Spliterators;
import java9.util.stream.Stream;

import java9.util.stream.StreamSupport;

public class Arrays_ {
    public static <T> Stream<T> stream(T[] array) {
        return stream(array, 0, array.length);
    }
    public static <T> Stream<T> stream(T[] array, int startInclusive, int endExclusive) {
        return StreamSupport.stream(spliterator(array, startInclusive, endExclusive), false);
    }
    public static <T> Spliterator<T> spliterator(T[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }
}
