package juns.lib.java.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Empty Check Common methods
 *
 * @author Jun.Wang
 */
public class EmptyUtil {
    /**
     * Check whether [the String Object] is empty
     * <p>
     * Like : null，" "，""
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str) || "".equals(str.trim());
    }

    /**
     * Check whether [the Collection Object] is empty
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.size() == 0;
    }

    /**
     * Check whether [the List Object] is empty
     */
    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }

    /**
     * Check whether [the Map Object] is empty
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.size() == 0;
    }

    /**
     * Check whether [the Set Object] is empty
     */
    public static <T> boolean isEmpty(Set<T> set) {
        return set == null || set.size() == 0;
    }

    /**
     * Check whether [the Array Object] is empty
     */
    public static <T> boolean isEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }
}
