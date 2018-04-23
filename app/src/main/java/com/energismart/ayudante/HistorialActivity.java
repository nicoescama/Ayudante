package com.energismart.ayudante;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {


    Toolbar myToolbar;

    final ArrayList<Category> tripList = new ArrayList<Category>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        init();
        setSupportActionBar(myToolbar);

        final ListView lv = (ListView) findViewById(R.id.listViewHistorial);
        final Drawable imageDraw = getResources().getDrawable(R.drawable.ic_info_black_48dp);
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        myRef.child("users").child(userKey).child("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot oneTrip : dataSnapshot.getChildren()){
                    String keyTrip = oneTrip.getKey();
                    String anticipo = oneTrip.child("anticipo").getValue(String.class);
                    String destino = oneTrip.child("destino").getValue(String.class);
                    String fecha = oneTrip.child("fecha").getValue(String.class);
                    String gastos = oneTrip.child("gastos").getValue(String.class);
                    String origen = oneTrip.child("origen").getValue(String.class);
                    String camion = oneTrip.child("truck").getValue(String.class);
                    Boolean cerrado = oneTrip.child("finalizado").getValue(Boolean.class);
                    if(cerrado) {
                        Category catTrip = new Category("trip", origen + "-" + destino, fecha, keyTrip, imageDraw, camion, false);
                        tripList.add(catTrip);
                    }
                }
                AdapterCategory adapter = new AdapterCategory(HistorialActivity.this, tripList);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category elegida = (Category) parent.getItemAtPosition(position);
                Intent detalleIntent = new Intent(HistorialActivity.this,DetalleGastosActivity.class);
                detalleIntent.putExtra("key",elegida.getCosto());
                startActivity(detalleIntent);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_historial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back_historial) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        myToolbar = (Toolbar) findViewById(R.id.my_toolbarHistorial);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setSubtitle("Usuario");
        getSupportActionBar().setTitle("Historial de viajes");
    }
}
