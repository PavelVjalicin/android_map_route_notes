package com.pavel.d2d.d2d;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import java.util.Date;

/**
 * Created by Pavel on 07-May-17.
 */

public class PolylineObject {
    public String ID;
    public Polyline polyline;
    public String Name;
    public String visited;
    public LatLng startPoint;
    public Marker mrk;
    public MapsActivity parent;
    public void remove()
    {
        parent.routes.remove(this);
        polyline.remove();
        mrk.remove();
        int index=0;
        for (int i = 0; i < parent.saveFileIDS.size();i++) {
            if(parent.saveFileIDS.get(i).equals(ID))
            {
                index = i;
                break;
            }
        }

        parent.saveFilePoints.remove(index);
        parent.saveFileIDS.remove(index);
    }
}
