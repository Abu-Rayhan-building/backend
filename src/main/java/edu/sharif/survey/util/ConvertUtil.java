package edu.sharif.survey.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertUtil {
    public static <T> Collection<T> emptyIfNull(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

    public static <T> T[] emptyIfNull(T[] array, Class<T> typeClass) {
        return array == null ? (T[]) Array.newInstance(typeClass, 0) : array;
    }
}
