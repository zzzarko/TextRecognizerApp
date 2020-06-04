package com.example.textrecognizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler extends SQLiteOpenHelper{
    /**
        Baza podataka mora imati verziju i ime
     */
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "imagedb";

    /**
        Ime tabele i polja:
     */
        private static final String TABLE_IMAGES = "images";
        private static final String KEY_ID = "id";
        private static final String KEY_NAME = "name";
        private static final String KEY_IMAGE = "image";

    /**
     Konstuktor sadrži instancu abstraktne klase Context, služi za sadržaj lokacije  baze podataka
     */

        public DataBaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

    /**
        Kreiranje baze: SQL upit

     Neophodno Overridovati metod onCreate...
     */
        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_IMAGES + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                    + KEY_IMAGE + " BLOB" + ")";
            db.execSQL(CREATE_IMAGES_TABLE);
        }

    /**
        Overridujemo metod onUpgrade:
        Ako postoji nesto u tabeli, ima smisla brisati
     */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
            onCreate(db);
        }

        /**
         * CRUD operacije:
         */

    /**
     * Dodavanje slike u bazu
     */
        public void addImage(SimpleImage simpleImage) {
            SQLiteDatabase db = this.getWritableDatabase();

            /**
             * Ubacujemo vrednosti jednog reda tabele
             */
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, simpleImage.getName());
            values.put(KEY_IMAGE, simpleImage.getImage());

            /**
             * Dodamo taj red i gasimo konekciju
             */
            db.insert(TABLE_IMAGES, null, values);
            db.close();
        }



    /**
     * Dobavljivanje slika iz baze:
     */
        public List<SimpleImage> getAllImages() {
            List<SimpleImage> simpleImageList = new ArrayList<SimpleImage>();
            /**
             * Upit kojim dobavljamo sve slike
             */
            String selectQuery = "SELECT  * FROM images ORDER BY name";

            SQLiteDatabase db = this.getWritableDatabase();
            /**
             Cursor -> Prolazak kroz redove
             */
            Cursor cursor = db.rawQuery(selectQuery, null);
            /**
             Prolazimo kroz sve redove ...
             */

            /**
             * Provera da li taj red ima neku vrednost
             */
            if (cursor.moveToFirst()) {
                do {
                    /**
                     * Instanca klase SimpleImage i setujemo ID, Ime i Sliku iz pojedinačnog reda
                     * takvu instancu dodajemo i listu simpleImagesList u koju skladištimo sve objekte tipa
                     * SimpleImage
                     */
                    SimpleImage simpleImage = new SimpleImage();
                    simpleImage.setID(Integer.parseInt(cursor.getString(0)));
                    simpleImage.setName(cursor.getString(1));
                    simpleImage.setImage(cursor.getBlob(2));

                    simpleImageList.add(simpleImage);
                } while (cursor.moveToNext());
            }

            db.close();

            return simpleImageList;

        }



    /**
     * Brisanje slike_
     */
        public void deleteImage(SimpleImage simpleImage) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_IMAGES, KEY_ID + " = ?",
                    new String[] { String.valueOf(simpleImage.getID()) });
            db.close();
        }



}