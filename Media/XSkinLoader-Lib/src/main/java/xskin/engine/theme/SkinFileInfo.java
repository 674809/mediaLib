package xskin.engine.theme;

/**
 * Skin file information.
 *
 * @author Jun.Wang
 */
public class SkinFileInfo extends Object {
    private int themeFlag;
    private String saveDir;// /data/user/0/com.egar.music/cache/skins
    private String saveFileName;// 1_ios.skin
    private String assetDir; //skins/MusicPlayer_Skin_IOS-release.apk

    public SkinFileInfo() {
    }

    public int getThemeFlag() {
        return themeFlag;
    }

    public void setThemeFlag(int themeFlag) {
        this.themeFlag = themeFlag;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }

    public String getAssetDir() {
        return assetDir;
    }

    public void setAssetDir(String assetDir) {
        this.assetDir = assetDir;
    }
}
