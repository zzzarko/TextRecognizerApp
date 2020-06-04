package com.example.textrecognizer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;


/**
    Koristimo ArrayAdapter kao posrednika između ListView-a u glavnoj Aktivnosti i slika (SimpleImage)
 */

public class ImageAdapter extends ArrayAdapter<SimpleImage> {
   private Context context;
   private int layoutResourceId;

    ArrayList<SimpleImage> data=new ArrayList<SimpleImage>();
    public ImageAdapter(Context context, int layoutResourceId, ArrayList<SimpleImage> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    /**
        Vraćamo jedan element liste, tj. jedan red - row
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /**
            Jedan element u listi tj. red.
         */
        View row = convertView;

        /**
        Klasa ImageHolder sadrži TextView i ImageView.
         Nalazi se dole.
         */
        ImageHolder holder = null;

        /**
            Kreiramo red ...
         */

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ImageHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            row.setTag(holder);
        }
        else
        {
            holder = (ImageHolder)row.getTag();
        }


        /**
            Dobavljamo sliku
         */
        SimpleImage picture = data.get(position);

        /**
            Setujemo ime slike
         */
        holder.txtTitle.setText(picture.getName());

        /**
            byte[] -> ByteArrayInputStream -> Bitmap
            Kako bismo na ImageView postavili Bitmap sliku, (ImageView) ne može prihvatiti
            niz bajtova ili sl.
         */
        byte[] outImage=picture.getImage();
        ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
        Bitmap theImage = BitmapFactory.decodeStream(imageStream);
        holder.imgIcon.setImageBitmap(theImage);
        return row;

    }

    static class ImageHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}