package com.example.mis_pc.postsomefaces;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mis_pc.postsomefaces.PostInfo;
import com.example.mis_pc.postsomefaces.RecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    static final String Database_Path = "All_Image_Uploads_Database";

    //Componentes y widgets
    Button subirFotoBoton;
    Button enviarPostBoton;
    ImageView fotoTomada;
    EditText descripcionEdit;
    TextView userLoggedText;
    Dialog subirFotoDialog;
    RecyclerView rv;

    //Location
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    public String latitude;
    public String longitude;

    public Bitmap ultimaFoto = null;
    DatabaseReference databaseReference;
    List<PostInfo> postinfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obteniendo ubicacion gps
    //    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toast.makeText(MainActivity.this,"Presiona el boton flotante para publicar un delito",Toast.LENGTH_SHORT).show();

        userLoggedText = findViewById(R.id.userLogged);
        userLoggedText.setText(getFirebaseUser().getEmail().toString());

        //Creado y mostrando reciclyview
        rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(new RecyclerAdapter(MainActivity.this, postinfo));

        //Actualizando recycleview con firebase cuando sea necesario
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postinfo = new ArrayList<>();
                for (DataSnapshot PostSnapshot : dataSnapshot.getChildren()) {

                    String foto = PostSnapshot.child("_foto").getValue(String.class);
                    String ubicacion = PostSnapshot.child("_ubicacion").getValue(String.class);
                    String descripcion = PostSnapshot.child("_descripcion").getValue(String.class);
                    String usuario = PostSnapshot.child("_usuario").getValue(String.class);
                    postinfo.add(new PostInfo(foto, ubicacion, descripcion, usuario));

                    Collections.reverse(postinfo);

                    rv.setAdapter(new RecyclerAdapter(MainActivity.this,postinfo ));
                }
                rv.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Creando y mostrando dialog
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subirFotoDialog = new Dialog(MainActivity.this);
                subirFotoDialog.setContentView(R.layout.content_upload);
                subirFotoDialog.show();



                //Tomar foto via app camara
                subirFotoBoton = subirFotoDialog.findViewById(R.id.botonTomarFoto);
                subirFotoBoton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                });

                //Enviar delito a firebase
                descripcionEdit = subirFotoDialog.findViewById(R.id.descripcionDelito);
                enviarPostBoton = subirFotoDialog.findViewById(R.id.botonEnviar);
                enviarPostBoton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ultimaFoto != null &&
                                descripcionEdit.getText().toString().length() >0 &&
                                getFirebaseUser().getEmail().toString().length() > 0)
                        {
                            PostInfo postInfo = new PostInfo(encodeBitmap(ultimaFoto),"geo:"+latitude+","+longitude, descripcionEdit.getText().toString(), getFirebaseUser().getEmail().toString());
                            enviarPostFirebase(postInfo);
                            ultimaFoto = null;
                            subirFotoDialog.dismiss();
                            rv.getAdapter().notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this,"Favor complete todos los campos",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //Mostrar foto tomada en imageview
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            fotoTomada = subirFotoDialog.findViewById(R.id.fotoTomada);
            fotoTomada.setImageBitmap(imageBitmap);
            ultimaFoto = imageBitmap;
        }
    }

    public String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }

    public void enviarPostFirebase(PostInfo postInfo) {
        String ImageUploadId = databaseReference.push().getKey();
        databaseReference.child(ImageUploadId).setValue(postInfo);

    }

    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            latitude = String.format(Locale.ENGLISH, "%f",mLastLocation.getLatitude());
                            longitude = String.format(Locale.ENGLISH, "%f",mLastLocation.getLongitude());

                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());

                        }
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

}
