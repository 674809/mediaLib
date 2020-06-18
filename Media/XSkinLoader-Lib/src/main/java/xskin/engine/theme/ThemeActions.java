package xskin.engine.theme;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ThemeActions {
    /**
     * Add skin file information
     *
     * @param themeFlag The theme value you set.
     * @param themeDesc Theme description string ,such as "ios"
     * @param assetDir  Theme package path in assets.
     *                  Such as you put file to "/src/main/assets/skins/ios_skin.apk", parameter must be "skins/ios_skin.apk"
     */
    void addSkinFileInfo(int themeFlag, @Nullable String themeDesc, @NonNull String assetDir);

    /**
     * Release skin file to default path.
     * <p>Release file from assets to native path will last for long time.</p>
     * <p>NOTE : Recommend you executed this method in async runnable.</p>
     *
     * @param themeFlag Current theme flag value.
     * @return The skin file path.
     */
    String releaseSkinFile(int themeFlag);
}
