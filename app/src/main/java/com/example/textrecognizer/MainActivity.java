package com.example.textrecognizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;


import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import android.widget.Button;


import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


import java.util.ArrayList;


import java.util.List;


public class MainActivity extends AppCompatActivity {

    /**
     Sve neophodne komponente
     */
    private boolean isTwoPane = false;
    private  Button addList;
    private ImageButton addImage;
    private ArrayList<SimpleImage> imageArry = new ArrayList<SimpleImage>();
    private ImageAdapter imageAdapter;
    private ListView dataList;
    private byte[] imageName;
    private int imageId;
    private Bitmap theImage, imageBitmap;
    private ImageView imageView;
    private TextView textView;
    List<SimpleImage> simpleImages;

    /**
     REQUEST_IMAGE_CAPTURE -> Neophodan kao identifikacija prilikom otvaranje kamere
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;
    /**
     Brojac, kako bi se čuvao redni broj slike...
     */
    private   int counter = 0;
    /**
     Instanca Baze podataka
     */
    private DataBaseHandler db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = findViewById(R.id.textView);

        /**
         ScrollingMovementMethod - Kako bismo mogli da skrolujemo u textView-u
         */
        textView.setMovementMethod(new ScrollingMovementMethod());
        addList = findViewById(R.id.btnAddList);

        imageView = findViewById(R.id.imageView);
        dataList =  findViewById(R.id.list);

        db = new DataBaseHandler(this);
        addImage =  findViewById(R.id.btnAdd);

        /**
         Poziv metoda kojim se dobija trenutna vrednost brojaca
         */
        loadCounter();

        /**
         Dugme za dodavanje trenutne slike u bazu podataka i u ListView
         */
        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 ByteArrayOutputStream -> BitMap -> []byte
                 */
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte imageInByte[] = stream.toByteArray();

                /**
                 Povecamo brojač za 1
                 */
                counter++;

                /**
                 Dodamo sliku u bazu podataka
                 */
                db.addImage(new SimpleImage("Slika "+counter , imageInByte));

                /**
                 Dugme za dodavanje onemogućimo, kako korisnik ne bi dodao sliku pre nego što slika
                 */
                addList.setEnabled(false);

                /**
                 Ostajemo u tekućoj aktivnosti
                 */
                Intent i = new Intent(getApplicationContext(),
                        MainActivity.class);

                        startActivity(i);


            }
        });


    /**
     Dobavimo sve slike iz baze
     */
         simpleImages = db.getAllImages();
        for (SimpleImage im : simpleImages) {
            /**
             Dodamo svaku sliku u ArrayList
             */
            imageArry.add(im);
        }

        /**
         * Kreiramo instancu ImageAdapter-a i stavimo kontekst tekuće klase, ID layout-a koji predstavlja pojedinačan element,
         * i listu slika
         *
         * Postavimo Item iz Baze u ListView, tako što postavimo adapter
         */
        imageAdapter = new ImageAdapter(this, R.layout.screen_list,
                imageArry);
        dataList.setAdapter(imageAdapter);
        /**
         * Kada kliknemo, idemo na DisplayActivity
         */
        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    final int position, long id) {

                /**
                 Dobavimo niz bajtova slike i ID -> Kako bismo povezali slike tj. znali na koju smo sliku kliknuli
                 */
                imageName = imageArry.get(position).getImage();
                imageId = imageArry.get(position).getID();

                /**
                 Konverzija
                 */
                ByteArrayInputStream imageStream = new ByteArrayInputStream(
                        imageName);
                theImage = BitmapFactory.decodeStream(imageStream);

                /**
                 Intent za DisplayActivity
                 */
                Intent intent = new Intent(MainActivity.this,
                        DisplayActivity.class);
                /**
                 Šaljemo sliku i ID
                 */
                intent.putExtra("imagename", theImage);
                intent.putExtra("imageid", imageId);


                startActivity(intent);


            }
        });
        /**
         Klikom na dugme pozivamo metod...
         */
        addImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    dispatchTakePictureIntent();
            }
        });

    }


    private void dispatchTakePictureIntent() {
        /**
         *Intent kom govorimo koju aktivnost treba da izvrši (CAPTURE)
         *Ako nije null, pozivamo metod
         */
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     *RequestCode je 1, ResultCode treba biti OK, i dobavljamo odgovarajući Intent.
     *Dobaljamo BitmapSliku i postavljamo je na ImageView
     * Pozivamo metod koji će prepoznati tekst sa slike
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            recognizeTextFromImage();
        }
    }




    private void recognizeTextFromImage() {

        /**
         * Prepoznavanje teksta sa slike pomoću impelemntacije FireBase-a.
         * Dependency se nalazi u build.gradle
         * Uzimamo sliku koju smo uslikali
         * Poziv već implementiranih meotda za detekciju teksta.
         * Ako je uspešno prepoznat tekost -> poziv metoda displayTextFromImg
         * Ako nije uspešno (problem sa slikom) -> Prikaz odgovarajuće poruke
         */
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImg(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Greška pirlikom prepoznavanja! " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.d("Greška ", e.getMessage());
            }
        });

        addList.setEnabled(true);
    }

    private void displayTextFromImg(FirebaseVisionText firebaseVisionText) {

        /**
         * Ako je lista blokova prazna, nemoguće je prepoznati tekst -> Odgovarajuća poruka
         * Ako nije, znači da postoji tekst -> Postavljamo tekst na textView
         */
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if(blockList.size() == 0)
            Toast.makeText(this, "Nemoguće prepoznati tekst!",
                    Toast.LENGTH_LONG).show();
        else{

            for(FirebaseVisionText.Block block : firebaseVisionText.getBlocks()){
                String recognizedText = block.getText();
                textView.setText(recognizedText);
            }
        }
    }

    /**
     * Metod koji služi za čuvanje trenutne vrednosti brojača, kako se ne bi uvek setovao na 0
     * SharedPreferences je kolekcija po principu Ključ - Vrednost
     * Postavljamo taj brojač
     */
    public void saveCounter(){
        SharedPreferences sharedPreferences = getSharedPreferences("saveCounter", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("counterValue", counter);
        editor.apply();

    }
    /**
     * Dobavljamo trenutnu vrednost brojača po ključu
     */
    public void  loadCounter(){
        SharedPreferences sharedPreferences = getSharedPreferences("saveCounter", MODE_PRIVATE);
        counter = sharedPreferences.getInt("counterValue", counter);
    }

    /**
     Prilikom zaustavljanja aplikacije, poziva se metod za čuvanje
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveCounter();
    }



}