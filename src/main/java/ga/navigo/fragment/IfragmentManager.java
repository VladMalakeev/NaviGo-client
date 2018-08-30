package ga.navigo.fragment;

import android.app.Fragment;
import android.content.Context;

/**
 * Created by ASUS 553 on 07.04.2018.
 */

public interface IfragmentManager {
    public void closeFragment();
   public void startProgress();
    public void setPercentage(final int percentage, final long size);
    public void endProgress(final String message);
}
