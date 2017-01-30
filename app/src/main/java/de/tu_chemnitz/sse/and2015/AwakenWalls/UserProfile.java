package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Base64;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mohammadasif on 16/01/2017.
 */

public class UserProfile {

    private String username;
    private String profilePicture;
    private UserAddress userAddress;
    private Context context;
    private UserConfiguration userConfiguration;




    public UserProfile(Context context,String username, UserAddress userAddress, String profilePicture, UserConfiguration userConfiguration) {

        this.context = context;
        this.username = username;
        this.userAddress = userAddress;
        this.profilePicture = profilePicture;
        this.userConfiguration = userConfiguration;


    }


    public void setProfile()
    {
        this.setUsername();
        this.setAddress();
        this.setProfilePicture();

    }


    public void setUserConfiguration(){

        TextView UserNumberOfRooms      = (TextView)((Activity)context).findViewById(R.id.number_of_rooms);
        TextView UserNumberOfAppliances = (TextView)((Activity)context).findViewById(R.id.number_of_appliances);

        UserNumberOfRooms.setText(userConfiguration.getNumberOfRooms());
        UserNumberOfAppliances.setText(userConfiguration.getNumberOfAppliances());



    }

    public void setUsername()
    {

        TextView UserName = (TextView)((Activity)context).findViewById(R.id.username);
        UserName.setText(username);

    }

    public void setAddress()
    {
        TextView UserAddress    =  (TextView)((Activity)context).findViewById(R.id.user_address);
        TextView UserCity       =  (TextView)((Activity)context).findViewById(R.id.user_city);
        TextView UserPostCode   =  (TextView)((Activity)context).findViewById(R.id.user_postCode);
        TextView UserCountry    =  (TextView)((Activity)context).findViewById(R.id.user_country);

        UserAddress.setText(userAddress.getAddress());
        UserCity.setText(userAddress.getCity());
        UserPostCode.setText(userAddress.getPostCode());
        UserCountry.setText(userAddress.getCountry());
    }

    public void setProfilePicture()
    {
        byte[] decodedString = Base64.decode(profilePicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0,decodedString.length);
        CircleImageView profilePictureView = (CircleImageView)((Activity)context).findViewById(R.id.profile_picture);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        profilePictureView.setImageBitmap(decodedByte);
        profilePictureView.setColorFilter(filter);
    }



}
