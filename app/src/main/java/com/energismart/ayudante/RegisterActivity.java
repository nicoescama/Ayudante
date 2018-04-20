package com.energismart.ayudante;

/**
 * Created by nicoescama on 01/02/2018.
 */


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    //Declaration EditTexts
    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextName;
    EditText editTextPhone;
    TextView textViewLicense;


    //Declaration TextInputLayout
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;
    TextInputLayout textInputLayoutName;
    TextInputLayout textInputLayoutPhone;
    Button buttonDueTo;



    //Declaration Progress Layout
    LinearLayout llProgress;

    //Declaration Button
    Button buttonRegister;

    //Firebase Auth Object used to login and create users and getting user info
    FirebaseAuth mAuth;
    //Firebase AuthStateListener which will listen for logout login event
    FirebaseAuth.AuthStateListener mAuthListener;


    Spinner spinnerLicense;
    Spinner spinnerProfile;
    TextView titleLicense;

    String userKeyChange, nameChange, phoneNumberChange, licenseDueToChange;
    String licenseTypeChange, emailChange, profileUserChange;

    boolean isChange;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                   // new Handler().postDelayed(new Runnable() {
                    //    @Override
                   //     public void run() {
                   //         Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                   //         startActivity(intent);
                   //         finish();
                   //     }
                   // }, Snackbar.LENGTH_LONG);

                }
            }
        };

        initTextViewLogin();
        initViews();

        if(getIntent().getExtras()!=null){
            updateFields();
        }

        // /*

        buttonDueTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Time currDate = new Time(Time.getCurrentTimezone());
                currDate.setToNow();
                DatePickerDialog.OnDateSetListener datePickerListener =
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay)
                            {
                                textViewLicense.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                            }
                        };
                Context context = RegisterActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePickerListener,
                        currDate.year,  currDate.month, currDate.monthDay);
                datePickerDialog.show();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    //show Progress to user disable button until we can verify credentials with firebase
                    showProgress();
                    buttonRegister.setEnabled(false);
                    if (!isChange) {
                        String Email = editTextEmail.getText().toString();
                        String Password = editTextPassword.getText().toString();
                        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //hide progress on task is complete and enable button
                                hideProgress();
                                buttonRegister.setEnabled(true);
                                if (task.isSuccessful()) {
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference();
                                        String email = editTextEmail.getText().toString();
                                        String name = editTextName.getText().toString();
                                        String phone = editTextPhone.getText().toString();
                                        String profileUser = spinnerProfile.getSelectedItem().toString();
                                        String dueTo = "";
                                        String licenseType = "";
                                        if (profileUser.equalsIgnoreCase("conductor")) {
                                            dueTo = textViewLicense.getText().toString();
                                            licenseType = spinnerLicense.getSelectedItem().toString();
                                        }
                                        User newUser = new User(userKey, name, email, phone, profileUser, licenseType, dueTo);
                                        Map<String, Object> userValues = newUser.toMap();
                                        Map<String, Object> childUpdates = new HashMap<>();
                                        childUpdates.put("/users/" + userKey, userValues);
                                        myRef.updateChildren(childUpdates);
                                    }
                                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                    FirebaseAuth.getInstance().signOut();
                                    Snackbar snackbar = Snackbar.make(buttonRegister, "Usuario creado, favor verificar en el correo", Snackbar.LENGTH_INDEFINITE)
                                            .setActionTextColor(Color.GREEN).setAction("Aceptar", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }, Snackbar.LENGTH_LONG);
                                                }
                                            });
                                    snackbar.setActionTextColor(Color.BLUE);
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(buttonRegister, "No se pudo crear cuenta", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Aceptar", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            });
                                    snackbar.show();
                                }
                            }
                        });
                    }
                    //modificacion de perfil
                    else{
                        String userKey = userKeyChange;
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                        String email = editTextEmail.getText().toString();
                        String name = editTextName.getText().toString();
                        String phone = editTextPhone.getText().toString();
                        String profileUser = spinnerProfile.getSelectedItem().toString();
                        String dueTo = "";
                        String licenseType = "";
                        if (profileUser.equalsIgnoreCase("conductor")) {
                            dueTo = textViewLicense.getText().toString();
                            licenseType = spinnerLicense.getSelectedItem().toString();
                        }
                        User newUser = new User(userKey, name, email, phone, profileUser, licenseType, dueTo);
                        Map<String, Object> userValues = newUser.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + userKey, userValues);
                        myRef.updateChildren(childUpdates);
                        Snackbar snackbar = Snackbar.make(buttonRegister, "Usuario modificado correctamente", Snackbar.LENGTH_INDEFINITE)
                                .setActionTextColor(Color.GREEN).setAction("Aceptar", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, Snackbar.LENGTH_LONG);
                                    }
                                });
                        snackbar.setActionTextColor(Color.BLUE);
                        snackbar.show();
                    }
                }
            }
        });

        //Crea popup
        //final AlertDialog.Builder builder;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    builder = new AlertDialog.Builder(RegisterActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        //} else {
        //    builder = new AlertDialog.Builder(RegisterActivity.this);
        //}
        //builder.setTitle("Delete entry")
        //        .setMessage("Are you sure you want to delete this entry?")
        //        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        //            public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
        //            }
        //        })
         //       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
          //          public void onClick(DialogInterface dialog, int which) {
                        // do nothing
          //          }
          //      })
          //      .setIcon(android.R.drawable.ic_dialog_alert);


        spinnerProfile.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                switch (((String) parent.getSelectedItem()).toLowerCase()) {
                    case "conductor":
                        spinnerLicense.setVisibility(View.VISIBLE);
                        titleLicense.setVisibility(View.VISIBLE);
                        buttonDueTo.setVisibility(View.VISIBLE);
                        textViewLicense.setVisibility(View.VISIBLE);
                       // builder.setMessage((String) parent.getSelectedItem())
                       // .show();
                        break;
                    case "dueño":
                        spinnerLicense.setVisibility(View.GONE);
                        titleLicense.setVisibility(View.GONE);
                        buttonDueTo.setVisibility(View.GONE);
                        textViewLicense.setVisibility(View.GONE);
                        // builder.setMessage((String) parent.getSelectedItem())
                        //        .show();
                        break;
                    case "admin":
                        spinnerLicense.setVisibility(View.GONE);
                        titleLicense.setVisibility(View.GONE);
                        buttonDueTo.setVisibility(View.GONE);
                        textViewLicense.setVisibility(View.GONE);
                        //builder.setMessage((String) parent.getSelectedItem())
                         //       .show();
                }

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
// */
    }

   // manage three screens

    private void initTextViewLogin() {
        TextView textViewLogin = (TextView) findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //this method is used to connect XML views to its Objects
    private void initViews() {

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        textViewLicense = (TextView) findViewById(R.id.textViewDateLicense);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        textInputLayoutName = (TextInputLayout) findViewById(R.id.textInputLayoutName);
        textInputLayoutPhone = (TextInputLayout) findViewById(R.id.textInputLayoutPhone);
        buttonDueTo = (Button) findViewById(R.id.buttonDateLicense);
        llProgress = (LinearLayout) findViewById(R.id.llProgress);

        buttonRegister = (Button) findViewById(R.id.buttonRegisterR);
        spinnerLicense = (Spinner) findViewById(R.id.license_spinnerR);
        spinnerProfile = (Spinner) findViewById(R.id.profile_spinnerR);
        titleLicense = (TextView) findViewById(R.id.textViewLicense);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.license_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterProfile = ArrayAdapter.createFromResource(this,
                R.array.profile_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterProfile.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLicense.setAdapter(adapter);
        spinnerLicense.setPrompt(getResources().getString(R.string.license_title));
        spinnerProfile.setAdapter(adapterProfile);
        spinnerProfile.setPrompt(getResources().getString(R.string.profile_title));

    }


    public void updateFields(){
        userKeyChange = getIntent().getStringExtra("userKey");
        nameChange = getIntent().getStringExtra("name");
        emailChange = getIntent().getStringExtra("email");
        licenseDueToChange = getIntent().getStringExtra("licenseDueTo");
        licenseTypeChange = getIntent().getStringExtra("licenseType");
        profileUserChange = getIntent().getStringExtra("profileUser");
        phoneNumberChange = getIntent().getStringExtra("phoneNumber");
        isChange = true;

        editTextEmail.setText(emailChange);
        editTextName.setText(nameChange);
        editTextPhone.setText(phoneNumberChange);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.license_array, android.R.layout.simple_spinner_item);
        spinnerLicense.setSelection(adapter.getPosition(licenseTypeChange));
        ArrayAdapter<CharSequence> adapterProfile = ArrayAdapter.createFromResource(this,
                R.array.profile_array, android.R.layout.simple_spinner_item);
        spinnerProfile.setSelection(adapterProfile.getPosition(profileUserChange));
        textViewLicense.setText(licenseDueToChange);
        buttonRegister.setText("Actualizar");
        editTextPassword.setText("estoNoSeUsa");
        textInputLayoutPassword.setVisibility(View.GONE);


    }

    //This method is used to validate input given by user
    public boolean validate() {
        boolean valid = true;

        //Get values from EditText fields
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();
        String name = editTextName.getText().toString();
        String phone = editTextPhone.getText().toString();
        String dueTo = textViewLicense.getText().toString();



        //Handling validation for Email field
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            valid = false;
            textInputLayoutEmail.setError("Favor ingresar email válido!");
        } else {
            textInputLayoutEmail.setError(null);
        }

        //Handling validation for Password field
        if (Password.isEmpty()) {
            valid = false;
            textInputLayoutPassword.setError("Favor ingresar contraseña válida!");
        } else {
            if (Password.length() > 5) {
                textInputLayoutPassword.setError(null);
            } else {
                valid = false;
                textInputLayoutPassword.setError("Contraseña debe contener al menos 6 caracteres!");
            }
        }

        //Handling validation for Name
        if (name.isEmpty()) {
            valid = false;
            textInputLayoutName.setError("Favor ingresar nombre!");
        } else {
            textInputLayoutName.setError(null);
            }

        //Handling validation for phone
        if (!Patterns.PHONE.matcher(phone).matches()) {
            valid = false;
            textInputLayoutPhone.setError("Favor ingresar un número de celular válido!");
        } else {
            textInputLayoutPhone.setError(null);
        }

        //Handling validation for dueTo
        if (spinnerProfile.getSelectedItem().toString().equalsIgnoreCase("conductor")) {
            String[] dueTosp=dueTo.split("-");
            if(dueTosp.length>0) {

            }
            else{
               valid=false;
            }
        }


        return valid;

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //Method to show progress
    public void showProgress() {
        llProgress.setVisibility(View.VISIBLE);
    }

    //Method to hide progress
    public void hideProgress() {
        llProgress.setVisibility(View.GONE);
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
