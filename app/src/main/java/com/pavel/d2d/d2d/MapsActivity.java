package com.pavel.d2d.d2d;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.admin.DeviceAdminReceiver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Xml;
import android.view.View;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.Manifest;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;

import android.support.v4.content.ContextCompat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener, OnPolylineClickListener {
    LocationManager locationManager;
    String Name = "Pavel";
    ArrayList<String> saveFileIDS = new ArrayList<String>();
    ArrayList<String> saveFilePoints = new ArrayList<String>();
    int lastID = 0;
    ViewFlipper flipper;
    private int polylineColorDone = Color.argb(90, 255, 0, 0);
    private int polylineColorNotDone = Color.argb(90, 0, 255, 0);
    private int polylineColorSelected = Color.argb(255, 0, 0, 255);
    private Object selected;
    public ArrayList<PolylineObject> routes = new ArrayList<PolylineObject>();
    public boolean routeDraw = false;
    private GoogleMap mMap;
    private PolylineOptions pol = new PolylineOptions().width(10).color(polylineColorDone);
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        flipper = (ViewFlipper) findViewById(R.id.flipper);





    }

    public void loadRoutes()
    {
        String s ="";
        SharedPreferences saveFile = getSharedPreferences("Data",0);
        String[] IDs = saveFile.getString("ID","null").split(",");
        if(IDs[0]=="")
            return;
        lastID = saveFile.getInt("lastID",0);
        String[] points = new String[IDs.length];
        if(!IDs[0].equals("null"))
        for(int i = 0;i<IDs.length;i++)
        {
            points[i] = saveFile.getString(IDs[i].toString()+"points","null");
            String visited = saveFile.getString(IDs[i].toString()+"visited","null");
            createRoute(IDs[i],points[i],visited);
        }
    }
    void createRoute(String ID,String points,String visited)
    {
        String[] pointPairs = points.split(";");
        String[] pointX= new String[pointPairs.length];
        String[] pointY=new String[pointPairs.length];
        int color =0;
        if(visited.equals("null"))
            color = polylineColorNotDone;
        else color = polylineColorDone;
        pol = pol.color(color);
        for(int i = 0;i<pointPairs.length;i++)
        {
            String[] point = pointPairs[i].split(",");
            pointX[i] = point[0];
            pointY[i] = point[1];
            LatLng ltn = new LatLng(Double.parseDouble(point[0]),Double.parseDouble(point[1]));
            pol.add(ltn);

        }
        if(polyline!=null)
            polyline.remove();
        polyline = mMap.addPolyline(pol);

        addRoute(ID,visited);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        String s = "Hello my name is Pavel.";
        try {
            s=(URLEncoder.encode(s, "UTF-8"));
        } catch(java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("MyActivity",s);
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        SharedPreferences saveFile = getSharedPreferences("Data",0);
        String IDs = saveFile.getString("ID","null");
        Log.d("MyActivity",IDs);
        loadRoutes();
    }
    @Override
    public void onMapClick(LatLng point) {
        if (routeDraw==true) {
            pol.add(point);
            if (polyline != null)
                polyline.remove();
            polyline = mMap.addPolyline(pol);
        }else{
            if(selected != null)
                deselect();
        }

    }
    @Override
    public void onPolylineClick(Polyline p)
    {
        if(routeDraw==false)
            select(p);



    }
    public PolylineObject getPolylineObject(Polyline p)
    {
        return (PolylineObject)p.getTag();
    }

    private void select(Polyline p)
    {
        if(selected!=null)
            deselect();
        selected = p;
        PolylineObject pObj = getPolylineObject(p);
        p.setColor(polylineColorSelected);
        flipper.setDisplayedChild(flipper.indexOfChild(findViewById(R.id.btvisit)));
        LatLng pos = pObj.startPoint;
        TextView txt = (TextView)findViewById(R.id.btvisit);
        if(!pObj.visited.equals("null"))
        {
            txt.setText("Change to not visited");
        }else {
            txt.setText("Change to visited");
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        pObj.mrk = setRouteMarker(pos,pObj.ID,pObj.visited);
        pObj.mrk.showInfoWindow();
    }

    private void deselect()
    {

        if(selected != null)
        {

            String str = selected.getClass().getName();
            String[] str2 = str.split("\\.");
            str = str2[str2.length-1];
            if(str.equals("Polyline"))
            {
                flipper.setDisplayedChild(flipper.indexOfChild(findViewById(R.id.btMark)));
                Polyline p = (Polyline)selected;
                PolylineObject pObj = (PolylineObject)p.getTag();
                if(pObj.mrk!=null) {
                    pObj.mrk.remove();
                    pObj.mrk = null;
                }
                if(!pObj.visited.equals("null"))
                    p.setColor(polylineColorDone);
                else{
                    p.setColor(polylineColorNotDone);
                }
            }
        }
    }
    private Marker setRouteMarker(LatLng pos,String title,String date)
    {
        if(!date.equals("null"))
            return mMap.addMarker(new MarkerOptions().position(pos).title(title).snippet("Area visited on: "+date));
        else return mMap.addMarker(new MarkerOptions().position(pos).title(title).snippet("Area not visited"));

    }
    public void newRoute(View view) {
        TextView text = (TextView)findViewById(R.id.btMark);

        if(routeDraw == false)
        {
            pol.color(polylineColorNotDone);
            routeDraw = true;
            text.setText("Save route");
            return;
        }
        else
        {
           routeDraw = false;
            addRoute();
            text.setText("New route");
        }
    }
    public void removeSelected(View view)
    {
        PolylineObject po = getPolylineObject((Polyline)selected);
        String ID = po.ID;
        po.remove();
        removeSave(ID);
        flipper.setDisplayedChild(R.id.btMark);
        selected = null;

    }
    public void visitSelected(View view)
    {
        PolylineObject po = getPolylineObject((Polyline)selected);
        TextView txt = (TextView)view;
        if(!po.visited.equals("null"))
        {
            txt.setText("Change to visited");
            po.visited = "null";
            saveVisitDate(po);
        }else {
            txt.setText("Change to not visited");
            String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
            po.visited = date;
            saveVisitDate(po);

        }
    }
    public void saveVisitDate(PolylineObject po)
    {
        SharedPreferences saveFile = getSharedPreferences("Data",0);
        SharedPreferences.Editor editor = saveFile.edit();
        editor.putString(po.ID+"visited",po.visited);
        editor.apply();


    }
    public void addRoute()
    {
        addRoute("","null");
    }

    public void addRoute(String ID,String visited)
    {
        if(polyline.getPoints().size()<2)
        {
            if(polyline != null)
                polyline.remove();
            return;
        }
        if(polyline!=null) {
            int color = 0;
            if(visited.equals("null"))
                color = polylineColorNotDone;
            else color = polylineColorDone;
            pol = new PolylineOptions().width(10).color(color);
            PolylineObject pObj = new PolylineObject();
            pObj.visited = visited;
            pObj.polyline = polyline;
            pObj.startPoint = polyline.getPoints().get(0);
            pObj.parent = this;
            if(ID.equals("")) {
                pObj.ID = Name + lastID;
                lastID++;
            }
            else{
                pObj.ID = ID;
            }

            List<LatLng> lst = polyline.getPoints();
            String points = "";
            for(int i=0;i<lst.size();i++)
            {
                points += lst.get(i).latitude+","+lst.get(i).longitude+";";
            }
            saveFileIDS.add(pObj.ID);
            saveFilePoints.add(points);

            polyline.setTag(pObj);
            routes.add(pObj);
            polyline.setClickable(true);
            polyline = null;
            if(ID.equals(""))
                saveRoute(routes.size()-1);
        }

    }
    void saveRoute(PolylineObject route)
    {

        SharedPreferences saveFile = getSharedPreferences("Data",0);
        SharedPreferences.Editor editor = saveFile.edit();
        String ids = "";
        editor.putInt("lastID",lastID);

        for(int i = 0;i<saveFileIDS.size();i++) {
            ids += saveFileIDS.get(i)+",";
        }
        editor.putString("ID",ids);
        editor.apply();
    }
    void removeSave(String id)
    {
        SharedPreferences saveFile = getSharedPreferences("Data",0);
        SharedPreferences.Editor editor = saveFile.edit();
        editor.remove(id+"points");
        String ids = "";
        for(int i = 0;i<saveFileIDS.size();i++) {
            ids += saveFileIDS.get(i)+",";
        }
        editor.putString("ID",ids);
        Log.d("MyActivity",ids);
        editor.apply();
    }

    void saveRoute(int index)
    {
        SharedPreferences saveFile = getSharedPreferences("Data",0);
        SharedPreferences.Editor editor = saveFile.edit();
        String ids = "";
        editor.putInt("lastID",lastID);
        for(int i = 0;i<saveFileIDS.size();i++) {
            ids += saveFileIDS.get(i)+",";
        }
        editor.putString(routes.get(index).ID+"points", saveFilePoints.get(index));
        editor.putString("ID",ids);
        Log.d("MyActivity",ids);
        Log.d("MyActivity",routes.get(index).ID);
        Log.d("MyActivity",saveFilePoints.get(index));

        editor.apply();
    }
}