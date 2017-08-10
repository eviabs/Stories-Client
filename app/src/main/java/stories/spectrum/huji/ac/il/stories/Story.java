package stories.spectrum.huji.ac.il.stories;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;

/**
 * A class that represents a Stroy
 */
public class Story implements Parcelable {
    
    public int storyID;
    public String storyName;
    public String storyAddress;
    public int storyUserID;
    public String storyFilePath;
    public String storyUserName;
    public String storyCreationDate;
    public double storyCoordLatitude;
    public double storyCoordLongitude;
    public String[] storyTags = {""};

    public static final String NAME_OF_PUT_EXTRA_PARAM = "story";

    public Story(int storyID, String storyName, int storyUserID, String storyFilePath, String storyUserName, String storyCreationDate, double storyCoordLatitude, double storyCoordLongitude, String[] storyTags, String storyAddress) {
        this.storyID = storyID;
        this.storyAddress = storyAddress;
        this.storyName = storyName;
        this.storyUserID = storyUserID;
        this.storyFilePath = storyFilePath;
        this.storyUserName = storyUserName;
        this.storyCreationDate = storyCreationDate;
        this.storyCoordLatitude = storyCoordLatitude;
        this.storyCoordLongitude = storyCoordLongitude;
        this.storyTags = storyTags;
    }

    public Story(JSONObject obj) {
        try {
            this.storyID = obj.getInt(StoryServerURLs.STORY_STORY_ID);
            this.storyAddress = obj.optString(StoryServerURLs.STORY_ADDRESS, "");
            this.storyName = obj.optString(StoryServerURLs.STORY_NAME, "");
            this.storyUserID = obj.getInt(StoryServerURLs.STORY_USER_ID);
            this.storyFilePath = obj.getString(StoryServerURLs.STORY_FILE_PATH);
            this.storyUserName = obj.getString(StoryServerURLs.STORY_USER_NAME);
            this.storyCreationDate = obj.getString(StoryServerURLs.STORY_CREATION_DATE);
            this.storyCoordLatitude = obj.getDouble(StoryServerURLs.STORY_COORD_LATITUDE);
            this.storyCoordLongitude = obj.getDouble(StoryServerURLs.STORY_COORD_LONGITUDE);
            this.storyTags = obj.getString(StoryServerURLs.STORY_TAGS).split(StoryServerURLs.STORY_TAGS_DELIMITER);

        } catch (JSONException e) {
            Log.e("BAD_JSON", "JSON EX");
        }
    }

    public String getReadableStoryAddress() {
        try {
            String[] address = storyAddress.split(";");
            return address[1] + " " + address[0] + ", " + address[2] + ", " + address[3];
        } catch (Exception a) {
            return "ישראל";
        }
    }
    public int getDistanceFrom(double lat, double lon) {
        return (int)(lat-lon);
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
        out.writeInt(this.storyID);
        out.writeString(this.storyAddress);
        out.writeString(this.storyName);
        out.writeInt(this.storyUserID);
        out.writeString(this.storyFilePath);
        out.writeString(this.storyUserName);
        out.writeString(this.storyCreationDate);
        out.writeDouble(this.storyCoordLatitude);
        out.writeDouble(this.storyCoordLongitude);
        out.writeStringArray(this.storyTags);

    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>() {
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Story(Parcel in) {
        this.storyID = in.readInt();
        this.storyAddress = in.readString();
        this.storyName = in.readString();
        this.storyUserID = in.readInt();
        this.storyFilePath = in.readString();
        this.storyUserName = in.readString();
        this.storyCreationDate = in.readString();
        this.storyCoordLatitude = in.readDouble();
        this.storyCoordLongitude = in.readDouble();
        this.storyTags = in.createStringArray();
    }
}
