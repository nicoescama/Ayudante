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

        Category dir = items.get(position);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);

        if(dir.getCategoryId().equals("hospedaje")||dir.getCategoryId().equals("peaje")
                || dir.getCategoryId().equals("otro") || dir.getCategoryId().equals("gasolina")
                || dir.getCategoryId().equals("alimentacion")) {
            TextView title = (TextView) v.findViewById(R.id.categoryCat);
            TextView description = (TextView) v.findViewById(R.id.textoCat);
            ImageView imagen = (ImageView) v.findViewById(R.id.imageViewCat);

            title.setText(dir.getCategoryId().toUpperCase()+"   Gastos: "+dir.getTipo());
            description.setText("Total: "+format.format(Integer.parseInt(dir.getCosto())));
            description.setTextSize(26);
            imagen.setImageDrawable(dir.getImage());

        }
        else {
            TextView title = (TextView) v.findViewById(R.id.categoryCat);
            String[] datos2 = dir.getLugar().split("&");
            String lugarDar = "";
            if (datos2.length == 2) {
                lugarDar = datos2[1];
            } else {
                lugarDar = dir.getLugar();
            }
            title.setText(lugarDar + "  " + dir.getFecha());

            TextView description = (TextView) v.findViewById(R.id.textoCat);
            description.setTextSize(26);
            String[] datos = dir.getCosto().split("&");
            description.setText(format.format(Integer.parseInt(datos[0])));

            ImageView imagen = (ImageView) v.findViewById(R.id.imageViewCat);
            imagen.setImageDrawable(dir.getImage());
        }

        return v;
    }
}