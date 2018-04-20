package com.energismart.ayudante;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContinueTripActivity extends AppCompatActivity {

    ImageView backImage;
    ImageView finalizarImage;
    ImageView newPeajeImage;
    ImageView newGasolinaImage;
    ImageView newHospedajeImage;
    ImageView newAlimentacionImage;
    ImageView newOtroImage;
    ImageView detalleGastosImage;
    TextView presupuestoTextview;
    String mostrar;
    Toolbar myToolbar;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue_trip);

        init();

        setSupportActionBar(myToolbar);
        mAuth = FirebaseAuth.getInstance();
        //get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser = mAuth.getCurrentUser();
            getSupportActionBar().setSubtitle(currentUser.getEmail());
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(currentUser.getUid()).child("currentTrip").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for(DataSnapshot curr: snapshot.getChildren()) {
                        String inicio = curr.child("origen").getValue().toString();
                        String destino = curr.child("destino").getValue().toString();
                        getSupportActionBar().setTitle(inicio+"-"+destino);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        backImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ContinueTripActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
        );

        newPeajeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContinueTripActivity.this, AddPeajeActivity.class);
                intent.putExtra("ciudades",getSupportActionBar().getTitle() );
                startActivity(intent);
            }
        });

        newHospedajeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContinueTripActivity.this, AddHospedajeActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        newGasolinaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContinueTripActivity.this, AddGasolinaActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        newAlimentacionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContinueTripActivity.this, AddAlimentacionActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        newOtroImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContinueTripActivity.this, AddOtroActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        detalleGastosImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContinueTripActivity.this, DetalleGastosActivity.class);
                startActivity(intent);
            }
        });



        finalizarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Crea popup
                final AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(ContinueTripActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(ContinueTripActivity.this);
                }
                builder.setTitle("Enviar reporte")
                        .setMessage("No se podrán hacer modificaciones adicionales al reporte")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference myRef = database.getReference();
                                final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot thisTrip: dataSnapshot.getChildren()){
                                            Trip finishTrip = new Trip(thisTrip.getValue(Trip.class));
                                            String newKey = thisTrip.getKey();
                                            thisTrip.getRef().removeValue();
                                            Map<String, Object> userValues = finishTrip.toMap();
                                            Map<String, Object> userUpdates = new HashMap<>();
                                            DatabaseReference refTrips = myRef.child("users").child(userKey).child("trips");
                                            userUpdates.put(userKey + "/trips/"+newKey, userValues);
                                            //refTrips.updateChildren(userUpdates);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                Intent intent = new Intent(ContinueTripActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                 //do nothing
                          }
                      })
                      .setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
            }

        });

    }

    public void init(){
        backImage = (ImageView) findViewById(R.id.photo_ContinueBack);
        newPeajeImage = (ImageView) findViewById(R.id.photo_addPeaje);
        newGasolinaImage = (ImageView) findViewById(R.id.photo_addGasolina);
        newHospedajeImage = (ImageView) findViewById(R.id.photo_addGastoHospedaje);
        newAlimentacionImage = (ImageView) findViewById(R.id.photo_addAlimentacion);
        newOtroImage = (ImageView) findViewById(R.id.photo_addGastoOtro);
        finalizarImage = (ImageView) findViewById(R.id.photo_continueFinish);
        detalleGastosImage = (ImageView) findViewById(R.id.photo_DetalleGastos);
        presupuestoTextview = (TextView) findViewById(R.id.textViewGasto);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbarCT);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final List<String> tripsPres = new ArrayList<String>();
        final List<String> tripsGastos = new ArrayList<String>();
        myRef.child("users").child(userKey).child("currentTrip").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot oneTrip: dataSnapshot.getChildren()){
                    tripsPres.add(oneTrip.child("anticipo").getValue(String.class));
                    tripsGastos.add(oneTrip.child("gastos").getValue(String.class));
                    int van=tripsPres.size();
                    int antic = Integer.parseInt(tripsPres.get(van-1));
                    int gast = Integer.parseInt(tripsGastos.get(van-1));
                    int aunInt = antic - gast;
                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                    String presupuestoString = format.format(aunInt);
                    presupuestoTextview.setText(presupuestoString);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //String presupuestoString =tripsPres.get(0);

    }

    public void setMostar(String mostrar){
        this.mostrar=mostrar;
    }

}
