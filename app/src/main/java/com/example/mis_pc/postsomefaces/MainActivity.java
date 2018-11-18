package com.example.mis_pc.postsomefaces;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mis_pc.postsomefaces.PostInfo;
import com.example.mis_pc.postsomefaces.RecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String Database_Path = "post";

    //Componentes y widgets
    Button subirFotoBoton;
    Button enviarPostBoton;
    ImageView fotoTomada;
    EditText ubicacionEdit;
    EditText descripcionEdit;
    TextView userLoggedText;
    Dialog subirFotoDialog;
    RecyclerView rv;

    public Bitmap ultimaFoto = null;
    DatabaseReference databaseReference;
    List<PostInfo> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userLoggedText = findViewById(R.id.userLogged);
        userLoggedText.setText(getFirebaseUser().getEmail().toString());

        //Creado y mostrando reciclyview
        rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(new RecyclerAdapter(MainActivity.this, posts));

        //Actualizando recycleview con firebase cuando sea necesario
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts = new ArrayList<>();
                String firebaseUserEmail = getFirebaseUser().getEmail().toString();
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                    if (firebaseUserEmail.equals(postSnapShot.child("_usuario").getValue(String.class))) {
                        String foto = postSnapShot.child("_foto").getValue(String.class);
                        String ubicacion = postSnapShot.child("_ubicacion").getValue(String.class);
                        String descripcion = postSnapShot.child("_descripcion").getValue(String.class);
                        String usuario = postSnapShot.child("_usuario").getValue(String.class);
                        posts.add(new PostInfo(foto, ubicacion, descripcion, usuario));
                    }
                    Collections.reverse(posts);


                    rv.setAdapter(new RecyclerAdapter(MainActivity.this, posts));
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

                //Obteniendo ubicacion gps


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

                //Enviar post a firebase
                ubicacionEdit = subirFotoDialog.findViewById(R.id.textUbicacion);
                descripcionEdit = subirFotoDialog.findViewById(R.id.descripcionPost);
                enviarPostBoton = subirFotoDialog.findViewById(R.id.botonEnviar);
                enviarPostBoton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ultimaFoto != null &&
//                                ubicacionEdit.getText().toString().length() > 0 &&
                              descripcionEdit.getText().toString().length() >0 &&
                                getFirebaseUser().getEmail().toString().length() > 0)
                        {
                            //PostInfo post = new PostInfo(encodeBitmap(ultimaFoto), ubicacionEdit.getText().toString(),
                            // descripcionEdit.getText().toString(), getFirebaseUser().getEmail().toString());
                            PostInfo post = new PostInfo(encodeBitmap(ultimaFoto), "Santo domingo",
                                   descripcionEdit.getText().toString(), getFirebaseUser().getEmail().toString());
                            enviarPostAFirebase(post);
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

    public void enviarPostAFirebase(PostInfo post) {
        String ImageUploadId = databaseReference.push().getKey();
        databaseReference.child(ImageUploadId).setValue(post);

    }

    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

}
