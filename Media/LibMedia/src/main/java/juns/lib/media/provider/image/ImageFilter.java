package juns.lib.media.provider.image;

/**
 * 图片过滤器
 */
public class ImageFilter {
    //媒体格式后缀
    private static final String[] QUALCOM_FORMATS = new String[]{
            ".jpg"
            , ".jpeg"
            , ".png"
            , ".bmp"
            , ".gif"
    };

    /**
     * 是否支持的格式
     * <p>比较文件后缀是否在支持的范围内</p>
     */
    public static boolean isSupport(String mediaUrl) {
        boolean isSupport = false;
        if (mediaUrl != null) {
            String mediaUrlLowerCase = mediaUrl.toLowerCase();
            for (String suffix : QUALCOM_FORMATS) {
                if (mediaUrlLowerCase.endsWith(suffix)) {
                    isSupport = true;
                    break;
                }
            }
        }
        return isSupport;
    }
}
