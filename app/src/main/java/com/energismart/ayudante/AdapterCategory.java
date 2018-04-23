package com.energismart.ayudante;

/**
 * Created by nicoescama on 27/03/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdapterCategory extends BaseAdapter {

    protected Activity activity;
    protected ArrayList<Category> items;

    public AdapterCategory (Activity activity, ArrayList<Category> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public void addAll(ArrayList<Category> category) {
        for (int i = 0; i < category.size(); i++) {
            items.add(category.get(i));
        }
    }

    @Override
    public Object getItem(int arg0) {
        return items.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.item_category, null);
        }

        final Category dir = items.get(position);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setMaximumFractionDigits(0);

        if(dir.getCategoryId().equals("hospedaje")||dir.getCategoryId().equals("peaje")
                || dir.getCategoryId().equals("otro") || dir.getCategoryId().equals("gasolina")
                || dir.getCategoryId().equals("alimentacion")) {
            TextView title = (TextView) v.findViewById(R.id.categoryCat);
            final TextView texto2 = (TextView) v.findViewById(R.id.textoCat2);
            texto2.setVisibility(View.GONE);
            TextView description = (TextView) v.findViewById(R.id.textoCat);
            ImageView imagen = (ImageView) v.findViewById(R.id.imageViewCat);

            title.setText(dir.getCategoryId().toUpperCase()+"   Gastos: "+dir.getTipo());
            description.setText("Total: "+format.format(Integer.parseInt(dir.getCosto())));
            description.setTextSize(26);
            imagen.setImageDrawable(dir.getImage());

        }
        else if(dir.getCategoryId().equals("trip")){
            final TextView title = (TextView) v.findViewById(R.id.categoryCat);
            final TextView description = (TextView) v.findViewById(R.id.textoCat);
            final ImageView imagen = (ImageView) v.findViewById(R.id.imageViewCat);
            final TextView texto2 = (TextView) v.findViewById(R.id.textoCat2);
            texto2.setVisibility(View.VISIBLE);

            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

            myRef.child("users").child(userKey).child("trucks").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    title.setText("Ruta: "+dir.getLugar());
                    description.setTextSize(26);
                    texto2.setText("Fecha inicio: " + dir.getFecha() );
                    texto2.setTextSize(26);
                    imagen.setImageDrawable(dir.getImage());
                    for(DataSnapshot truckActual : dataSnapshot.getChildren()) {
                        if(truckActual.getKey().equals(dir.getTipo())) {
                            description.setText("Placa: "+truckActual.child("placa").getValue(String.class));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
        else {
            TextView title = (TextView) v.findViewById(R.id.categoryCat);
            String[] datos2 = dir.getLugar().split("&");
            String lugarDar = "";
            final TextView texto2 = (TextView) v.findViewById(R.id.textoCat2);
            texto2.setVisibility(View.VISIBLE);
            texto2.setTextSize(26);
            if (datos2.length == 2) {
                lugarDar = datos2[1];
            } else {
                lugarDar = dir.getLugar();
            }
            title.setText("Lugar: "+lugarDar);
            texto2.setText("Fecha: " + dir.getFecha());

            TextView description = (TextView) v.findViewById(R.id.textoCat);
            description.setTextSize(26);
            String[] datos = dir.getCosto().split("&");
            description.setText("Total: "+format.format(Integer.parseInt(datos[0])));

            ImageView imagen = (ImageView) v.findViewById(R.id.imageViewCat);
            imagen.setImageDrawable(dir.getImage());
        }

        return v;
    }
}