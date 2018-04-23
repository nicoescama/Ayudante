package com.energismart.ayudante;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //Declaration EditTexts
    EditText editTextEmail;
    EditText editTextPassword;

    //Declaration TextInputLayout
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;

    //Declaration Progress Layout
    LinearLayout llProgress;

    //Declaration Button
    ImageView loginImage;
    ImageView registerImage;
    Button getPassword;
    //Firebase Auth Object used to login and create users and getting user info
    FirebaseAuth mAuth;
    //Firebase AuthStateListener which will listen for logout login event
    FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in now launch your home screen activity
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, Snackbar.LENGTH_LONG);

                }
            }
        };
        //initCreateAccountTextView();

        initViews();

        //set click event of login button

        hideProgress();

        loginImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check user input is correct or not
                if (validate()) {
                    //show Progress to user and disable button until we can verify credentials with firebase
                    showProgress();
                    loginImage.setEnabled(false);
                    //Get values from EditText fields
                    String Email = editTextEmail.getText().toString();
                    String Password = editTextPassword.getText().toString();

                    //Authenticate user
                    mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //hide progress on task is complete and enable button
                            hideProgress();
                            loginImage.setEnabled(true);
                            if (task.isSuccessful()) {
                                //our sign in task is successful but do not launch home Screen Activity from here
                                //After successful sign in  onAuthStateChanged method get triggered automatically inside
                                //check for current user and then launch Home Screen Activity
                                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    Snackbar.make(loginImage, "Se inició sesión", Snackbar.LENGTH_INDEFINITE).show();
                                }
                                else {
                                    Snackbar.make(loginImage, "Por favor verificar correo", Snackbar.LENGTH_INDEFINITE).show();
                                    FirebaseAuth.getInstance().signOut();
                                }
                            } else {
                                Snackbar.make(loginImage, "No se pudo iniciar sesión", Snackbar.LENGTH_INDEFINITE).show();
                            }
                        }
                    });
                }
            }
        });

        registerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        getPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();

                String correoE = editTextEmail.getText().toString();

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoE).matches()) {
                    textInputLayoutEmail.setError("Escribir el correo electrónico asociado");
                } else {
                    textInputLayoutEmail.setError(null);
                    auth.sendPasswordResetEmail(correoE)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar snackbar = Snackbar.make(getPassword, "Se ha enviado un correo para reestablecer", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Aceptar", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        FirebaseAuth.getInstance().signOut();
                                                        finish();
                                                    }
                                                });
                                        snackbar.setActionTextColor(Color.BLUE);
                                        snackbar.show();
                                    }
                                }
                            });
                }


            }
        });


    }

    //this method used to set Create account TextView text and click event( maltipal colors
    // for TextView yet not supported in Xml so i have done it programmatically)
  //  private void initCreateAccountTextView() {
      //  TextView textViewCreateAccount = (TextView) findViewById(R.id.textViewCreateAccount);
      //  textViewCreateAccount.setText(fromHtml("<font color='#ffffff'>I don't have account yet. </font><font color='#0c0099'>create one</font>"));
      //  textViewCreateAccount.setOnClickListener(new View.OnClickListener() {
      //      @Override
      //      public void onClick(View view) {
      //          Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        //        startActivity(intent);
       //     }
     //   });
   // }

    //this method is used to connect XML views to its Objects
    private void initViews() {
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        llProgress = (LinearLayout) findViewById(R.id.llProgress);
        loginImage = (ImageView) findViewById(R.id.photo_login);
        registerImage = (ImageView) findViewById(R.id.photo_register);
        getPassword =(Button) findViewById(R.id.buttonGetPassword);

    }

    //This method is for handling fromHtml method deprecation
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    //This method is used to validate input given by user
    public boolean validate() {
        boolean valid = false;

        //Get values from EditText fields
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();

        //Handling validation for Email field
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            valid = false;
            textInputLayoutEmail.setError("Por favor ingresar correo válido!");
        } else {
            valid = true;
            textInputLayoutEmail.setError(null);
        }

        //Handling validation for Password field
        if (Password.isEmpty()) {
            valid = false;
            textInputLayoutPassword.setError("Por favor ingrese una contraseña");
        } else {
            if (Password.length() > 5) {
                valid = true;
                textInputLayoutPassword.setError(null);
            } else {
                valid = false;
                textInputLayoutPassword.setError("La contraseña debe contener al menos 5 caracteres");
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
}

