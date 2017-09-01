package stories.spectrum.huji.ac.il.stories.net;

/**
 * Manages server calls.
 * All get/post calls should go through this class.
 * All methods and members are static.
 */
public class StoryServerURLs {

    /* Timeout CONST */
    public static final int TIME_OUT_SECONDS = 10;

    public static final int GPS_CHECK_INTERVAL_MILLISECONDS = 1000;

    public static final int MAX_NUM_OF_COORDS = 10;

    public static final int MAX_DISTANCE_METERS_FROM_ACTIVE_COORD = 50;

    public static final int FIRST_COORD_ORDER = 1;

    public static final int NO_PREVIOUS_RECORDING = -1;

    public static final int NOT_EXIST = -1;

    public static final int EMPTY_COORD_LIST = -1;

    public static final int TOO_FAR_FROM_COORDS = -2;

    /* Seerver URL */
    public static String serverURLServer = "http://ec2-34-209-57-147.us-west-2.compute.amazonaws.com/"; // server
    public static String serverURLLocal = "http://10.0.0.3:80/"; // home
    //private static String serverURLUni = "http://132.65.250.11:80/"; // university

    public static String serverURL = serverURLServer;

    /* JSON consts */

    // Story
    public static final String STORY_STORY_ID = "story_id";
    public static final String STORY_ADDRESS = "story_address";
    public static final String STORY_NAME = "story_name";
    public static final String STORY_USER_ID = "story_user_id";
    public static final String STORY_FILE_PATH = "story_file_path";
    public static final String STORY_USER_NAME = "story_user_name";
    public static final String STORY_CREATION_DATE = "story_creation_date";
    public static final String STORY_COORD_LATITUDE = "story_coord_latitude";
    public static final String STORY_COORD_LONGITUDE = "story_coord_longitude";
    public static final String STORY_TAGS = "story_tags";
    public static final String STORY_TAGS_DELIMITER = ";";

    // Route
    public static final String ROUTE_ROUTE_ID = "route_id";

    // Coords
    public static final String COORD_COORD_ID = "coord_id";
    public static final String COORD_ROUTE_ID = "coord_route_id";
    public static final String COORD_STORY_ID = "coord_story_id";
    public static final String COORD_COORD_LATITUDE = "coord_latitude";
    public static final String COORD_COORD_LONGITUDE = "coord_longitude";
    public static final String COORD_ORDER = "coord_order";
    public static final String COORD_USER_NAME = "coord_user_name";
    public static final String COORD_CREATION_DATE = "coord_creation_date";

    // Recording
    public static final String RECORDING_RECORDING_ID = "recording_id";
    public static final String RECORDING_COORD_ID = "recording_coord_id";
    public static final String RECORDING_CREATION_DATE = "recording_creation_date";
    public static final String RECORDING_FILE_PATH = "recording_file_path";
    public static final String RECORDING_USER_NAME = "recording_user_name";
    public static final String RECORDING_PREVIOUS_RECORDING_ID = "recording_previous_recording_id";
    public static final String RECORDING_FILE_DURATION = "recording_file_duration";
    public static final String RECORDING_RATING = "recording_rating";

    // Users
    public static final String USER_USER_ID = "user_id";
    public static final String USER_TAKEN = "\"taken\"";

    /*
     * Constructor.
     * One cannot instantiate this class.
     */
    private StoryServerURLs() {
        // Do not instantiate me!
    }

    public static String getAllStories() {
        return serverURL + "get_data?type=story";
    }

    public static String getStoryByID(String storyID) {
        return serverURL + "get_data?type=story&story_id=" + storyID;
    }

    public static String getImageURL(String filePath) {
        return serverURL + "get_file?file_name=" + filePath + "&file_type=image";
    }

    public static String getRecordByPathURL(String filePath) {
        return serverURL + "get_file?file_name=" + filePath + "&file_type=sound";
    }

    public static String getAllRecordingByCoordIDURL(int id) {
        return serverURL + "get_data?type=recording&coord_id=" + id;
    }

    public static String getAllCoordsByStoryIDURL(int id) {
        return serverURL + "get_data?type=coord&story_id=" + id;
    }

    public static String getUploadSoundFileURL(int userID, int coordID, int previousRecordingID, int soundFileLength) {
        return serverURL + "upload_file?type=recording&user_id=" + userID +"&coord_id=" + coordID +"&previous_recording_id=" + previousRecordingID + "&recording_file_duration=" + soundFileLength;
    }

    public static String getAddNewCoordURL(int userID, int routeID, double latitude, double longitude) {
        return serverURL + "set_data?type=coord&coord_user_id=" + userID + "&coord_route_id=" + routeID + "&coord_latitude=" + latitude + "&coord_longitude=" + longitude;
    }

    public static String getStaticMapURL(String size, double latitude, double longitude) {
        // use: http://staticmapmaker.com/google/
        return "https://maps.googleapis.com/maps/api/staticmap?autoscale=false&size=" + size +"&maptype=roadmap&format=jpg&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + latitude +",+" + longitude + "&language=iw";
    }

    public static String getUserLogindURL(String user, String pass) {
        return serverURL + "get_data?type=login&user_name=" + user + "&user_password=" + pass;
    }

    public static String getRegisterdURL(String user, String pass, String email) {
        return serverURL + "set_data?type=user&user_name=" + escape(user) +"&user_password=" + escape(pass)+ "&user_mail=" + escape(email);

    }

    public static String getAddRatingURL(int userID, int recordingID, float rating) {
        return serverURL + "set_data?type=rating&rating_recording_id=" + recordingID + "&rating_user_id=" + userID +"&rating_value=" + rating;

    }

    public static String getAddressOfLocation(double latitude, double longitude) {
        return "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&language=iw";

    }

    public static String getUploadStoryURL(int userID, String storyAddress, String storyName, String storyTags, int recordingFileDuration, double storyLatitude, double storyLongitude) {
        return serverURL + "upload_file?type=story&user_id=" + userID + "&story_address=" + escape(storyAddress) + "&story_name=" + escape(storyName) + "&story_tags=" + escape(storyTags) + "&recording_file_duration=" + recordingFileDuration + "&story_latitude=" + storyLatitude + "&story_longitude=" + storyLongitude;

    }

    private static String escape(String str) {
        return str.replaceAll("'", "''");
    }
}





