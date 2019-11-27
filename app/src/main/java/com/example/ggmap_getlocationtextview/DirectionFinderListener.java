package com.example.ggmap_getlocationtextview;

import android.util.Log;
import java.util.List;

public interface DirectionFinderListener {
    void  setText(List<Route> routes);
    void changeColorJoinMaker(double wasteJoinLat, double wasteJoinLon);
}
