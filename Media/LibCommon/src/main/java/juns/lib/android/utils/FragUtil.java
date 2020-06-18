package juns.lib.android.utils;

/**
 * Fragment 加载相关操作
 *
 * @author Jun.Wang
 */
public class FragUtil {

    /**
     * Fragment 加载
     *
     * @param replaceId 替代View的ID
     * @param frag      目标Fragment
     * @param fm        FragmentManager
     */
    public static void loadV4Fragment(int replaceId, android.support.v4.app.Fragment frag,
                                      android.support.v4.app.FragmentManager fm) {
        if (fm != null) {
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.add(replaceId, frag);
            ft.commit();
        }
    }

    /**
     * Fragment 嵌套加载
     *
     * @param replaceId 替代View的ID
     * @param frag      目标Fragment
     * @param fm        FragmentManager
     */
    public static void loadV4ChildFragment(int replaceId,
                                           android.support.v4.app.Fragment frag,
                                           android.support.v4.app.FragmentManager fm) {
        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(replaceId, frag);
        transaction.commit();
    }

    /**
     * Load V4 Fragment
     */
    public static void removeV4Fragment(android.support.v4.app.Fragment frag,
                                        android.support.v4.app.FragmentManager fm) {
        if (fm != null) {
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.remove(frag);
            ft.commitAllowingStateLoss();
        }
    }
}
