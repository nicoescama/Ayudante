package com.energismart.ayudante;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPeajeActivity extends AppCompatActivity {

    ImageView backImage, addImage, fechaImage, photoImage;

    Spinner numEjesSpinner, peajeSpinner;

    TextInputLayout pagoTextInputLayout;

    EditText pagoEditText, lugarEditText;

    TextInputLayout lugarTextInputLayOut;

    TextView ejesTitulo;

    int cat = 0;

    String costoChange, lugarChange, fechaChange,ejesChange,idChange, otroLugarChange;
    boolean isChange=false;

    String mCurrentPhotoPath="";
    static final int REQUEST_TAKE_PHOTO = 1;


    String ciudades="";
    String[] origDest;
    final List<List<String>> preciosPeajes = new ArrayList<List<String>>();

    String peajeActual;
    String ejesActual;
    TextView fechaTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_peaje);

        ciudades = getIntent().getStringExtra("ciudades");
        if(!ciudades.isEmpty()) {
            origDest = ciudades.split("-");
        }
        init();

        mCurrentPhotoPath="";
        /*pagoEditText.setText("");
        pagoEditText.setEnabled(true);
        numEjesSpinner.setVisibility(View.GONE);
        ejesTitulo.setVisibility(View.GONE);
        lugarTextInputLayOut.setVisibility(View.VISIBLE);*/


        peajeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
                if(preciosPeajes.isEmpty()){
                    //pagoEditText.setText("");
                    pagoEditText.setEnabled(true);
                    numEjesSpinner.setVisibility(View.GONE);
                    ejesTitulo.setVisibility(View.GONE);
                    lugarTextInputLayOut.setVisibility(View.VISIBLE);
                }
                else {
                    if (parent.getSelectedItem().toString().equals("otro")) {
                        //pagoEditText.setText("");
                        pagoEditText.setEnabled(true);
                        numEjesSpinner.setVisibility(View.GONE);
                        ejesTitulo.setVisibility(View.GONE);
                        lugarTextInputLayOut.setVisibility(View.VISIBLE);
                    } else {
                        peajeActual = String.valueOf(parent.getSelectedItemPosition());
                        String precioActualN = preciosPeajes.get(Integer.parseInt(peajeActual)).get(cat);
                        pagoEditText.setText(precioActualN);
                        numEjesSpinner.setVisibility(View.VISIBLE);
                        pagoEditText.setEnabled(false);
                        ejesTitulo.setVisibility(View.VISIBLE);
                        lugarTextInputLayOut.setVisibility(View.GONE);
                    }

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        numEjesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(preciosPeajes.isEmpty()){
                    //pagoEditText.setText("");
                    pagoEditText.setEnabled(true);
                    numEjesSpinner.setVisibility(View.GONE);
                }
                else {
                    ejesActual = adapterView.getSelectedItem().toString();
                    setCat();
                    String precioActualN = preciosPeajes.get(Integer.parseInt(peajeActual)).get(cat);
                    pagoEditText.setText(precioActualN);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        photoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                //galleryAddPic();
            }
        });

        fechaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Time currDate = new Time(Time.getCurrentTimezone());
                currDate.setToNow();
                DatePickerDialog.OnDateSetListener datePickerListener =
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay)
                            {
                                fechaTextView.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                            }
                        };
                Context context = AddPeajeActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(AddPeajeActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePickerListener,
                        currDate.year,  currDate.month, currDate.monthDay);
                datePickerDialog.show();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    if (!isChange) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference();
                        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final List<String> tripsGastos = new ArrayList<String>();
                        final int gasto = Integer.parseInt(pagoEditText.getText().toString());
                        final List<String> gastoAcumString = new ArrayList<String>();
                        final List<String> tripKey = new ArrayList<String>();
                        myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot oneTrip : dataSnapshot.getChildren()) {
                                    tripsGastos.add(oneTrip.child("gastos").getValue(String.class));
                                    tripKey.add(oneTrip.getKey());
                                    int gastoAcum = Integer.parseInt(tripsGastos.get(0));
                                    gastoAcum = gastoAcum + gasto;
                                    gastoAcumString.add(Integer.toString(gastoAcum));
                                }
                                String tKey = tripKey.get(0);
                                DatabaseReference newHospedajeRef = myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("peaje").push();
                                String newGastoKey = newHospedajeRef.getKey();
                                Map<String, Object> childUpdates = new HashMap<>();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("fecha", fechaTextView.getText().toString());
                                if (peajeSpinner.getSelectedItem().equals("otro")) {
                                    result.put("lugar",  "otro&"+lugarEditText.getText().toString());
                                } else {
                                    result.put("lugar", peajeSpinner.getSelectedItem().toString());
                                }
                                result.put("costo", Integer.toString(gasto));
                                if (!mCurrentPhotoPath.isEmpty()) {
                                    result.put("foto", true);
                                }
                                else{
                                    result.put("foto", false);
                                }
                                result.put("ejes", numEjesSpinner.getSelectedItem().toString());
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("peaje").child(newGastoKey).setValue(result);
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("peaje").child(newGastoKey).setValue(result);

                                //Subir foto
                                if (!mCurrentPhotoPath.isEmpty()) {
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    Uri fileToU = Uri.fromFile(new File(mCurrentPhotoPath));
                                    StorageReference riversRef = storageRef.child("images/" + userKey + "/" + tKey + "/" + newGastoKey + ".jpg");
                                    UploadTask subirFoto = riversRef.putFile(fileToU);

                                    // Register observers to listen for when the download is done or if it fails
                                    subirFoto.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        finish();
                    }
                    else{
                        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final List<String> tripsGastos = new ArrayList<String>();
                        final int gasto = Integer.parseInt(pagoEditText.getText().toString())-Integer.parseInt(costoChange);
                        final List<String> gastoAcumString = new ArrayList<String>();
                        final List<String> tripKey = new ArrayList<String>();
                        myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot oneTrip : dataSnapshot.getChildren()) {
                                    tripsGastos.add(oneTrip.child("gastos").getValue(String.class));
                                    tripKey.add(oneTrip.getKey());
                                    int gastoAcum = Integer.parseInt(tripsGastos.get(0));
                                    gastoAcum = gastoAcum + gasto;
                                    gastoAcumString.add(Integer.toString(gastoAcum));
                                }
                                String tKey = tripKey.get(0);
                                Map<String, Object> childUpdates = new HashMap<>();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("fecha", fechaTextView.getText().toString());
                                    if (peajeSpinner.getSelectedItem().equals("otro")) {
                                    result.put("lugar", "otro&"+lugarEditText.getText().toString());
                                } else {
                                    result.put("lugar", peajeSpinner.getSelectedItem().toString());
                                }
                                result.put("costo", pagoEditText.getText().toString());
                                result.put("ejes", numEjesSpinner.getSelectedItem().toString());
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("peaje").child(idChange).setValue(result);
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("peaje").child(idChange).setValue(result);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        finish();
                    }

                }
            }
        });
    }

    public boolean validate(){
        return true;
    }

    public void init(){
        backImage = (ImageView) findViewById(R.id.photo_cancelPeaje);
        addImage = (ImageView) findViewById(R.id.photo_confirmPeaje);
        fechaImage = (ImageView) findViewById(R.id.today_peaje);
        photoImage = (ImageView) findViewById(R.id.photo_peaje);
        ejesTitulo = (TextView) findViewById(R.id.textViewEjesPeaje);

        fechaTextView = (TextView) findViewById(R.id.textViewDatePeaje);
        lugarEditText = (EditText) findViewById(R.id.editTextOtroPeaje);
        lugarTextInputLayOut = (TextInputLayout) findViewById(R.id.textInputLayoutOtroPeaje);

        pagoEditText = (EditText)findViewById(R.id.editTextPagoPeaje);
        pagoTextInputLayout= (TextInputLayout)findViewById(R.id.textInputLayoutPagoPeaje);

        numEjesSpinner = (Spinner)findViewById(R.id.numeroEjes_spinner);
        peajeSpinner = (Spinner) findViewById(R.id.costo_spinner);


        if(getIntent().hasExtra("id")){
            idChange = getIntent().getStringExtra("id");
            costoChange = getIntent().getStringExtra("costo");
            fechaChange = getIntent().getStringExtra("fecha");
            pagoEditText.setText(costoChange);
            fechaTextView.setText(fechaChange);
            //addButton.setText("Actualizar");
            isChange=true;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final List<String> ejes = new ArrayList<String>();
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ct : dataSnapshot.getChildren()){
                    final String truckKey = ct.child("truck").getValue(String.class);
                    myRef.child("users").child(userKey).child("trucks").child(truckKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int cuantos = Integer.parseInt(dataSnapshot.child("ejesCuantos").getValue(String.class));
                            int cuantos_1 = cuantos - 1;
                            int cuantos_2 = cuantos - 2;
                            if (cuantos_2 > 0) {
                               ejes.add(Integer.toString(cuantos_2));
                            }
                            if (cuantos_1 > 0) {
                               ejes.add(Integer.toString(cuantos_1));
                            }
                            ejes.add(dataSnapshot.child("ejesCuantos").getValue(String.class));
                            ArrayAdapter adapterNumEjes = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, android.R.id.text1, ejes);
                            adapterNumEjes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            numEjesSpinner.setAdapter(adapterNumEjes);
                            numEjesSpinner.setPrompt("Número de ejes");
                            if(getIntent().hasExtra("ejes")){
                                ejesChange = getIntent().getStringExtra("ejes");
                                ejesActual = ejesChange;
                                int indEjes = adapterNumEjes.getPosition(ejesActual);
                                numEjesSpinner.setSelection(indEjes);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        String origDest_0 = "";
        String origDest_1 = "";
        final List<String> peajes = new ArrayList<String>();
        if(origDest.length==2) {
            origDest_0=origDest[0];
            origDest_1=origDest[1];
        }
        final String origenSS = origDest_0;
        final String destinoSS = origDest_1;
        myRef.child("trips").child("peajes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ciudad1 : dataSnapshot.getChildren()){
                     if(ciudad1.getKey().equals(origenSS)||ciudad1.getKey().equals(destinoSS)){
                         for(DataSnapshot ciudad2 : ciudad1.getChildren()){
                             if(ciudad2.getKey().equals(origenSS)||ciudad2.getKey().equals(destinoSS)){
                                 for(DataSnapshot peaje : ciudad2.getChildren()){
                                     peajes.add(peaje.getKey());
                                     List<String> precios = new ArrayList<String>();
                                     for(DataSnapshot precioCat : peaje.getChildren()){
                                         precios.add(precioCat.getValue().toString());
                                     }
                                     preciosPeajes.add(precios);
                                 }
                             }
                         }
                     }
                    peajes.add("otro");
                }
                ArrayAdapter adapterPeajes=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, android.R.id.text1, peajes);
                adapterPeajes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                peajeSpinner.setAdapter(adapterPeajes);
                peajeSpinner.setPrompt("Peaje");
                if(getIntent().hasExtra("lugar")){
                    lugarChange = getIntent().getStringExtra("lugar");
                    peajeActual = lugarChange;
                    int indPeaje = adapterPeajes.getPosition(peajeActual);
                    peajeSpinner.setSelection(indPeaje);
                    if(lugarChange.equals("otro")) {
                        otroLugarChange = getIntent().getStringExtra("lugarOtro");
                        lugarTextInputLayOut.setVisibility(View.VISIBLE);
                        lugarEditText.setText(otroLugarChange);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setCat(){
        if (ejesActual.equals("1")) {
            cat = 0;
        } else if (ejesActual.equals("2")) {
            cat=1;
        } else if (ejesActual.equals("3")) {
            cat=2;
        } else if (ejesActual.equals("4")) {
            cat=3;
        } else if (ejesActual.equals("5")) {
            cat=4;
        } else if (ejesActual.equals("6")) {
            cat=4;
        } else {
            cat=4;
        }
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.energismart.ayudante.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                File imgFile = new File(mCurrentPhotoPath);
                if (imgFile.exists()) {
                    //photo.setVisibility(View.VISIBLE);
                    Bitmap bitmapPic = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    int dimX = bitmapPic.getWidth();
                    int dimY = bitmapPic.getHeight();
                    if(dimX >= dimY){
                        int scale = dimX/1200;
                        bitmapPic = Bitmap.createScaledBitmap(bitmapPic, 1200, dimY/scale, false);
                    }
                    else{
                        int scale = dimY/1200;
                        bitmapPic = Bitmap.createScaledBitmap(bitmapPic, dimX/scale, 1200, false);

                    }
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmapPic.compress(Bitmap.CompressFormat.JPEG, 75, bytes);
                    File storageDir2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String timeStamp2 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File f = new File(storageDir2.getAbsolutePath() +"/"+ timeStamp2+".jpg");
                    try {
                        f.createNewFile();
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    File fileR =  new File(mCurrentPhotoPath);
                    fileR.delete();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    ByteArrayOutputStream blob = new ByteArrayOutputStream();
                    bitmapPic.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
                    byte[] bitmapdata = blob.toByteArray();
                    Bitmap myBitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                    photoImage.setImageBitmap(myBitmap);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
