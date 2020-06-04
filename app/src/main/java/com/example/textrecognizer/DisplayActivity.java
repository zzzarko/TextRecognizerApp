package com.example.textrecognizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;
import java.util.Locale;

public class DisplayActivity extends AppCompatActivity {



    private Button  btnSpeech, btnFind, btnCopy;
    private ImageView imageDetail;
    private int imageId;
    private Bitmap theImage;
    private TextView tvDetails;
    private TextToSpeech textToSpeech;
    private EditText editText;
    private ImageButton btnDelete;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);



        editText = findViewById(R.id.edit_text);
        btnSpeech = findViewById(R.id.btnSpeech);
        btnCopy = findViewById(R.id.btnCopy);
        btnFind = findViewById(R.id.btnFind);
        btnDelete =  findViewById(R.id.btnDelete);
        imageDetail = findViewById(R.id.imageViewDetail);
        tvDetails = findViewById(R.id.textViewDetail);


    /**
    instanca klase TextToSpeech - proveravamo da li napisan tekst može da se reprodukuje
    */
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int res = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.e("TTS", "Jezik nije podržan");
                }
                else
                    Log.e("TTS", "Nemoguća inicijalizacija");

            }
        });


    /**
     Klikom na dugmice pozivamo metode...
     */
        btnSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText();
            }
        });
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findText();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showDialog();
            }
        });


        tvDetails.setMovementMethod(new ScrollingMovementMethod());
        editText.setMovementMethod(new ScrollingMovementMethod());

        /**
         Dobavljamo Intent i setujemo primeljenu sliku i ID
         */
        final Intent intnt = getIntent();
        theImage = (Bitmap) intnt.getParcelableExtra("imagename");
        imageId = intnt.getIntExtra("imageid", 20);
        imageDetail.setImageBitmap(theImage);

        /**
            Poziv metoda za prepoznavanje teksta, kao u MainActivity
         */
        recognizeTextFromImage();
    }


    private void recognizeTextFromImage() {
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(theImage);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImg(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayActivity.this, "Greška pirlikom prepoznavanja! " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.d("Greška ", e.getMessage());
            }
        });
    }

    private void displayTextFromImg(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if(blockList.size() == 0)
            Toast.makeText(this, "Nemoguće prepoznati tekst!",
                    Toast.LENGTH_LONG).show();
        else{

            for(FirebaseVisionText.Block block : firebaseVisionText.getBlocks()){
                String recognizedText = block.getText();
                tvDetails.setText(recognizedText);
            }
        }
    }

    /**
     Ako može da se reprodukuje - tekst iz textViexa se reprodukuje
     */
    private void speak(){
        String texSpeech = tvDetails.getText().toString();
        textToSpeech.speak(texSpeech, TextToSpeech.QUEUE_FLUSH, null);
    }
    @Override
    protected void onDestroy() {
        if(textToSpeech!=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
    /**
     * Metod za nalaženje teksta
     * Ako je unet tekst dužine nula -> odgovarajuća poruka
     * Ako je unet neki tekst, kreiramo novi tekst sa istim sadržajem ali obojen u žuto, i gde god se pojavljuje u TextView isti tekst
     * zamenimo ga sa obojenim
     */
    private void findText(){
        if(editText.getText().toString().trim().length()==0)
            Toast.makeText(DisplayActivity.this,"Nije unet teskt za pretragu", Toast.LENGTH_SHORT).show();
        String textHighLight = editText.getText().toString();
        if(textHighLight.length()!=0){
            String replaceWith = "<span style='background-color:yellow'>" + textHighLight + "</span>";
            String originalText = tvDetails.getText().toString();
            String modifiedText =  originalText.replaceAll(textHighLight, replaceWith);
            tvDetails.setText(Html.fromHtml(modifiedText));
        }
    }

    private void copyText() {
        /**
         * Ako postoji tekst onda se kopira u privremenu memoriju
         * Inače odogovarajuća poruka
         */
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("EditText", tvDetails.getText().toString());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(DisplayActivity.this, "Kopiran tekst! " ,
                Toast.LENGTH_LONG).show();
    }
    /**
     * Kreiramo dijalog
     * Postavljamo naslov, poruku, i ikonu
     * Opcija za positiveButton -> Sadrži tekst da,  zatim se prikazuje odgovarajuća poruka i poziv metoda za brisanje
     * Opcija za negativeButton -> Sadrži tekst ne, i odgovarajuća poruka
     */
    private void showDialog(){
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setTitle("Potvrdite brisanje");
        alBuilder.setMessage("Da li zaista želite da obrišete sliku?");
        alBuilder.setIcon(R.drawable.trash);
        alBuilder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Slika je obrisana",Toast.LENGTH_SHORT).show();
                deleteImage();
            }
        });
        alBuilder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Slika nije obrisana",Toast.LENGTH_SHORT).show();
            }
        });
        alBuilder.show();

    }

    private void deleteImage(){
        DataBaseHandler db = new DataBaseHandler(
                DisplayActivity.this);
        /**
         * Brišemo sliku iz baze
         */
        Log.d("Delete Image: ", "Deleting.....");
        db.deleteImage(new SimpleImage(imageId));
        Intent i = new Intent(DisplayActivity.this,
                MainActivity.class);

        startActivity(i);
        finish();
    }

}
