package stories.spectrum.huji.ac.il.stories;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;

/**
 * A class that represents a Recording
 */
public class Recording implements Parcelable {

    // Data members
    public int recordingID;
    public int recordingCoordID;
    public String recordCreationDate;
    public String recordingFilePath;
    public String recordingUserName;
    public int recordingPreviousRecordingID;
    public int recordingFileDuration;
    public double recordingRating;

    // Consts
    public static final String NAME_OF_PUT_EXTRA_PARAM = "recording";

    public Recording(int recordingID, int recordingCoordID, String recordCreationDate,String recordingFilePath, String recordingUserName, int recordingPreviousRecordingID, int recordingFileDuration, double recordingRating) {
        this.recordingID = recordingID;
        this.recordingCoordID = recordingCoordID;
        this.recordCreationDate = recordCreationDate;
        this.recordingFilePath = recordingFilePath;
        this.recordingUserName = recordingUserName;
        this.recordingPreviousRecordingID = recordingPreviousRecordingID;
        this.recordingFileDuration = recordingFileDuration;
        this.recordingRating = recordingRating;
    }

    public Recording(JSONObject obj) {
        try {
            this.recordingID = obj.getInt(StoryServerURLs.RECORDING_RECORDING_ID);
            this.recordingCoordID = obj.getInt(StoryServerURLs.RECORDING_COORD_ID);
            this.recordCreationDate = obj.getString(StoryServerURLs.RECORDING_CREATION_DATE);
            this.recordingFilePath = obj.getString(StoryServerURLs.RECORDING_FILE_PATH);
            this.recordingUserName = obj.getString(StoryServerURLs.RECORDING_USER_NAME);
            this.recordingPreviousRecordingID = obj.getInt(StoryServerURLs.RECORDING_PREVIOUS_RECORDING_ID);
            this.recordingFileDuration = obj.getInt(StoryServerURLs.RECORDING_FILE_DURATION);
            this.recordingRating = obj.getDouble(StoryServerURLs.RECORDING_RATING);

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
        out.writeInt(recordingID);
        out.writeInt(recordingCoordID);
        out.writeString(recordCreationDate);
        out.writeString(recordingFilePath);
        out.writeString(recordingUserName);
        out.writeInt(recordingPreviousRecordingID);
        out.writeInt(recordingFileDuration);
        out.writeDouble(recordingRating);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Recording> CREATOR = new Parcelable.Creator<Recording>() {
        public Recording createFromParcel(Parcel in) {
            return new Recording(in);
        }

        public Recording[] newArray(int size) {
            return new Recording[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Recording(Parcel in) {
        this.recordingID = in.readInt();
        this.recordingCoordID = in.readInt();
        this.recordCreationDate = in.readString();
        this.recordingFilePath = in.readString();
        this.recordingUserName = in.readString();
        this.recordingPreviousRecordingID = in.readInt();
        this.recordingFileDuration = in.readInt();
        this.recordingRating = in.readDouble();
    }
}
