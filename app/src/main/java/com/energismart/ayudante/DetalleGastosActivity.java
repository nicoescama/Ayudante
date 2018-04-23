package com.energismart.ayudante;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetalleGastosActivity extends AppCompatActivity {


    Toolbar myToolbar;
    String tripElegidoKey;
    ImageView getReport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_gastos);

        init();
        setSupportActionBar(myToolbar);


        final ArrayList<Category> catList = new ArrayList<Category>();

        final ArrayList<Category> generalList = new ArrayList<Category>();

        final ListView lv = (ListView) findViewById(R.id.listViewDG);

        final ListView lvGeneral = (ListView) findViewById(R.id.listViewGeneral);

        final Drawable gasolinaDraw = getResources().getDrawable(R.drawable.ic_local_gas_station_black_48dp);
        final Drawable alimentacionDraw = getResources().getDrawable(R.drawable.ic_restaurant_black_48dp);
        final Drawable peajeDraw = getResources().getDrawable(R.drawable.ic_peaje_48dp);
        final Drawable otroDraw = getResources().getDrawable(R.drawable.ic_more_horiz_black_48dp);
        final Drawable hospedajeDraw = getResources().getDrawable(R.drawable.ic_hotel_black_48dp);



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(getIntent().getExtras()!=null) {
            tripElegidoKey = getIntent().getStringExtra("key");
            getReport = (ImageView)findViewById(R.id.photo_getReporte);
            getReport.setVisibility(View.VISIBLE);
            myRef.child("users").child(userKey).child("trips").child(tripElegidoKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot currentTrip) {
                    catList.clear();
                    generalList.clear();
              //      for (final DataSnapshot currentTrip : dataSnapshot.getChildren()) {
                        int conteoAlimentacion = 0;
                        int conteoHospedaje = 0;
                        int conteoPeaje = 0;
                        int conteoGasolina = 0;
                        int conteoOtro = 0;
                        int gastoTHospedaje = 0;
                        int gastoTAlimentacion = 0;
                        int gastoTPeaje = 0;
                        int gastoTGasolina = 0;
                        int gastoTOtro = 0;
                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("hospedaje").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            conteoHospedaje++;
                            gastoTHospedaje += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto, hospedajeDraw, "hospedaje", tieneFoto));
                        }
                        if (gastoTHospedaje > 0) {
                            generalList.add(new Category("hospedaje", null, null, Integer.toString(gastoTHospedaje), hospedajeDraw, Integer.toString(conteoHospedaje), false));
                        }
                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("gasolina").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String galones = gasto.child("galones").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            String id = gasto.getKey();
                            conteoGasolina++;
                            gastoTGasolina += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto + "&" + galones, gasolinaDraw, "gasolina", tieneFoto));
                        }
                        if (gastoTGasolina > 0) {
                            generalList.add(new Category("gasolina", null, null, Integer.toString(gastoTGasolina), gasolinaDraw, Integer.toString(conteoGasolina), false));
                        }

                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("alimentacion").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            conteoAlimentacion++;
                            gastoTAlimentacion += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto, alimentacionDraw, "alimentacion", tieneFoto));
                        }
                        if (gastoTAlimentacion > 0) {
                            generalList.add(new Category("alimentacion", null, null, Integer.toString(gastoTAlimentacion), alimentacionDraw, Integer.toString(conteoAlimentacion), false));
                        }

                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("otros").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            String concepto = gasto.child("concepto").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            conteoOtro++;
                            gastoTOtro += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto, otroDraw, concepto, tieneFoto));
                        }
                        if (gastoTOtro > 0) {
                            generalList.add(new Category("otro", null, null, Integer.toString(gastoTOtro), otroDraw, Integer.toString(conteoOtro), false));
                        }

                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("peaje").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            String ejes = gasto.child("ejes").getValue(String.class);
                            conteoPeaje++;
                            gastoTPeaje += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto + "&" + ejes, peajeDraw, "peaje", tieneFoto));
                        }
                        if (gastoTPeaje > 0) {
                            generalList.add(new Category("peaje", null, null, Integer.toString(gastoTPeaje), peajeDraw, Integer.toString(conteoPeaje), false));
                        }

                  //  }
                    AdapterCategory adapter = new AdapterCategory(DetalleGastosActivity.this, generalList);
                    lvGeneral.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else {
            myRef.child("users").child(userKey).child("currentTrip").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    catList.clear();
                    generalList.clear();
                    for (final DataSnapshot currentTrip : dataSnapshot.getChildren()) {
                        int conteoAlimentacion = 0;
                        int conteoHospedaje = 0;
                        int conteoPeaje = 0;
                        int conteoGasolina = 0;
                        int conteoOtro = 0;
                        int gastoTHospedaje = 0;
                        int gastoTAlimentacion = 0;
                        int gastoTPeaje = 0;
                        int gastoTGasolina = 0;
                        int gastoTOtro = 0;
                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("hospedaje").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            conteoHospedaje++;
                            gastoTHospedaje += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto, hospedajeDraw, "hospedaje", tieneFoto));
                        }
                        if (gastoTHospedaje > 0) {
                            generalList.add(new Category("hospedaje", null, null, Integer.toString(gastoTHospedaje), hospedajeDraw, Integer.toString(conteoHospedaje), false));
                        }
                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("gasolina").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String galones = gasto.child("galones").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            String id = gasto.getKey();
                            conteoGasolina++;
                            gastoTGasolina += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto + "&" + galones, gasolinaDraw, "gasolina", tieneFoto));
                        }
                        if (gastoTGasolina > 0) {
                            generalList.add(new Category("gasolina", null, null, Integer.toString(gastoTGasolina), gasolinaDraw, Integer.toString(conteoGasolina), false));
                        }

                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("alimentacion").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            conteoAlimentacion++;
                            gastoTAlimentacion += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto, alimentacionDraw, "alimentacion", tieneFoto));
                        }
                        if (gastoTAlimentacion > 0) {
                            generalList.add(new Category("alimentacion", null, null, Integer.toString(gastoTAlimentacion), alimentacionDraw, Integer.toString(conteoAlimentacion), false));
                        }

                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("otros").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            String concepto = gasto.child("concepto").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            conteoOtro++;
                            gastoTOtro += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto, otroDraw, concepto, tieneFoto));
                        }
                        if (gastoTOtro > 0) {
                            generalList.add(new Category("otro", null, null, Integer.toString(gastoTOtro), otroDraw, Integer.toString(conteoOtro), false));
                        }

                        for (DataSnapshot gasto : currentTrip.child("listaGastos").child("peaje").getChildren()) {
                            String costoGasto = gasto.child("costo").getValue(String.class);
                            String lugarGasto = gasto.child("lugar").getValue(String.class);
                            String fecha = gasto.child("fecha").getValue(String.class);
                            Boolean tieneFoto = gasto.child("foto").getValue(Boolean.class);
                            String id = gasto.getKey();
                            String ejes = gasto.child("ejes").getValue(String.class);
                            conteoPeaje++;
                            gastoTPeaje += Integer.parseInt(costoGasto);
                            catList.add(new Category(id, lugarGasto, fecha, costoGasto + "&" + ejes, peajeDraw, "peaje", tieneFoto));
                        }
                        if (gastoTPeaje > 0) {
                            generalList.add(new Category("peaje", null, null, Integer.toString(gastoTPeaje), peajeDraw, Integer.toString(conteoPeaje), false));
                        }

                    }
                    AdapterCategory adapter = new AdapterCategory(DetalleGastosActivity.this, generalList);
                    lvGeneral.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        lvGeneral.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArrayList<Category> listActual = new ArrayList<Category>();
                Category catElegida = (Category) parent.getItemAtPosition(position);
                switch (catElegida.getCategoryId()){
                    case "alimentacion":
                        for(Category obj : catList){
                            if(obj.getTipo().equals("alimentacion")){
                                listActual.add(obj);
                            }
                        }
                        break;
                    case "peaje":
                        for(Category obj : catList){
                            if(obj.getTipo().equals("peaje")){
                                listActual.add(obj);
                            }
                        }
                        break;
                    case "hospedaje":
                        for(Category obj : catList){
                            if(obj.getTipo().equals("hospedaje")){
                                listActual.add(obj);
                            }
                        }
                        break;
                    case "gasolina":
                        for(Category obj : catList){
                            if(obj.getTipo().equals("gasolina")){
                                listActual.add(obj);
                            }
                        }
                        break;
                    case "otro":
                        for(Category obj : catList){
                            if(!obj.getTipo().equals("alimentacion") && !obj.getTipo().equals("hospedaje")
                                    && !obj.getTipo().equals("gasolina") && !obj.getTipo().equals("peaje")){
                                listActual.add(obj);
                            }
                        }

                }
                AdapterCategory adapterActual = new AdapterCategory(DetalleGastosActivity.this, listActual);
                lv.setAdapter(adapterActual);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //myRef.child("users").child(userKey).child("currentTrip").removeEventListener(refLectura);
                Category elegida = (Category) adapterView.getItemAtPosition(position);
                switch(elegida.getTipo()){
                    case "hospedaje":
                        Intent intentHospedaje = new Intent(DetalleGastosActivity.this, AddHospedajeActivity.class);
                        intentHospedaje.putExtra("id",elegida.getCategoryId());
                        intentHospedaje.putExtra("lugar",elegida.getLugar());
                        intentHospedaje.putExtra("fecha",elegida.getFecha());
                        intentHospedaje.putExtra("costo",elegida.getCosto());
                        startActivity(intentHospedaje);
                        break;
                    case "peaje":
                        Intent intentPeaje = new Intent(DetalleGastosActivity.this, AddPeajeActivity.class);
                        intentPeaje.putExtra("id",elegida.getCategoryId());
                        String[] datosOtro = elegida.getLugar().split("&");
                        if(datosOtro.length==2){
                            intentPeaje.putExtra("lugar","otro");
                            intentPeaje.putExtra("lugarOtro",datosOtro[1]);
                        }
                        else {
                            intentPeaje.putExtra("lugar", elegida.getLugar());
                        }
                        intentPeaje.putExtra("fecha",elegida.getFecha());
                        String[] datosPeaje = elegida.getCosto().split("&");
                        if(datosPeaje.length==2) {
                            intentPeaje.putExtra("costo", datosPeaje[0]);
                            intentPeaje.putExtra("ejes", datosPeaje[1]);
                        }
                        else{
                            intentPeaje.putExtra("costo", elegida.getCosto());
                            intentPeaje.putExtra("ejes", "0");
                        }
                        intentPeaje.putExtra("ciudades",getSupportActionBar().getSubtitle());
                        startActivity(intentPeaje);
                        break;
                    case "alimentacion":
                        Intent intentAlimentacion = new Intent(DetalleGastosActivity.this, AddAlimentacionActivity.class);
                        intentAlimentacion.putExtra("id",elegida.getCategoryId());
                        intentAlimentacion.putExtra("lugar",elegida.getLugar());
                        intentAlimentacion.putExtra("fecha",elegida.getFecha());
                        intentAlimentacion.putExtra("costo",elegida.getCosto());
                        intentAlimentacion.putExtra("tieneFoto",elegida.getTieneFoto());
                        startActivity(intentAlimentacion);
                        break;
                    case "gasolina":
                        Intent intentGasolina = new Intent(DetalleGastosActivity.this, AddGasolinaActivity.class);
                        intentGasolina.putExtra("id",elegida.getCategoryId());
                        intentGasolina.putExtra("lugar",elegida.getLugar());
                        intentGasolina.putExtra("fecha",elegida.getFecha());
                        String[] datos = elegida.getCosto().split("&");
                        if(datos.length==2) {
                            intentGasolina.putExtra("costo", datos[0]);
                            intentGasolina.putExtra("galones", datos[1]);
                        }
                        else{
                            intentGasolina.putExtra("costo", elegida.getCosto());
                            intentGasolina.putExtra("galones", "0");
                        }
                        startActivity(intentGasolina);
                        break;
                    default:
                        Intent intentOtro = new Intent(DetalleGastosActivity.this, AddOtroActivity.class);
                        intentOtro.putExtra("id",elegida.getCategoryId());
                        intentOtro.putExtra("lugar",elegida.getLugar());
                        intentOtro.putExtra("fecha",elegida.getFecha());
                        intentOtro.putExtra("costo",elegida.getCosto());
                        intentOtro.putExtra("concepto",elegida.getTipo());
                        startActivity(intentOtro);
                }
                //Toast.makeText(getApplicationContext(),"Position :"+position+"  ListItem : " +id +" queEs"+elegida.getCategoryId(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detalle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back_detalle) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        myToolbar = (Toolbar) findViewById(R.id.my_toolbarDG);

        setSupportActionBar(myToolbar);
        getSupportActionBar().setSubtitle("Destino");
        getSupportActionBar().setTitle("Detalle gastos");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot oneTrip: dataSnapshot.getChildren()){
                    String origenS=oneTrip.child("origen").getValue(String.class);
                    String destinoS=oneTrip.child("destino").getValue(String.class);
                    getSupportActionBar().setSubtitle(origenS+"-"+destinoS);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
