package tw.wally.dixit.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtils {

    public static <T, R> List<R> mapToList(T[] array, Function<T, R> mapper) {
        return mapToList(asList(array), mapper);
    }

    public static <T, R> List<R> mapToList(Collection<T> collection, Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(toList());
    }

    public static <T> List<T> generate(int count, IntFunction<T> mapper) {
        return range(0, count).mapToObj(mapper).collect(toList());
    }

    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(toList());
    }

    public static <T> List<T> limit(Collection<T> collection, int limit) {
        return collection.stream().limit(limit).collect(toList());
    }

    public static <T> List<T> skip(Collection<T> collection, int skip) {
        return collection.stream().skip(skip).collect(toList());
    }

    public static <T> boolean contains(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().anyMatch(predicate);
    }

    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).count();
    }

}
