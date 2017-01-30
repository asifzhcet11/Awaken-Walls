package de.tu_chemnitz.sse.and2015.AwakenWalls;

/**
 * Created by mohammadasif on 16/01/2017.
 */

public class UserConfiguration {

    private String numberOfRooms;
    private String numberOfAppliances;
    private String videoURL;


    public UserConfiguration(String numberOfRooms, String numberOfAppliances, String videoURL) {

        this.numberOfRooms = numberOfRooms;
        this.numberOfAppliances = numberOfAppliances;
        this.videoURL = videoURL;
    }


    public String getNumberOfRooms() {
        return numberOfRooms;
    }

    public String getNumberOfAppliances() {
        return numberOfAppliances;
    }


    public String getVideoURL() {
        return videoURL;
    }
}
