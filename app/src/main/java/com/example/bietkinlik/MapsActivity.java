package com.example.bietkinlik;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.koushikdutta.ion.Ion;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private FirebaseAuth fAuth;
    private DatabaseReference dRef;
    SearchView searchView;
    SharedPreference sharedPreference;
    Context context = this;
    String myParentNode,ppURL,donenDeger;
    Object uName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference();

        searchView = findViewById(R.id.location);
        sharedPreference = new SharedPreference();
        donenDeger = sharedPreference.getValue(context);

        searchView.setSelected(false);
        searchView.setFocusable(false);
        ppURL=getIntent().getExtras().get("url").toString();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(location, 1);
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        getUserName();
    }

    public void DialogDelete(){
        String userId = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        DeleteActivity(userId);
    }

    public void ActivityChange() {
        FirebaseUser fUser = fAuth.getCurrentUser();
        assert fUser != null;
        String userId = fUser.getUid();
        dRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        dRef.child("activity").setValue("true");
    }


    public void DeleteActivity(String userId){
        DatabaseReference delActivity=FirebaseDatabase.getInstance().getReference("Activitys").child(userId);
        delActivity.removeValue();
        DatabaseReference changeActivity= FirebaseDatabase.getInstance().getReference("Users").child(userId);
        changeActivity.child("activity").setValue("false");
        Toast.makeText(this,"Etkinliğiniz silindi. Şimdi yeni etkinlik ekleyebilirsiniz...",Toast.LENGTH_LONG).show();
        Intent map = new Intent(MapsActivity.this, MapsActivity.class);
        map.putExtra("url",ppURL);
        startActivity(map);
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        if (donenDeger.equals("true")) {
            new SweetAlertDialog(MapsActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("Oops...")
                    .setContentText("Sadece bir etkinlik ekleyebilirsiniz! Yeni etkinliği eklemek için eski etkinliğinizi silmelisiniz...")
                    .setCancelButton("Delete", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            FirebaseUser fUser = fAuth.getCurrentUser();
                            assert fUser != null;
                            String userId = fUser.getUid();
                            DeleteActivity(userId);
                        }
                    })
                    .show();
        } else {
            MarkerOptions markerOptions =
                    new MarkerOptions().position(latLng);
            markerOptions.draggable(true);
            mMap.addMarker(markerOptions);

            FirebaseUser fUser = fAuth.getCurrentUser();
            assert fUser != null;
            String userId = fUser.getUid();
            dRef = FirebaseDatabase.getInstance().getReference().child("Activitys").child(userId);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("activity", "");
            hashMap.put("latitude", latLng.latitude);
            hashMap.put("longitude", latLng.longitude);
            hashMap.put("time", "");
            hashMap.put("photo", ppURL);
            dRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        donenDeger="true";
                        EtkinlikDialog ed = new EtkinlikDialog();
                        ed.show(getSupportFragmentManager(), "Etkinlik Ekle");
                    }
                }
            });
    Toast.makeText(this, "Marker eklendi. Şimdi etkinliğinizi ekleyebilirsiniz.", Toast.LENGTH_LONG).show();
            ActivityChange();
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.setTitle(marker.getPosition().toString());
        marker.showInfoWindow();
        marker.setAlpha(0.5f);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        marker.setTitle(marker.getPosition().toString());
        marker.showInfoWindow();
        marker.setAlpha(0.5f);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        marker.setTitle(marker.getPosition().toString());
        marker.showInfoWindow();
        marker.setAlpha(1.0f);
    }

//    private boolean ShouldAddMarker(double latA, double lngA, double latB, double lngB) {
//        Location locationA = new Location("point A");
//        locationA.setLatitude(latA);
//        locationA.setLongitude(lngA);
//        Location locationB = new Location("point B");
//        locationB.setLatitude(latB);
//        locationB.setLongitude(lngB);
//        float distance = locationA.distanceTo(locationB);
//        return distance > 1000;
//    }

    private void getUserName(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        final Query userQuery = dRef.orderByChild("username");

        userQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                uName=dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                          @Override
                                          public boolean onMarkerClick(final Marker marker) {
                                              dRef = FirebaseDatabase.getInstance().getReference().child("Activitys");
                                              dRef.addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                      for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                          Map<String, Object> map = (Map<String, Object>) ds.getValue();
                                                          assert map != null;
                                                          final Object getActivity = map.get("activity");
                                                          Object getTime = map.get("time");
                                                          Object getLat = map.get("latitude");
                                                          Object getLng = map.get("longitude");

                                                          double Lat = Double.parseDouble(String.valueOf(getLat));
                                                          double Lng = Double.parseDouble(String.valueOf(getLng));

                                                          if (marker.getPosition().latitude==Lat && marker.getPosition().longitude==Lng) {
                                                              final Query userQuery = dRef.orderByChild("activity");

                                                              userQuery.addChildEventListener(new ChildEventListener() {
                                                                  @Override
                                                                  public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                                      myParentNode= dataSnapshot.getKey();
                                                                  }

                                                                  @Override
                                                                  public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                                                  }

                                                                  @Override
                                                                  public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                                                  }

                                                                  @Override
                                                                  public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                                                  }

                                                                  @Override
                                                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                  }
                                                              });

                                                                  new SweetAlertDialog(MapsActivity.this, SweetAlertDialog.NORMAL_TYPE).setTitleText((String) getTime)
                                                                          .setContentText((String) getActivity)
                                                                          .setCancelText("Kapat")
                                                                          .setConfirmButton("Mesaj At", new SweetAlertDialog.OnSweetClickListener() {
                                                                              @Override
                                                                              public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                              Intent i = new Intent(getApplicationContext(),ChatActivity.class);
                                                                                              i.putExtra("parent_key",myParentNode);
                                                                                              i.putExtra("control","map");
                                                                                              i.putExtra("username",(String)uName);
                                                                                              i.putExtra("topic",(String)getActivity);
                                                                                              startActivity(i);
                                                                              }
                                                                          })
                                                                          .show();
                                                          }
                                                      }
                                                  }
                                                  @Override
                                                  public void onCancelled(@NonNull DatabaseError databaseError) {
                                                  }
                                              });
                                              return false;
                                          }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        dRef = FirebaseDatabase.getInstance().getReference().child("Activitys");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    assert map != null;
                    Object getActivity = map.get("activity");
                    Object getTime = map.get("time");
                    Object getLat = map.get("latitude");
                    Object getLng = map.get("longitude");
                    Object geturl = map.get("photo");

                    Bitmap b = null;
                    if(geturl!=null){
                        try {
                            String convertedToString = String.valueOf(geturl);
                            b = Ion.with(context).load(convertedToString).asBitmap().get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    if(b==null){
                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.userlogo);
                        b=bitmapdraw.getBitmap();
                    }
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

                    double Lat = Double.parseDouble(String.valueOf(getLat));
                    double Lng = Double.parseDouble(String.valueOf(getLng));
                    LatLng latLng = new LatLng(Lat, Lng);
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).title(getTime + " , " + getActivity);
                    markerOptions.draggable(true);
                    mMap.addMarker(markerOptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();

        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(),
                    Locale.getDefault());

            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude,
                        longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    String state = listAddresses.get(0).getAdminArea();
                    String country = listAddresses.get(0).getCountryName();
                    String subLocality = listAddresses.get(0).getSubLocality();
                    markerOptions.title("" + latLng + "," + subLocality + "," + state
                            + "," + country);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}