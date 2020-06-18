package juns.lib.media.provider.video;

/**
 * 视频过滤器
 * <p>1. 不支持AC3解码的媒体,该解码方式需要申请许可证 </p>
 * <p>2. 80-nu339-1sc文档中说明, 由于license的的原因高通默认是关闭对divx这种码流的支持的. 如果需要支持,需联系高通TAM获取相关的license</p>
 * <p>3. .vob格式，高通平台 不支持</p>
 */
public final class VideoFilter {
    //媒体格式后缀
    private static final String[] QUALCOM_FORMATS = new String[]{
            ".mp4" //高通支持
            , ".avi"//高通支持
            , ".mkv"//高通支持
            , ".3gp"//高通支持
            , ".flv"//高通支持
            , ".mov"//高通支持
            , ".m4v" // ??
            , ".pmp" // ??
            , ".ts" // ??
            , ".tp" // ??
            , ".m2ts" // ??
            , ".mpg" // ??
            , ".asf" // ??
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
