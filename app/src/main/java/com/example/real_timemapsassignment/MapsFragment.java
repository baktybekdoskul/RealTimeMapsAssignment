package com.example.real_timemapsassignment;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, ValueEventListener {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.activity_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        return res;
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
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (database.getReference("markers").child(curUser.getUid()).getKey() != null) {
            database.getReference("markers/").child(curUser.getUid()).addListenerForSingleValueEvent(this);
        }

    }


    @Override
    public void onMapClick(final LatLng ltlg) {
        final LatLong latLng = new LatLong(ltlg.latitude, ltlg.longitude);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Marker addition")
                .setMessage("Will you save this place as a marker?")
                .setPositiveButton("Yes, I will", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        final FirebaseUser curUser = mAuth.getCurrentUser();
                        DatabaseReference ref = database.getReference("markers/").child(curUser.getUid());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    List<LatLong> markers = new ArrayList<>();
                                    markers.add(latLng);
                                    database.getReference("markers/").child(curUser.getUid()).setValue(markers);
                                } else {
                                    List<LatLong> markers = new ArrayList<>();
                                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                                        LatLong latLng1 = dss.getValue(LatLong.class);
                                        markers.add(latLng1);
                                    }
                                    markers.add(latLng);
                                    database.getReference("markers/").child(curUser.getUid()).setValue(markers);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        if (mMap != null) {
                            mMap.addMarker(new MarkerOptions().position(ltlg).title("Marker in " + ltlg.toString()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(ltlg));
                            Toast.makeText(getActivity().getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No, I won't", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            for (DataSnapshot dss : dataSnapshot.getChildren()) {
                LatLong latLng = dss.getValue(LatLong.class);
                LatLng ltlg = new LatLng(latLng.getLatitude(), latLng.getLongitude());
                if (mMap != null) {
                    mMap.addMarker(new MarkerOptions().position(ltlg).title("Marker in " + ltlg.toString()));
                }
            }

        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
