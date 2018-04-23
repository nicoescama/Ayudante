package com.energismart.ayudante;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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

public class AddOtroActivity extends AppCompatActivity {

    ImageView addImage;
    ImageView cancelImage;
    ImageView fechaImage;
    ImageView photoImage;
    EditText lugar, costo, concepto;
    TextView fechaTextView;

    String mCurrentPhotoPath="";
    static final int REQUEST_TAKE_PHOTO = 1;

    String costoChange, lugarChange, fechaChange,idChange,conceptoChange;
    boolean isChange=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_otro);
        init();
        mCurrentPhotoPath="";

        if(getIntent().getExtras()!=null){
            updateFields();
        }


        cancelImage.setOnClickListener(new View.OnClickListener() {
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
                Context context = AddOtroActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(AddOtroActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePickerListener,
                        currDate.year,  currDate.month, currDate.monthDay);
                datePickerDialog.show();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (!isChange) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference();
                        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final List<String> tripsGastos = new ArrayList<String>();
                        final int gasto = Integer.parseInt(costo.getText().toString());
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
                                DatabaseReference newHospedajeRef = myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("otros").push();
                                String newGastoKey = newHospedajeRef.getKey();
                                Map<String, Object> childUpdates = new HashMap<>();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("lugar", lugar.getText().toString());
                                result.put("fecha", fechaTextView.getText().toString());
                                result.put("costo", Integer.toString(gasto));
                                result.put("concepto", concepto.getText().toString());
                                if (!mCurrentPhotoPath.isEmpty()) {
                                    result.put("foto", true);
                                }
                                else{
                                    result.put("foto", false);
                                }
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("otros").child(newGastoKey).setValue(result);
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("otros").child(newGastoKey).setValue(result);

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
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference();
                        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final List<String> tripsGastos = new ArrayList<String>();
                        final int gasto = Integer.parseInt(costo.getText().toString())-Integer.parseInt(costoChange);
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
                                //DatabaseReference newHospedajeRef = myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("otros").push();
                                //String newGastoKey = newHospedajeRef.getKey();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("lugar", lugar.getText().toString());
                                result.put("fecha", fechaTextView.getText().toString());
                                result.put("costo", costo.getText().toString());
                                result.put("concepto", concepto.getText().toString());
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("otros").child(idChange).setValue(result);
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("otros").child(idChange).setValue(result);
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

    public void init(){
        addImage = (ImageView) findViewById(R.id.photo_confirmOtro);
        cancelImage = (ImageView) findViewById(R.id.photo_cancelOtro);
        fechaImage = (ImageView) findViewById(R.id.today_otro);
        photoImage = (ImageView) findViewById(R.id.photo_otro);
        lugar = (EditText) findViewById(R.id.editTextLugarOtro);
        costo = (EditText) findViewById(R.id.editTextCostoOtro);
        concepto = (EditText) findViewById(R.id.editTextConceptoOtro);
        fechaTextView = (TextView) findViewById(R.id.textViewDateOtro);
    }

    public boolean validate(){
        boolean pasa = true;
        String lugarAc= lugar.getText().toString();
        String gasto = costo.getText().toString();
        String fecha = fechaTextView.getText().toString();
        String conceptoAc = concepto.getText().toString();

        if(lugarAc.isEmpty() || gasto.isEmpty() || fecha.isEmpty() || conceptoAc.isEmpty()){
            Snackbar snackbar = Snackbar.make(addImage, "Completar los espacios solicitados", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
            pasa = false;
        }

        return pasa;
    }

    public void updateFields(){
        lugarChange = getIntent().getStringExtra("lugar");
        costoChange = getIntent().getStringExtra("costo");
        fechaChange = getIntent().getStringExtra("fecha");
        idChange = getIntent().getStringExtra("id");
        conceptoChange=getIntent().getStringExtra("concepto");

        lugar.setText(lugarChange);
        fechaTextView.setText(fechaChange);
        costo.setText(costoChange);
        concepto.setText(conceptoChange);
        isChange=true;
        //addButton.setText("actualizar");
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
