package com.example.textrecognizer;
public class SimpleImage
{
    /**
        Klasa kojom predstavljamo jednu sliku.
       - ID jer se smešta u bazu
       - Ime slike, kako bi korisnik aplikacije imao veću preglednost
       - Samu sliku koja je data nizom bajtova.
     */
    private int id;
    private String name;
    private byte[]  image;

    /**
        Konstruktori
     */
    public SimpleImage() { }



    public SimpleImage(String name, byte[] image) {
        this.name = name;
        this.image = image;

    }
    public SimpleImage(int id) {
        this.id = id;

    }
    /**
        Geteri i seteri
     */

    public int getID() {
        return this.id;
    }


    public void setID(int keyId) {
        this.id = keyId;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public byte[] getImage() {
        return this.image;
    }


    public void setImage(byte[] image) {
        this.image = image;
    }
}