package com.energismart.ayudante;

import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText newPasswordEditText, currentPasswordEditText;
    ImageView aceptar, cancelar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        init();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail().toString();
                String currentPassword = currentPasswordEditText.getText().toString();

                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, currentPassword);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String newPassword = newPasswordEditText.getText().toString();
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Snackbar snackbar = Snackbar.make(aceptar, "Se ha Cambiado correctamente la contraseña", Snackbar.LENGTH_INDEFINITE)
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
                        });



            }
        });
    }

    public void init(){
        newPasswordEditText = (EditText)findViewById(R.id.editTextNewPassword);
        currentPasswordEditText = (EditText) findViewById(R.id.editTextCurrentPassword);
        aceptar = (ImageView)findViewById(R.id.photo_confirmNewPassword);
        cancelar = (ImageView)findViewById(R.id.photo_cancelNewPassword);
    }
}
