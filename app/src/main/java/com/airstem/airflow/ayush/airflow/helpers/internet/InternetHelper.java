package com.airstem.airflow.ayush.airflow.helpers.internet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v13.app.ActivityCompat;
import android.telecom.Call;
import android.text.TextUtils;

import com.airstem.airflow.ayush.airflow.Manifest;
import com.airstem.airflow.ayush.airflow.enums.search.Type;
import com.airstem.airflow.ayush.airflow.events.volly.Callback;
import com.airstem.airflow.ayush.airflow.helpers.collection.CollectionConstant;
import com.airstem.airflow.ayush.airflow.helpers.collection.CollectionKey;
import com.airstem.airflow.ayush.airflow.model.search.SearchAlbum;
import com.airstem.airflow.ayush.airflow.model.search.SearchArtist;
import com.airstem.airflow.ayush.airflow.model.search.SearchImage;
import com.airstem.airflow.ayush.airflow.model.search.SearchPaging;
import com.airstem.airflow.ayush.airflow.model.search.SearchRadio;
import com.airstem.airflow.ayush.airflow.model.search.SearchTrack;
import com.airstem.airflow.ayush.airflow.model.search.SearchVideo;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mcd-50 on 15/8/17.
 */

public class InternetHelper {

    Context mContext;

