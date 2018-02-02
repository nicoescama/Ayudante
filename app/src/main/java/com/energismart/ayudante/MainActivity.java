package com.energismart.ayudante;

/**
 * Created by nicoescama on 01/02/2018.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity{
    //Deceleration of Email Textview
    TextView textViewEmail;
    //Deceleration of logout Button
    Button buttonLogout;

    //Firebase Auth Object used to login and create users and getting user info
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        mAuth = FirebaseAuth.getInstance();
        //get current user
        FirebaseUser CurrentUser = mAuth.getCurrentUser();
        if (CurrentUser != null) {
            textViewEmail.setText(CurrentUser.getEmail());
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
