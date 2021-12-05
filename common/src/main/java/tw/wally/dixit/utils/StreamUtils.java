package tw.wally.dixit.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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

    public static <T, R> Set<R> mapToSet(Collection<T> collection, Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(toSet());
    }

    public static <T, R> List<T> flatMapToList(Collection<R> collection, Function<? super R, ? extends Stream<? extends T>> flatMapping) {
        return collection.stream().flatMap(flatMapping::apply).collect(toList());
    }

    public static <T, K, U> Map<K, U> toMap(Collection<T> collection,
                                            Function<? super T, ? extends K> keyMapper,
                                            Function<T, U> valueMapper) {
        return collection.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <T> List<T> generate(int count, IntFunction<T> mapper) {
        return range(0, count).mapToObj(mapper).collect(toList());
    }

    public static <T> List<T> filterToList(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(toList());
    }

    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst();
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
