# Stories-Client

This is the client side of the project.

The app interacts with a server, and retrieves/sends data to it.

For the server see [Stories-Server](https://github.com/eviabs/Stories-Server).

## Getting Started 

### Installing And Deployment

* Copy the client's content into a new directory.

* Open the file [StoryServerURLs.java](https://github.com/eviabs/Stories-Client/blob/master/app/src/main/java/stories/spectrum/huji/ac/il/stories/net/StoryServerURLs.java) and chnage lines [30](https://github.com/eviabs/Stories-Client/blob/master/app/src/main/java/stories/spectrum/huji/ac/il/stories/net/StoryServerURLs.java#L30) and [31](https://github.com/eviabs/Stories-Client/blob/master/app/src/main/java/stories/spectrum/huji/ac/il/stories/net/StoryServerURLs.java#L31)

  ```
  public static String serverURLServer = "http://server.url/"; // server
  public static String serverURLLocal = "http://localhost/"; // home
  ```

  where *http://server.url/* is the address of the main server that has been deployed, and *http://localhost/* is the address of the local server that has been deployed (used for testing).

* Open the file [google_maps_api.xml](https://github.com/eviabs/Stories-Client/blob/master/app/src/release/res/values/google_maps_api.xml) and insert your google maps api key.

* Build the project.

Now the app can be used and interact with the newly deployed server. 


## Authors

**Evyatar Ben-Shitrit** - [eviabs](https://github.com/eviabs)

## License

This project is licensed under the MIT License.
