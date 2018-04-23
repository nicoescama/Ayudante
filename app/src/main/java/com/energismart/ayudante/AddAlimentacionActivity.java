package com.energismart.ayudante;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddAlimentacionActivity extends AppCompatActivity {

    ImageView addImage;
    EditText lugar, costo;
    TextView fechaTextView;
    ImageView photo,fechaImagen,photoTomada;

    String costoChange, lugarChange, fechaChange,idChange;
    boolean tieneFotoChange;
    boolean isChange=false;

    Toolbar myToolbar;


    String mCurrentPhotoPath="";
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alimentacion);
        init();

        setSupportActionBar(myToolbar);

        if(getIntent().getExtras()!=null){
            updateFields();
        }

        mCurrentPhotoPath="";

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                //galleryAddPic();
            }
        });

        fechaImagen.setOnClickListener(new View.OnClickListener() {
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
                Context context = AddAlimentacionActivity.this;
                if (isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(AddAlimentacionActivity.this, android.R.style.Theme_Holo_Light_Dialog);
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
                                DatabaseReference newHospedajeRef = myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("alimentacion").push();
                                String newGastoKey = newHospedajeRef.getKey();
                                Map<String, Object> childUpdates = new HashMap<>();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("lugar", lugar.getText().toString());
                                result.put("fecha", fechaTextView.getText().toString());
                                result.put("costo", Integer.toString(gasto));
                                if (!mCurrentPhotoPath.isEmpty()) {
                                    result.put("foto", true);
                                }
                                else{
                                    result.put("foto", false);
                                }
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("alimentacion").child(newGastoKey).setValue(result);
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("alimentacion").child(newGastoKey).setValue(result);

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
                                //DatabaseReference newHospedajeRef = myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("alimentacion").push();
                                //String newGastoKey = newHospedajeRef.getKey();
                                Map<String, Object> childUpdates = new HashMap<>();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("lugar", lugar.getText().toString());
                                result.put("fecha", fechaTextView.getText().toString());
                                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                                format.setParseIntegerOnly(true);
                                try {
                                    result.put("costo", format.parse(costo.getText().toString()).toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if(!mCurrentPhotoPath.isEmpty()){
                                    result.put("foto",true);
                                }
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("alimentacion").child(idChange).setValue(result);
                                myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("alimentacion").child(idChange).setValue(result);
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

    costo.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals("")) {
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                format.setMaximumFractionDigits(0);
                format.setParseIntegerOnly(true);
                String numberString;
                try {
                    numberString = format.parse(s.toString()).toString();
                } catch (ParseException e) {
                    numberString = s.toString();
                    if(numberString.length()>1) {
                        costo.setText("");
                        return;
                    }
                }
                lugar.setText(numberString);
                try {
                    format.setMaximumFractionDigits(0);
                    if (!format.format(Integer.parseInt(numberString)).equals(s.toString())) {
                        int number = Integer.parseInt(numberString);
                        format.setMaximumFractionDigits(0);
                        String numberFormat = format.format(number);
                        costo.setText(numberFormat);
                        costo.setSelection(costo.getText().length());
                        fechaTextView.setText(numberString);

                    }
                } catch (NumberFormatException e) {
                    costo.setText("");
                }
            }
        }
    });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agregar_gasto, menu);
        if(getIntent().getExtras()!=null){
            MenuItem borrarItem = menu.findItem(R.id.action_delete_agregarGasto);
            borrarItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back_agregarGasto) {
            finish();
        }
        if (id == R.id.action_delete_agregarGasto){

            final AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(AddAlimentacionActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(AddAlimentacionActivity.this);
            }
            builder.setTitle("Eliminar gasto")
                    .setMessage("Se eliminará gasto")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                            final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            final List<String> tripsGastos = new ArrayList<String>();
                            final List<String> tripKey = new ArrayList<String>();
                            final int gasto = Integer.parseInt(costoChange);
                            final List<String> gastoAcumString = new ArrayList<String>();
                            myRef.child("users").child(userKey).child("currentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot oneTrip : dataSnapshot.getChildren()) {
                                        tripsGastos.add(oneTrip.child("gastos").getValue(String.class));
                                        tripKey.add(oneTrip.getKey());
                                        int gastoAcum = Integer.parseInt(tripsGastos.get(0));
                                        gastoAcum = gastoAcum - gasto;
                                        gastoAcumString.add(Integer.toString(gastoAcum));
                                    }
                                    String tKey = tripKey.get(0);


                                    myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("listaGastos").child("alimentacion").child(idChange).removeValue();
                                    myRef.child("users").child(userKey).child("currentTrip").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                    myRef.child("users").child(userKey).child("trips").child(tKey).child("gastos").setValue(gastoAcumString.get(0));
                                    myRef.child("users").child(userKey).child("trips").child(tKey).child("listaGastos").child("alimentacion").child(idChange).removeValue();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            Intent intent = new Intent(AddAlimentacionActivity.this, ContinueTripActivity.class);
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
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        addImage = (ImageView) findViewById(R.id.photo_confirmAlimentacion);
        fechaImagen = (ImageView) findViewById(R.id.today_alimentacion);
        lugar = (EditText) findViewById(R.id.editTextLugarAlimentacion);
        costo = (EditText) findViewById(R.id.editTextCostoAlimentacion);
        fechaTextView = (TextView) findViewById(R.id.textViewDateAA);
        photo = (ImageView) findViewById(R.id.photo_alimentacion);
        photoTomada = (ImageView) findViewById(R.id.photo_alimentacionTomada);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbarAddAlimentacion);

        setSupportActionBar(myToolbar);
        getSupportActionBar().setSubtitle("Agregar gasto");
        getSupportActionBar().setTitle("Alimentación");

    }

    public boolean validate(){
        boolean pasa = true;
        String lugarAc= lugar.getText().toString();
        String gasto = costo.getText().toString();
        String fecha = fechaTextView.getText().toString();

        if(lugarAc.isEmpty() || gasto.isEmpty() || fecha.isEmpty()){
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
        tieneFotoChange = getIntent().getExtras().getBoolean("tieneFoto");
        lugar.setText(lugarChange);
        fechaTextView.setText(fechaChange);
        costo.setText(costoChange);
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
                    photo.setVisibility(View.VISIBLE);
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
                    photoTomada.setVisibility(View.VISIBLE);
                    photoTomada.setImageBitmap(myBitmap);
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
