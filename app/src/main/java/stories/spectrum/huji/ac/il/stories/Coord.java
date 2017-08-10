package stories.spectrum.huji.ac.il.stories;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;

/**
 * A class that represents a Coord
 */
public class Coord implements Parcelable {

    // Data members
    public int coordID;
    public int coordRouteID;
    public int coordStoryID;
    public double coordLatitude;
    public double coordLongitude;
    public int coordOrder;
    public String coordUserName;
    public String coordCreationDate;
    public ArrayList<Recording> coordRecordings = new ArrayList<>();

    /*** The marker IS NOT moved as any other Parcelable. Will always be set to null!  */
    public Marker marker;
    /*** The isVisited value IS NOT moved as any other Parcelable. Will always be set to false!  */
    public boolean isVisited;

    /*** The lastPlayedRecordingArrIndex value IS NOT moved as any other Parcelable. Will always be set to -1!
     *   This indicates what is the last Recording file that was played. Linked to the next uploaded recording file.
     */
    public int lastPlayedRecordingID;


    // Consts
    public static final String NAME_OF_PUT_EXTRA_PARAM = "coord";

    public Coord(int coordID, int coordRouteID, int coordStoryID, double coordLatitude, double coordLongitude, int coordOrder, String coordUserName, String coordCreationDate) {
        this.coordID = coordID;
        this.coordRouteID = coordRouteID;
        this.coordStoryID = coordStoryID;
        this.coordLatitude = coordLatitude;
        this.coordLongitude = coordLongitude;
        this.coordOrder = coordOrder;
        this.coordUserName = coordUserName;
        this.coordCreationDate = coordCreationDate;
        this.marker = null;
        this.isVisited = false;
        this.lastPlayedRecordingID = StoryServerURLs.NO_PREVIOUS_RECORDING;
    }

    public Coord(JSONObject obj) {
        try {

            this.coordID = obj.getInt(StoryServerURLs.COORD_COORD_ID);
            this.coordRouteID = obj.getInt(StoryServerURLs.COORD_ROUTE_ID);
            this.coordStoryID = obj.getInt(StoryServerURLs.COORD_STORY_ID);
            this.coordLatitude = obj.getDouble(StoryServerURLs.COORD_COORD_LATITUDE);
            this.coordLongitude = obj.getDouble(StoryServerURLs.COORD_COORD_LONGITUDE);
            this.coordOrder = obj.getInt(StoryServerURLs.COORD_ORDER);
            this.coordUserName = obj.getString(StoryServerURLs.COORD_USER_NAME);
            this.coordCreationDate = obj.getString(StoryServerURLs.COORD_CREATION_DATE);
            this.marker = null;
            this.isVisited = false;
            this.lastPlayedRecordingID = StoryServerURLs.NO_PREVIOUS_RECORDING;

        } catch (JSONException e) {
            Log.e("BAD_JSON", "JSON EX");
        }
    }

    /* ==========================================================
       == Everything below here is for implementing Parcelable ==
       ========================================================== */

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(coordID);
        out.writeInt(coordRouteID);
        out.writeDouble(coordLatitude);
        out.writeDouble(coordLongitude);
        out.writeInt(coordOrder);
        out.writeString(coordUserName);
        out.writeTypedList(coordRecordings);
        out.writeString(coordCreationDate);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Coord> CREATOR = new Parcelable.Creator<Coord>() {
        public Coord createFromParcel(Parcel in) {
            return new Coord(in);
        }

        public Coord[] newArray(int size) {
            return new Coord[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Coord(Parcel in) {
        this.coordID = in.readInt();
        this.coordRouteID = in.readInt();
        this.coordLatitude = in.readDouble();
        this.coordLongitude = in.readDouble();
        this.coordOrder = in.readInt();
        this.coordUserName = in.readString();
        in.readTypedList(this.coordRecordings, Recording.CREATOR);
        this.coordCreationDate = in.readString();
    }
}
