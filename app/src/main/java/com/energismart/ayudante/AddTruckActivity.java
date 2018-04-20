package com.energismart.ayudante;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTruckActivity extends AppCompatActivity {

    //Declaration EditTexts
    EditText editTextPlaca;
    EditText editTextClase;
    EditText editTextLinea;
    EditText editTextModelo;
    EditText editTextCilindraje;
    EditText editTextEjesCuantos;
    EditText editTextAgregar;

    //Declaration TextInputLayout
    TextInputLayout textInputLayoutPlaca;
    TextInputLayout textInputLayoutClase;
    TextInputLayout textInputLayoutLinea;
    TextInputLayout textInputLayoutModelo;
    TextInputLayout textInputLayoutCilindraje;
    TextInputLayout textInputLayoutEjesCuantos;
    TextInputLayout textInputLayoutAgregar;

    //Declaration Progress Layout
    LinearLayout llProgressTruck;

    TextView titlePlacas;
    TextView dateSOAT;

    //Declaration Button
    ImageView addImage,backImage;
    Button buttonSOAT;

    //Firebase Auth Object used to login and create users and getting user info
    FirebaseAuth mAuth;
    //Firebase AuthStateListener which will listen for logout login event
    FirebaseAuth.AuthStateListener mAuthListener;

    Spinner spinnerAdd;
    Spinner spinnerPlacas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_truck);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                }
                else{
                    // User is signed in
                     new Handler().postDelayed(new Runnable() {
                        @Override
                         public void run() {
                             Intent intent = new Intent(AddTruckActivity.this, LoginActivity.class);
                             startActivity(intent);
                             finish();
                         }
                     }, Snackbar.LENGTH_LONG);

                }
            }
        };
        initViews();

        buttonSOAT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Time currDate = new Time(Time.getCurrentTimezone());
                currDate.setToNow();
                DatePickerDialog.OnDateSetListener datePickerListener =
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay)
                            {
                                dateSOAT.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                            }
                        };
                Context context = AddTruckActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(AddTruckActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePickerListener,
                        currDate.year,  currDate.month, currDate.monthDay);
                datePickerDialog.show();
            }
            });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                addImage.setVisibility(View.GONE);
                backImage.setVisibility(View.GONE);
                if (validate()) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        String truckProfile = spinnerAdd.getSelectedItem().toString();
                        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if (truckProfile.equalsIgnoreCase("registrar")) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference();
                            String placa = editTextPlaca.getText().toString();
                            String clase = editTextClase.getText().toString();
                            String linea = editTextLinea.getText().toString();
                            String modelo = editTextModelo.getText().toString();
                            String cilindraje = editTextCilindraje.getText().toString();
                            String ejesCuantos = editTextEjesCuantos.getText().toString();
                            String soat = dateSOAT.getText().toString();
                            Truck newTruck = new Truck(placa, clase, linea, modelo, cilindraje, ejesCuantos, soat, userKey);
                            Map<String, Object> userValues = newTruck.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            DatabaseReference postRef = myRef.child("trucks");
                            DatabaseReference newPostRef = postRef.push();
                            String newKey = newPostRef.getKey();
                            childUpdates.put("/trucks/" + newKey, userValues);
                            myRef.updateChildren(childUpdates);

                            DatabaseReference refOwner = myRef.child("users");
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put(userKey + "/trucks/"+newKey, userValues);
                            refOwner.updateChildren(userUpdates);
                            Snackbar snackbarAdded = Snackbar.make(addImage, "Se agregó camión correctamente", Snackbar.LENGTH_INDEFINITE)
                                    .setActionTextColor(Color.GREEN).setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(AddTruckActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, Snackbar.LENGTH_INDEFINITE);
                                        }
                                    });
                            snackbarAdded.setActionTextColor(Color.BLUE);
                            snackbarAdded.show();

                        } else {

                            Snackbar snackbar = Snackbar.make(addImage, "No se agrego camion", Snackbar.LENGTH_INDEFINITE)
                                    .setActionTextColor(Color.GREEN).setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(AddTruckActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, Snackbar.LENGTH_INDEFINITE);
                                        }
                                    });
                            snackbar.setActionTextColor(Color.BLUE);
                            snackbar.show();
                        }

                    }
                }
                hideProgress();
                addImage.setEnabled(true);
            }
        });

        spinnerAdd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                switch (((String) parent.getSelectedItem()).toLowerCase()) {
                    case "registrar":
                        spinnerPlacas.setVisibility(View.GONE);
                        titlePlacas.setVisibility(View.GONE);
                        textInputLayoutAgregar.setVisibility(View.GONE);

                        textInputLayoutPlaca.setVisibility(View.VISIBLE);
                        textInputLayoutClase.setVisibility(View.VISIBLE);
                        textInputLayoutLinea.setVisibility(View.VISIBLE);
                        textInputLayoutModelo.setVisibility(View.VISIBLE);
                        textInputLayoutCilindraje.setVisibility(View.VISIBLE);
                        dateSOAT.setVisibility(View.VISIBLE);
                        buttonSOAT.setVisibility(View.VISIBLE);
                        //addImage.setText("Registrar camión");
                        textInputLayoutEjesCuantos.setVisibility(View.VISIBLE);
                        break;
                    case "asociar":
                        //Query zonesQuery = zonesRef.orderByChild("ZCODE").equalTo("ECOR");
                        //zonesQuery.addValueEventListener(new ValueEventListener() {
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
                                spinnerPlacas.setAdapter(adapterPlacas);
                                spinnerPlacas.setPrompt(getResources().getString(R.string.placas_title));
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                // ...
                            }
                        });

                        spinnerPlacas.setVisibility(View.VISIBLE);
                        titlePlacas.setVisibility(View.VISIBLE);
                        textInputLayoutAgregar.setVisibility(View.VISIBLE);

                        textInputLayoutPlaca.setVisibility(View.GONE);
                        textInputLayoutClase.setVisibility(View.GONE);
                        textInputLayoutLinea.setVisibility(View.GONE);
                        textInputLayoutModelo.setVisibility(View.GONE);
                        textInputLayoutCilindraje.setVisibility(View.GONE);
                        textInputLayoutEjesCuantos.setVisibility(View.GONE);
                        dateSOAT.setVisibility(View.GONE);
                        buttonSOAT.setVisibility(View.GONE);
                        //buttonAdd.setText("Asociar");
                        break;
                    }

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void initViews() {
        editTextPlaca = (EditText) findViewById(R.id.editTextPlaca);
        editTextClase = (EditText) findViewById(R.id.editTextClase);
        editTextLinea = (EditText) findViewById(R.id.editTextLinea);
        editTextModelo = (EditText) findViewById(R.id.editTextModelo);
        editTextCilindraje = (EditText) findViewById(R.id.editTextCilindraje);
        editTextEjesCuantos = (EditText) findViewById(R.id.editTextEjes);
        editTextAgregar = (EditText) findViewById(R.id.editTextCAgregar);

        titlePlacas = (TextView) findViewById(R.id.textViewPlacas);
        dateSOAT = (TextView) findViewById(R.id.textViewSOAT);

        textInputLayoutPlaca = (TextInputLayout) findViewById(R.id.textInputLayoutPlaca);
        textInputLayoutClase = (TextInputLayout) findViewById(R.id.textInputLayoutClase);
        textInputLayoutLinea = (TextInputLayout) findViewById(R.id.textInputLayoutLinea);
        textInputLayoutModelo = (TextInputLayout) findViewById(R.id.textInputLayoutModelo);
        textInputLayoutCilindraje = (TextInputLayout) findViewById(R.id.textInputLayoutCilindraje);
        textInputLayoutEjesCuantos = (TextInputLayout) findViewById(R.id.textInputLayoutEjes);
        textInputLayoutAgregar = (TextInputLayout) findViewById(R.id.textInputLayoutCAgregar);

        llProgressTruck = (LinearLayout) findViewById(R.id.llProgressTruck);
        addImage = (ImageView) findViewById(R.id.photo_confirmTruck);
        buttonSOAT = (Button) findViewById(R.id.buttonSOAT);
        spinnerAdd = (Spinner) findViewById(R.id.newTruck_spinner);
        spinnerPlacas = (Spinner) findViewById(R.id.placas_spinner);
        //titleLicense = (TextView) findViewById(R.id.textViewLicense);

        backImage = (ImageView) findViewById(R.id.photo_cancelTruck);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.newTruck_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // Apply the adapter to the spinner
        spinnerAdd.setAdapter(adapter);
        spinnerAdd.setPrompt(getResources().getString(R.string.newTruck_title));

    }

    public boolean validate(){

        Boolean valid = true;

        String placaNueva = editTextPlaca.getText().toString();
        //String clase = editTextClase.getText().toString();
        //String linea = editTextLinea.getText().toString();
        //String modelo = editTextModelo.getText().toString();
        //String cilindraje = editTextCilindraje.getText().toString();
        String ejesCuantos = editTextEjesCuantos.getText().toString();
        String soat = dateSOAT.getText().toString();
        String correoAgregar = editTextAgregar.getText().toString();


        if (spinnerAdd.getSelectedItem().toString().equalsIgnoreCase("registrar")) {
            /*String[] soatsplit=soat.split("-");
            if(soatsplit.length==3) {
                if (soatsplit[0].length() == 2  && soatsplit[1].length() == 2 && soatsplit[2].length() == 4) {
                    dateSOAT.setError(null);
                } else {
                    dateSOAT.setError("Selecciona la fecha final del SOAT");
                    valid = false;
                }
            }
            else{
                dateSOAT.setError("El vencimiento del SOAT en formato DD-MM-AAAA");
                valid = false;
            }*/
            if(!soat.isEmpty())
            {
                dateSOAT.setError(null);
            }else{
                dateSOAT.setError("Selecciona la fecha final del SOAT");
                valid = false;
            }

            if(!placaNueva.isEmpty()){
                textInputLayoutPlaca.setError(null);
            }  else{
                textInputLayoutPlaca.setError("La placa no puede estar vacia");
                valid=false;
            }
            if(!ejesCuantos.isEmpty()){
                textInputLayoutEjesCuantos.setError(null);
            }
            else{
                valid=false;
                textInputLayoutEjesCuantos.setError("El número de ejes se usará para pago de peajes");
            }
        }

        else if(spinnerAdd.getSelectedItem().toString().equalsIgnoreCase("asociar")){
            String placaAgregar = spinnerPlacas.getSelectedItem().toString();
            if(!placaAgregar.isEmpty()){

            } else{
                valid = false;
                Snackbar snackbar = Snackbar.make(addImage, "Seleccione una placa, debe haberla registrado", Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(Color.RED).setAction("Aceptar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                snackbar.setActionTextColor(Color.BLUE);
                snackbar.show();
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoAgregar).matches()) {
                valid = false;
                textInputLayoutAgregar.setError("Ingrese un correo válido");
            } else {
                textInputLayoutAgregar.setError(null);
            }


        }
        return valid;
    }

    public void showProgress() {
        llProgressTruck.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        llProgressTruck.setVisibility(View.GONE);
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


}
