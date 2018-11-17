package com.example.mis_pc.postsomefaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyRecycleItemViewHolder>{

    private final List<PostInfo> items;
    private final Context context;

    public RecyclerAdapter(Context context , List<PostInfo> items){
        this.context = context;
        this.items = items;
    }

    @Override
    public MyRecycleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        return new MyRecycleItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.principal_card;
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecycleItemViewHolder holder, int position) {
        final PostInfo postinfo = items.get(position);

        Bitmap fotoDecodificada = decodeFromFirebaseBase64(postinfo.get_foto());
        holder._foto.setImageBitmap(fotoDecodificada);
        holder._descripcion.setText(postinfo.get_descripcion());
        holder._usuario.setText(postinfo.get_usuario());
        holder._ubicacion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Uri gmmIntentUri = Uri.parse(postinfo._ubicacion);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });


        //Compartir en facebook
        SharePhoto photo = new SharePhoto.Builder().setBitmap(fotoDecodificada).build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setShareHashtag(new ShareHashtag.Builder()
                        .setHashtag("#ConsiganmeElUser"+postinfo.get_ubicacion().replace(" ","")).build())
                .build();
        holder._shareButton.setShareContent(content);

    }

    public static Bitmap decodeFromFirebaseBase64(String image){
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public static class MyRecycleItemViewHolder extends RecyclerView.ViewHolder{
        public TextView _descripcion, _usuario;
        public Button _ubicacion;
        public ImageView _foto;
        public ShareButton _shareButton;


        public MyRecycleItemViewHolder(View itemView) {
            super(itemView);
            _ubicacion = itemView.findViewById(R.id.irAMaps);
            _descripcion = itemView.findViewById(R.id.textDescripcion);
            _foto = itemView.findViewById(R.id.fotoDelito);
            _shareButton = itemView.findViewById(R.id.facebookButton);
            _usuario = itemView.findViewById(R.id.textoUsuario);
        }
    }
}
