package ga.navigo.help;

/**
 * Created by ASUS 553 on 11.03.2018.
 */

import android.location.Location;

public interface MyLocationListener {

    void updateLocation(Location location);
    void viewLocation(Location location);


}
