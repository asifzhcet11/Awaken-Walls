package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;

/**
 * Created by mohammadasif on 28/01/2017.
 *
 * This class is used to decode the images recieved from the server
 *
 *
 */

public class DetectedImage {

    private ArrayList<String> imagesString = new ArrayList<String>();
    private ArrayList<Bitmap> imagesArray = new ArrayList<Bitmap>();


    public DetectedImage(ArrayList<String> imagesString) {
        this.imagesString = imagesString;
          }

    public ArrayList<Bitmap> returnImages()
    {
        for (int i=0; i<imagesString.size(); i++) {

                //decoding the image after recieving from the server
                byte[] decodedImageString = Base64.decode(this.imagesString.get(i), Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedImageString, 0, decodedImageString.length);
                imagesArray.add(i,decodedImage);


        }
        return imagesArray;
    }

}
