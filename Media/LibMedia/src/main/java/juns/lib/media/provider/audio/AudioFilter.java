package juns.lib.media.provider.audio;

/**
 * 音频过滤器
 * <p>1. 不支持AC3解码的媒体,该解码方式需要申请许可证 </p>
 * <p>2. ".wma" //高通解码不支持,平台支持-需要软解码</p>
 * <p>3. ".m4a" //高通支持播放,但不支持拖动 </p>
 */
public final class AudioFilter {
    //媒体格式后缀
    private static final String[] QUALCOM_FORMATS = new String[]{
            ".mp3" //高通支持
            , ".aac"//高通支持
            , ".flac"//高通支持
            , ".ape"//高通支持
            , ".wav"//高通支持
            , ".m4a"//高通支持播放,但不支持拖动
            , ".ogg" // ??
            , ".amr" // ??
            , ".alac"// ??
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