    public InternetHelper(Context context) {
        mContext = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void searchRadio(String query, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", String.valueOf(query));
            final ArrayList<SearchRadio> searchRadios = new ArrayList<>();


            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = result.getString("type");

                                    if (type.equals(Type.AIRSTEM_RADIO.toString())) {

                                        JSONObject radios = result.getJSONObject("radios");
                                        JSONArray radios_result = radios.getJSONArray("result");

                                        for (int j = 0; j < radios_result.length(); j++) {
                                            JSONObject _item = radios_result.getJSONObject(j);
                                            String title = _item.getString("title");
                                            String country = _item.getString("country");
                                            String maxUser = _item.getString("max_user");

                                            JSONArray url = _item.getJSONArray("url");
                                            String[] urlArray = new String[url.length()];
                                            for (int k = 0; k < url.length(); k++) {
                                                urlArray[k] = url.getString(k);
                                            }

                                            JSONArray genre = _item.getJSONArray("genre");
                                            String[] genreArray = new String[genre.length()];
                                            for (int k = 0; k < genre.length(); k++) {
                                                genreArray[k] = genre.getString(k);
                                            }

                                            SearchRadio searchRadio = new SearchRadio(title, maxUser, urlArray, genreArray, country, null);
                                            searchRadios.add(searchRadio);
                                        }
                                    }
                                }
                                callback.onRadios(searchRadios);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);

        }
    }

    public void searchVideo(String query, final String nextPage, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", String.valueOf(query));
            if (nextPage != null) jsonBody.put("youtube_page", nextPage);

            final ArrayList<SearchVideo> searchVideos = new ArrayList<>();
            final String[] nextPageToken = {null};

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = result.getString("type");

                                    if (type.equals(Type.YOUTUBE_SEARCH.toString())) {

                                        JSONObject tracks = result.getJSONObject("tracks");
                                        JSONObject tracks_meta = tracks.getJSONObject("meta");
                                        JSONArray tracks_result = tracks.getJSONArray("result");


                                        nextPageToken[0] = tracks_meta.getString("next_page");

                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String description = _item.getString("description");
                                            String author = _item.getString("channel_title");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();

                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "YOUTUBE");
                                                searchImages.add(image);
                                            }

                                            JSONArray tags = _item.getJSONArray("tags");
                                            ArrayList<String> searchTags = new ArrayList<>();

                                            for (int k = 0; k < tags.length(); k++) {
                                                searchTags.add(tags.getString(k));
                                            }

                                            SearchVideo searchVideo = new SearchVideo(title, description, author, searchTags, searchImages, "YOUTUBE", id);
                                            searchVideos.add(searchVideo);
                                        }
                                    }
                                }
                                callback.onVideos(searchVideos, nextPageToken[0]);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);

        }
    }

    public void searchDeezer(String query, int nextPage, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", String.valueOf(query));
            jsonBody.put("deezer_page", nextPage);
            jsonBody.put("limit", 30);

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            final ArrayList<SearchAlbum> searchAlbums = new ArrayList<>();
            final ArrayList<SearchArtist> searchArtists = new ArrayList<>();
            final ArrayList<SearchTrack> youtubeSearchTracks = new ArrayList<>();

            final SearchPaging searchPaging = new SearchPaging();

            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = result.getString("type");

                                    if (type.equals(Type.YOUTUBE_SEARCH.toString())) {
                                        JSONObject tracks = result.getJSONObject("tracks");
                                        JSONObject tracks_meta = tracks.getJSONObject("meta");
                                        JSONArray tracks_result = tracks.getJSONArray("result");


                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String description = _item.getString("description");
                                            String author = _item.getString("channel_title");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();

                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "YOUTUBE");
                                                searchImages.add(image);
                                            }

                                            SearchTrack searchTrack = new SearchTrack(title, author, "", searchImages, "YOUTUBE", id);
                                            youtubeSearchTracks.add(searchTrack);
                                        }

                                    } else if (type.equals(Type.DEEZER_SEARCH.toString())) {

                                        JSONObject tracks = result.getJSONObject("tracks");
                                        JSONObject artists = result.getJSONObject("artists");
                                        JSONObject albums = result.getJSONObject("albums");

                                        JSONArray tracks_result = tracks.getJSONArray("result");
                                        JSONArray artists_result = artists.getJSONArray("result");
                                        JSONArray albums_result = albums.getJSONArray("result");

                                        /*searchPaging.setAlbumNextPage( String.valueOf(albums_meta.getInt("album_next_page")));
                                        searchPaging.setArtistNextPage( String.valueOf(artist_meta.getInt("artist_next_page")));
                                        searchPaging.setTrackNextPage( String.valueOf(tracks_meta.getInt("track_next_page")));*/


                                        searchPaging.setAlbumNextPage("-1");
                                        searchPaging.setArtistNextPage("-1");
                                        searchPaging.setTrackNextPage("-1");


                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String album_name = _item.getString("album_name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchTrack searchTrack = new SearchTrack(title, artist_name, album_name, searchImages, "DEEZER", id);
                                            searchTracks.add(searchTrack);
                                        }

                                        for (int j = 0; j < artists_result.length(); j++) {
                                            JSONObject _item = artists_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchArtist searchArtist = new SearchArtist(title, searchImages, "DEEZER", id);
                                            searchArtists.add(searchArtist);
                                        }

                                        for (int j = 0; j < albums_result.length(); j++) {
                                            JSONObject _item = albums_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchAlbum searchAlbum = new SearchAlbum(title, artist_name, searchImages, "DEEZER", id);
                                            searchAlbums.add(searchAlbum);
                                        }
                                    }
                                }
                                if(searchTracks.size() < 1){
                                    searchTracks.clear();
                                    searchTracks.addAll(youtubeSearchTracks);
                                }
                                callback.onSuccess(searchTracks, searchArtists, searchAlbums, searchPaging);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);

        }
    }


    public void search(String query, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", String.valueOf(query));

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            final ArrayList<SearchAlbum> searchAlbums = new ArrayList<>();
            final ArrayList<SearchArtist> searchArtists = new ArrayList<>();
            final ArrayList<SearchRadio> searchRadios = new ArrayList<>();
            final ArrayList<SearchVideo> searchVideos = new ArrayList<>();

            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = result.getString("type");

                                    if (type.equals(Type.AIRSTEM_RADIO.toString())) {

                                        JSONObject radios = result.getJSONObject("radios");
                                        JSONObject radio_meta = radios.getJSONObject("meta");
                                        JSONArray radios_result = radios.getJSONArray("result");

                                        for (int j = 0; j < radios_result.length(); j++) {
                                            JSONObject _item = radios_result.getJSONObject(j);
                                            String fullName = _item.getString("full_name");
                                            String title = _item.getString("title");
                                            String country = _item.getString("country");
                                            String maxUser = _item.getString("max_user");

                                            JSONArray url = _item.getJSONArray("url");
                                            String[] urlArray = new String[url.length()];
                                            for (int k = 0; k < url.length(); k++) {
                                                urlArray[k] = url.getString(k);
                                            }

                                            JSONArray genre = _item.getJSONArray("genre");
                                            String[] genreArray = new String[genre.length()];
                                            for (int k = 0; k < genre.length(); k++) {
                                                genreArray[k] = genre.getString(k);
                                            }

                                            SearchRadio searchRadio = new SearchRadio(title, maxUser, urlArray, genreArray, country, null);
                                            searchRadios.add(searchRadio);
                                        }

                                    } else if (type.equals(Type.YOUTUBE_SEARCH.toString())) {
                                        JSONObject tracks = result.getJSONObject("tracks");
                                        JSONObject tracks_meta = tracks.getJSONObject("meta");
                                        JSONArray tracks_result = tracks.getJSONArray("result");


                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String description = _item.getString("description");
                                            String author = _item.getString("channel_title");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();

                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "YOUTUBE");
                                                searchImages.add(image);
                                            }

                                            JSONArray tags = _item.getJSONArray("tags");
                                            ArrayList<String> searchTags = new ArrayList<>();

                                            for (int k = 0; k < tags.length(); k++) {
                                                searchTags.add(tags.getString(k));
                                            }

                                            SearchVideo searchVideo = new SearchVideo(title, description, author, searchTags, searchImages, "YOUTUBE", id);
                                            searchVideos.add(searchVideo);
                                        }

                                    } else if (type.equals(Type.DEEZER_SEARCH.toString())) {
                                        JSONObject tracks = result.getJSONObject("tracks");
                                        JSONObject artists = result.getJSONObject("artists");
                                        JSONObject albums = result.getJSONObject("albums");

                                        JSONObject tracks_meta = tracks.getJSONObject("meta");
                                        JSONObject artists_meta = artists.getJSONObject("meta");
                                        JSONObject albums_meta = albums.getJSONObject("meta");

                                        JSONArray tracks_result = tracks.getJSONArray("result");
                                        JSONArray artists_result = artists.getJSONArray("result");
                                        JSONArray albums_result = albums.getJSONArray("result");


                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String album_name = _item.getString("album_name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchTrack searchTrack = new SearchTrack(title, artist_name, album_name, searchImages, "DEEZER", id);
                                            searchTracks.add(searchTrack);
                                        }

                                        for (int j = 0; j < artists_result.length(); j++) {
                                            JSONObject _item = artists_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchArtist searchArtist = new SearchArtist(title, searchImages, "DEEZER", id);
                                            searchArtists.add(searchArtist);
                                        }

                                        for (int j = 0; j < albums_result.length(); j++) {
                                            JSONObject _item = albums_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchAlbum searchAlbum = new SearchAlbum(title, artist_name, searchImages, "DEEZER", id);
                                            searchAlbums.add(searchAlbum);
                                        }
                                    }
                                }
                                callback.onSearch(searchTracks, searchAlbums, searchArtists, searchVideos, searchRadios);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void searchArtists(String artist_name, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ARTISTS;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", String.valueOf(artist_name));

            final ArrayList<SearchArtist> searchArtists = new ArrayList<>();

            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject artists_meta = jsonObject.getJSONObject("meta");
                                    JSONArray artists_result = jsonObject.getJSONArray("result");

                                    //leave meta for now
                                    for (int j = 0; j < artists_result.length(); j++) {
                                        JSONObject _item = artists_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String id = _item.getString("id");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "DEEZER");
                                            searchImages.add(image);
                                        }

                                        SearchArtist searchArtist = new SearchArtist(title, searchImages, "DEEZER", id);
                                        searchArtists.add(searchArtist);
                                    }
                                }
                                callback.onSearch(null, null, searchArtists, null, null);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchAlbums(String artist_name, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ALBUMS;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", String.valueOf(artist_name));

            final ArrayList<SearchAlbum> searchAlbums = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONArray albums_result = jsonObject.getJSONArray("result");
                                    //leave meta for now

                                    if (albums_result.length() < 0) {
                                        callback.OnFailure("Array is null or empty");
                                    } else {
                                        for (int j = 0; j < albums_result.length(); j++) {
                                            JSONObject _item = albums_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String id = _item.getString("id");
                                            JSONArray images = _item.getJSONArray("images");
                                            ArrayList<SearchImage> searchImages = new ArrayList<>();
                                            for (int k = 0; k < images.length(); k++) {
                                                JSONObject __item = images.getJSONObject(k);
                                                String size = __item.getString("size");
                                                String url = __item.getString("url");
                                                SearchImage image = new SearchImage(size, url, "DEEZER");
                                                searchImages.add(image);
                                            }

                                            SearchAlbum searchAlbum = new SearchAlbum(title, artist_name, searchImages, "DEEZER", id);
                                            searchAlbums.add(searchAlbum);
                                        }
                                    }
                                }
                                callback.onSuccess(null, null, searchAlbums, new SearchPaging());
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchArtistById(String artistId, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ARTIST_INFO_DEEZER;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_id", artistId);

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    if (result.length() < 0) {
                                        callback.OnFailure("Array is null or empty");
                                    } else {
                                        JSONArray tracks_result = result.getJSONArray("tracks");
                                        JSONArray images = result.getJSONArray("images");
                                        //leave meta for now

                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String album_name = _item.getString("album_name");
                                            String id = _item.getString("id");
                                            SearchTrack searchTrack = new SearchTrack(title, artist_name, album_name, searchImages, "DEEZER", id);
                                            searchTracks.add(searchTrack);
                                        }

                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "DEEZER");
                                            searchImages.add(image);
                                        }

                                        //setting images
                                        for (SearchTrack searchTrack : searchTracks) {
                                            searchTrack.setArtworkUrl(searchImages);
                                        }
                                    }
                                }
                                callback.onSuccess(searchTracks, null, null, new SearchPaging());
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchAlbumTracks(String artist_name, String album_name, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ALBUM_INFO_LAST;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", String.valueOf(artist_name));
            jsonBody.put("album_name", String.valueOf(album_name));

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject tracks_meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    JSONArray tracks_result = result.getJSONArray("tracks");
                                    //leave meta for now

                                    for (int j = 0; j < tracks_result.length(); j++) {
                                        JSONObject _item = tracks_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String artist_name = _item.getString("artist_name");
                                        String id = _item.getString("mbid");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "LASTFM");
                                            searchImages.add(image);
                                        }

                                        SearchTrack searchTrack = new SearchTrack(title, artist_name, null, searchImages, "LASTFM", id);
                                        searchTracks.add(searchTrack);
                                    }
                                }
                                callback.onSuccess(searchTracks, null, null, new SearchPaging());
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void searchAlbumById(String albumId, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ALBUM_INFO_DEEZER;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("album_id", albumId);

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    if (result.length() < 0) {
                                        callback.OnFailure("Array is null or empty");
                                    } else {
                                        JSONArray tracks_result = result.getJSONArray("tracks");
                                        JSONArray images = result.getJSONArray("images");
                                        //leave meta for now

                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int j = 0; j < tracks_result.length(); j++) {
                                            JSONObject _item = tracks_result.getJSONObject(j);
                                            String title = _item.getString("name");
                                            String artist_name = _item.getString("artist_name");
                                            String album_name = _item.getString("album_name");
                                            String id = _item.getString("id");
                                            SearchTrack searchTrack = new SearchTrack(title, artist_name, album_name, searchImages, "DEEZER", id);
                                            searchTracks.add(searchTrack);
                                        }

                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "DEEZER");
                                            searchImages.add(image);
                                        }

                                        //setting images
                                        for (SearchTrack searchTrack : searchTracks) {
                                            searchTrack.setArtworkUrl(searchImages);
                                        }
                                    }
                                }
                                callback.onSuccess(searchTracks, null, null, new SearchPaging());
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchArtistAlbums(String artist_name, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ARTIST_ALBUMS;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", String.valueOf(artist_name));

            final ArrayList<SearchAlbum> searchAlbums = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject albums_meta = jsonObject.getJSONObject("meta");
                                    JSONArray albums_result = jsonObject.getJSONArray("result");
                                    //leave meta for now

                                    for (int j = 0; j < albums_result.length(); j++) {
                                        JSONObject _item = albums_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String artist_name = _item.getString("artist_name");
                                        String id = _item.getString("mbid");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "LASTFM");
                                            searchImages.add(image);
                                        }

                                        SearchAlbum searchAlbum = new SearchAlbum(title, artist_name, searchImages, "LASTFM", id);
                                        searchAlbums.add(searchAlbum);
                                    }
                                }
                                callback.onSearch(null, searchAlbums, null, null, null);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchArtistTracks(String artist_name, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ARTIST_TRACKS;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", String.valueOf(artist_name));

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject tracks_meta = jsonObject.getJSONObject("meta");
                                    JSONArray tracks_result = jsonObject.getJSONArray("result");
                                    //leave meta for now

                                    for (int j = 0; j < tracks_result.length(); j++) {
                                        JSONObject _item = tracks_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String artist_name = _item.getString("artist_name");
                                        String id = _item.getString("mbid");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "LASTFM");
                                            searchImages.add(image);
                                        }

                                        SearchTrack searchTrack = new SearchTrack(title, artist_name, null, searchImages, "LASTFM", id);
                                        searchTracks.add(searchTrack);
                                    }

                                }
                                callback.onSearch(searchTracks, null, null, null, null);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchTopData(final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_TOP_DATA;
            final JSONObject jsonBody = new JSONObject();

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();
            final ArrayList<SearchAlbum> searchAlbums = new ArrayList<>();
            final ArrayList<SearchArtist> searchArtists = new ArrayList<>();


            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = result.getString("type");

                                    JSONArray artists_result = result.getJSONArray("artists");
                                    JSONArray albums_result = result.getJSONArray("albums");
                                    JSONArray tracks_result = result.getJSONArray("tracks");


                                    for (int j = 0; j < tracks_result.length(); j++) {
                                        JSONObject _item = tracks_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String artist_name = _item.getString("artist_name");
                                        String album_name = _item.getString("album_name");
                                        String id = _item.getString("id");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "DEEZER");
                                            searchImages.add(image);
                                        }

                                        SearchTrack searchTrack = new SearchTrack(title, artist_name, album_name, searchImages, "DEEZER", id);
                                        searchTracks.add(searchTrack);
                                    }

                                    for (int j = 0; j < artists_result.length(); j++) {
                                        JSONObject _item = artists_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String id = _item.getString("id");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "DEEZER");
                                            searchImages.add(image);
                                        }

                                        SearchArtist searchArtist = new SearchArtist(title, searchImages, "DEEZER", id);
                                        searchArtists.add(searchArtist);
                                    }

                                    for (int j = 0; j < albums_result.length(); j++) {
                                        JSONObject _item = albums_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String artist_name = _item.getString("artist_name");
                                        String id = _item.getString("id");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "DEEZER");
                                            searchImages.add(image);
                                        }

                                        SearchAlbum searchAlbum = new SearchAlbum(title, artist_name, searchImages, "DEEZER", id);
                                        searchAlbums.add(searchAlbum);
                                    }

                                }
                                callback.onSearch(searchTracks, searchAlbums, searchArtists, null, null);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void searchSimilarData(String id, String track_name, String artist_name, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_SIMILAR_DATA;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("track_name", track_name);
            jsonBody.put("artist_name", artist_name);
            jsonBody.put("related_video_id", id);

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();

            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = result.getString("type");
                                    JSONArray tracks_result = result.getJSONArray("tracks");

                                    for (int j = 0; j < tracks_result.length(); j++) {
                                        JSONObject _item = tracks_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String description = _item.getString("description");
                                        String author = _item.getString("channel_title");
                                        String id = _item.getString("id");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "YOUTUBE");
                                            searchImages.add(image);
                                        }

                                        SearchTrack searchTrack = new SearchTrack(title, author, null, searchImages, "YOUTUBE", id);
                                        searchTracks.add(searchTrack);
                                    }
                                }
                                callback.onSearch(searchTracks, null, null, null, null);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchNewData(final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_NEW_DATA;
            final JSONObject jsonBody = new JSONObject();

            final ArrayList<SearchTrack> searchTracks = new ArrayList<>();

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");


                                    //leave meta for now
                                    String type = result.getString("type");
                                    JSONArray tracks_result = result.getJSONArray("tracks");

                                    for (int j = 0; j < tracks_result.length(); j++) {
                                        JSONObject _item = tracks_result.getJSONObject(j);
                                        String title = _item.getString("name");
                                        String description = _item.getString("description");
                                        String author = _item.getString("channel_title");
                                        String id = _item.getString("id");
                                        JSONArray images = _item.getJSONArray("images");
                                        ArrayList<SearchImage> searchImages = new ArrayList<>();
                                        for (int k = 0; k < images.length(); k++) {
                                            JSONObject __item = images.getJSONObject(k);
                                            String size = __item.getString("size");
                                            String url = __item.getString("url");
                                            SearchImage image = new SearchImage(size, url, "YOUTUBE");
                                            searchImages.add(image);
                                        }
                                        SearchTrack searchTrack = new SearchTrack(title, author, null, searchImages, "YOUTUBE", id);
                                        searchTrack.setUrl(String.valueOf(CollectionKey.SECRET_URL_BASE + id));
                                        searchTracks.add(searchTrack);
                                    }
                                }
                                callback.onTracks(searchTracks, null);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void searchArtistArtwork(String artist_name, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ARTIST_ARTWORK;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", artist_name);

            final ArrayList<SearchImage> searchImages = new ArrayList<>();

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {

                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = jsonObject.getString("type");
                                    JSONArray images = result.getJSONArray("images");

                                    for (int k = 0; k < images.length(); k++) {
                                        JSONObject __item = images.getJSONObject(k);
                                        String size = __item.getString("size");
                                        String url = __item.getString("url");
                                        SearchImage image = new SearchImage(size, url, type);
                                        searchImages.add(image);
                                    }

                                }
                                callback.onArtistImages(searchImages);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void searchAlbumArtwork(String artist_name, String album_name, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_ALBUM_ARTWORK;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", artist_name);
            jsonBody.put("album_name", album_name);

            final ArrayList<SearchImage> searchImages = new ArrayList<>();

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {


                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = jsonObject.getString("type");
                                    JSONArray images = result.getJSONArray("images");

                                    for (int k = 0; k < images.length(); k++) {
                                        JSONObject __item = images.getJSONObject(k);
                                        String size = __item.getString("size");
                                        String url = __item.getString("url");
                                        SearchImage image = new SearchImage(size, url, type);
                                        searchImages.add(image);
                                    }

                                }
                                callback.onArtistImages(searchImages);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void searchLyrics(String artist_name, String album_name, final Callback callback) throws IOException, JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.SERVER_BASE + CollectionConstant.ENDPOINT_SEARCH_LYRICS;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("artist_name", artist_name);
            jsonBody.put("album_name", album_name);

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {

                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                String text = null;
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject meta = jsonObject.getJSONObject("meta");
                                    JSONObject result = jsonObject.getJSONObject("result");

                                    //leave meta for now
                                    String type = jsonObject.getString("type");
                                    JSONObject lyricsObject = result.getJSONObject("lyrics");
                                    text = lyricsObject.getString("text");
                                }
                                callback.onLyrics(text);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void matchTrack(String name, String artistName, String albumName, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.MATCH_BASE + CollectionConstant.ENDPOINT_MATCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("artist_name", artistName);
            jsonBody.put("album_name", albumName);
            final String[] downloadUrl = {""};

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    JSONArray match_array = result.getJSONArray("match");

                                    for(int j = 0; j < match_array.length(); j++){
                                        JSONObject item = match_array.getJSONObject(j);
                                        if(item.has("type") && item.getString("type").equals(String.valueOf(Type.YOUTUBE_TRACK)) && item.getString("extension").equals("m4a")){
                                            downloadUrl[0] = item.getString("download_url");
                                            break;
                                        }else if(item.has("type") && item.getString("type").equals(String.valueOf(Type.MP3PM_TRACK))){
                                            downloadUrl[0] = item.getString("download_url");
                                            break;
                                        }else if(item.has("type")){
                                            downloadUrl[0] = item.getString("download_url");
                                        }

                                    }
                                    if(!TextUtils.isEmpty(downloadUrl[0])){
                                        break;
                                    }
                                }
                                callback.onMatch(downloadUrl[0]);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }


    public void manualMatchTrack(String name, String artistName, String albumName, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.MATCH_BASE + CollectionConstant.ENDPOINT_MATCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("manual_match", true);
            jsonBody.put("artist_name", artistName);
            jsonBody.put("album_name", albumName);
            final ArrayList<JSONObject> downloadUrls = new ArrayList<JSONObject>();

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    JSONArray match_array = result.getJSONArray("match");

                                    for(int j = 0; j < match_array.length(); j++){
                                        JSONObject item = match_array.getJSONObject(j);
                                        JSONObject _jsonObject = new JSONObject();
                                        if(item.has("type") && item.getString("type").equals(String.valueOf(Type.YOUTUBE_TRACK)) && item.getString("extension").equals("m4a")){
                                            _jsonObject.put("download_url", item.getString("download_url"));
                                            _jsonObject.put("provider", item.getString("type"));
                                            downloadUrls.add(_jsonObject);
                                        }else if(item.has("type") && item.getString("type").equals(String.valueOf(Type.MP3PM_TRACK))){
                                            _jsonObject.put("download_url", item.getString("download_url"));
                                            _jsonObject.put("provider", item.getString("type"));
                                            downloadUrls.add(_jsonObject);
                                        }else if(item.has("type")){
                                            _jsonObject.put("download_url", item.getString("download_url"));
                                            _jsonObject.put("provider", item.getString("type"));
                                            downloadUrls.add(_jsonObject);
                                        }
                                    }
                                }
                                callback.onVideoMatch(downloadUrls);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void matchVideo(String name, String[] videoIds, final Callback callback) throws JSONException {
        if (!isNetworkAvailable()) {
            callback.OnFailure("Network Error.");
        } else {
            String url = CollectionConstant.MATCH_BASE + CollectionConstant.ENDPOINT_MATCH;
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("video_ids", videoIds);
            final ArrayList<JSONObject> downloadUrls = new ArrayList<JSONObject>();

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {

                            JSONArray jsonArray = response.getJSONArray("messages");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                String text = null;
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    JSONArray match_array = result.getJSONArray("match");

                                    for(int j = 0; j < match_array.length(); j++){
                                        JSONObject item = match_array.getJSONObject(j);
                                        if(item.has("type") && item.getString("type").equals(String.valueOf(Type.YOUTUBE_TRACK)) && item.getString("extension").equals("mp4")){
                                            JSONObject _jsonObject = new JSONObject();
                                            _jsonObject.put("download_url", item.getString("download_url"));
                                            _jsonObject.put("width", item.getString("width"));
                                            _jsonObject.put("width", item.getString("height"));
                                            downloadUrls.add(_jsonObject);
                                        }
                                    }
                                }
                                callback.onVideoMatch(downloadUrls);
                            } else {
                                callback.OnFailure("Array is null or empty");
                            }
                        } catch (JSONException error) {
                            callback.OnFailure(error.getMessage());
                        }
                    } else {
                        callback.OnFailure("Response is null or empty");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyError e = error;
                    callback.OnFailure(error.getMessage());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }
}
