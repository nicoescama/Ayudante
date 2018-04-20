package com.energismart.ayudante;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.format.Time;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewTripActivity extends AppCompatActivity{


    Button buttonDateNT, buttonTimeNT;
    Button buttonAdd, buttonBackNTrip;
    TextView txtDate, txtTime;

    TextInputLayout textInputLayoutAnticipo;

    EditText editTextCargue;
    EditText editTextDescargue;
    EditText editTextAnticipo;

    LinearLayout llProgress;

    Spinner spinnerPlacaNT;
    Spinner spinnerOrigen;
    Spinner spinnerDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        initViews();

       /* FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        myRef.child("trips").child("origins").child("Neiva").setValue(true);
        myRef.child("trips").child("origins").child("Bogotá").setValue(true);
        myRef.child("trips").child("origins").child("Paipa").setValue(true);
        myRef.child("trips").child("destinations").child("Neiva").setValue(true);
        myRef.child("trips").child("destinations").child("Bogotá").setValue(true);
        myRef.child("trips").child("destinations").child("Paipa").setValue(true);*/



        buttonDateNT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               // final Calendar c = Calendar.getInstance();
                //https://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app
                Time currDate = new Time(Time.getCurrentTimezone());
                currDate.setToNow();
                DatePickerDialog.OnDateSetListener datePickerListener =
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay)
                            {
                                txtDate.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                            }
                        };
                Context context = NewTripActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(NewTripActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                }
               DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePickerListener,
                        currDate.year,  currDate.month, currDate.monthDay);
                datePickerDialog.show();
            }
        });

        buttonTimeNT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Time currDate = new Time(Time.getCurrentTimezone());
                currDate.setToNow();
                TimePickerDialog.OnTimeSetListener timePickerListener =
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                txtTime.setText(selectedHour + ":" + selectedMinute);
                            }
                        };
                Context context = NewTripActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(NewTripActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,timePickerListener,
                        currDate.hour,currDate.minute,false);
                timePickerDialog.show();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                if (validate()) {
                    buttonAdd.setEnabled(false);
                    buttonAdd.setActivated(false);
                    buttonAdd.setClickable(false);
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        //String truckProfile = spinnerAdd.getSelectedItem().toString();
                        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference();
                        DatabaseReference myRefUsers = database.getReference("users");
                        DatabaseReference trucksRef = myRefUsers.child(userKey).child("trucks");
                        final String placa = spinnerPlacaNT.getSelectedItem().toString();
                        final String origen = spinnerOrigen.getSelectedItem().toString();
                        final String puntoCargue = editTextCargue.getText().toString();
                        final String destino = spinnerDestino.getSelectedItem().toString();
                        final String puntoDescargue = editTextDescargue.getText().toString();
                        final String fecha = txtDate.getText().toString();
                        final String hora = txtTime.getText().toString();
                        final List<String> truckKey = new ArrayList<String>();
                        final String anticipo = editTextAnticipo.getText().toString();
                        final List<String> placas = new ArrayList<String>();
                        myRef.child("users").child(userKey).child("trucks").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot truckSnapshot: dataSnapshot.getChildren()) {
                                    if(truckSnapshot.child("placa").getValue(String.class).equals(placa)) {
                                        truckKey.add(truckSnapshot.getKey());
                                    }

                                }
                                Trip newTrip = new Trip(placa, origen, puntoCargue, destino, puntoDescargue,anticipo,fecha,hora, userKey, truckKey.get(0));
                                Map<String, Object> userValues = newTrip.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                DatabaseReference postRef = myRef.child("trips");
                                DatabaseReference newPostRef = postRef.push();
                                String newKey = newPostRef.getKey();
                                childUpdates.put("/trips/" + newKey, userValues);
                                myRef.updateChildren(childUpdates);

                                DatabaseReference refOwner = myRef.child("users");
                                Map<String, Object> userUpdates = new HashMap<>();
                                userUpdates.put(userKey + "/currentTrip/"+newKey, userValues);
                                userUpdates.put(userKey + "/trips/"+newKey, userValues);
                                refOwner.updateChildren(userUpdates);
                                DatabaseReference refTruck = myRef.child("trucks");
                                Map<String, Object> truckUpdates = new HashMap<>();
                                truckUpdates.put(truckKey + "/trips/"+newKey, true);
                                refTruck.updateChildren(truckUpdates);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        Snackbar snackbarAdded = Snackbar.make(buttonAdd, "Se agregó viaje correctamente", Snackbar.LENGTH_INDEFINITE)
                                    .setActionTextColor(Color.GREEN).setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                }
                                            }, Snackbar.LENGTH_INDEFINITE);
                                        }
                                    });
                            snackbarAdded.setActionTextColor(Color.BLUE);
                            snackbarAdded.show();
                    }
                }
                hideProgress();
                buttonAdd.setEnabled(true);
            }
        });

        buttonBackNTrip.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        finish();
                                    }
                                }
        );

    }

    private void initViews() {

        buttonDateNT=(Button)findViewById(R.id.buttonDateNT);
        buttonTimeNT=(Button)findViewById(R.id.buttonTimeNT);
        buttonAdd=(Button)findViewById(R.id.buttonAddTripNT);
        txtDate=(TextView)findViewById(R.id.editTextDateNT);
        txtTime=(TextView)findViewById(R.id.editTextTimeNT);

        textInputLayoutAnticipo = (TextInputLayout)findViewById(R.id.textInputLayoutAnticipo);

        buttonBackNTrip = (Button) findViewById(R.id.buttonBackNTrip);

        llProgress = (LinearLayout)findViewById(R.id.llProgressNT);

        editTextAnticipo = (EditText)findViewById(R.id.editTextAnticipo);
        editTextCargue = (EditText)findViewById(R.id.editTextPCargue);
        editTextDescargue = (EditText)findViewById(R.id.editTextPDescargue);

        spinnerPlacaNT = (Spinner)findViewById(R.id.placaNT_spinner);
        spinnerOrigen = (Spinner)findViewById(R.id.origen_spinner);
        spinnerDestino= (Spinner)findViewById(R.id.destino_spinner);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final List<String> placas = new ArrayList<String>();
        myRef.child("users").child(userKey).child("trucks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot truckSnapshot: dataSnapshot.getChildren()) {
                    placas.add(truckSnapshot.child("placa").getValue(String.class));
                }
                ArrayAdapter adapterPlacas=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, android.R.id.text1, placas);
                adapterPlacas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlacaNT.setAdapter(adapterPlacas);
                spinnerPlacaNT.setPrompt(getResources().getString(R.string.placas_title));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        final List<String> origenes = new ArrayList<String>();
       myRef.child("trips").child("origins").addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               for (DataSnapshot origenSnapshot: dataSnapshot.getChildren()) {
                   origenes.add(origenSnapshot.getKey());
               }
               ArrayAdapter adapterOrigen=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, android.R.id.text1, origenes);
               adapterOrigen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
               spinnerOrigen.setAdapter(adapterOrigen);
               spinnerOrigen.setPrompt("Origen");
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

        final List<String> destinos = new ArrayList<String>();
        myRef.child("trips").child("destinations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot origenSnapshot: dataSnapshot.getChildren()) {
                    destinos.add(origenSnapshot.getKey());
                }
                ArrayAdapter adapterDestinos=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, android.R.id.text1, destinos);
                adapterDestinos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDestino.setAdapter(adapterDestinos);
                spinnerDestino.setPrompt("Destino");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private static boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1));
    }

    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }

    public void showProgress() {
        llProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        llProgress.setVisibility(View.GONE);
    }

    public boolean validate(){
        boolean valid=true;
        String anticipo = editTextAnticipo.getText().toString();
        if (anticipo.isEmpty()) {
            valid = false;
            textInputLayoutAnticipo.setError("Agregar anticipo!");
        } else {
            textInputLayoutAnticipo.setError(null);
        }

        return valid;
    }
}


