package com.energismart.ayudante;

/**
 * Created by nicoescama on 01/02/2018.
 */

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends AppCompatActivity{
    //Deceleration of Email Textview
    TextView textViewEmail;
    //Deceleration of logout Button
    Button buttonLogout;
    Button buttonAddTruck;
    Button buttonTrip;
    Button buttonContinue;
    Toolbar myToolbar;
    private DatabaseReference mDatabase;


    //Firebase Auth Object used to login and create users and getting user info
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setSupportActionBar(myToolbar);

        mAuth = FirebaseAuth.getInstance();
        //get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getSupportActionBar().setSubtitle(currentUser.getEmail());
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(currentUser.getUid()).child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    getSupportActionBar().setTitle(snapshot.getValue().toString());
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

        buttonAddTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTruckActivity.class);
                startActivity(intent);
                //finish();
            }
        });
        buttonTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference();
                final String userKey9 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final List<String> actual = new ArrayList<String>();
                myRef.child("users").child(userKey9).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot thisTrip: dataSnapshot.getChildren()){
                            actual.add(thisTrip.getKey());
                        }
                        if(actual.isEmpty()){

                            final List<String> camiones = new ArrayList<String>();
                            myRef.child("users").child(userKey9).child("trucks").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot camionAct : dataSnapshot.getChildren()) {
                                        camiones.add(camionAct.getKey());
                                    }
                                    if(camiones.isEmpty()){
                                        Snackbar snackbar = Snackbar.make(buttonContinue, "Debe registrar al menos un vehículo", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Aceptar", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                    }
                                                });
                                        snackbar.show();
                                    }
                                    else{
                                        Intent intent = new Intent(MainActivity.this, NewTripActivity.class);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            //finish();
                        }
                        else{
                            Snackbar snackbar = Snackbar.make(buttonContinue, "Ya existe un viaje en curso", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });
                            snackbar.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference();
                final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final List<String> actual = new ArrayList<String>();
                myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot thisTrip: dataSnapshot.getChildren()){
                         actual.add(thisTrip.getKey());
                        }
                        if(!actual.isEmpty()){
                            Intent intent = new Intent(MainActivity.this, ContinueTripActivity.class);
                            startActivity(intent);
                            //finish();
                        }
                        else{
                            Snackbar snackbar = Snackbar.make(buttonContinue, "Debe crear primero un viaje", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });
                            snackbar.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            //Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            String userKeyP = FirebaseAuth.getInstance().getCurrentUser().getUid();
            myRef.child("users").child(userKeyP).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot usr) {
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    intent.putExtra("userKey",usr.getKey());
                    intent.putExtra("email",usr.child("email").getValue(String.class));
                    intent.putExtra("licenseDueTo",usr.child("licenseDueTo").getValue(String.class));
                    intent.putExtra("licenseType",usr.child("licenseType").getValue(String.class));
                    intent.putExtra("name",usr.child("name").getValue(String.class));
                    intent.putExtra("phoneNumber",usr.child("phoneNumber").getValue(String.class));
                    intent.putExtra("profileUser",usr.child("profileUser").getValue(String.class));
                    startActivity(intent);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        return super.onOptionsItemSelected(item);
    }

    public void init(){
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonAddTruck = (Button) findViewById(R.id.buttonAddTruck);
        buttonTrip = (Button) findViewById(R.id.buttonTrip);
        buttonContinue = (Button) findViewById(R.id.buttonContinue);
    }
}
