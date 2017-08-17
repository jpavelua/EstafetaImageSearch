package com.jpavel.estafetaimagesearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/* Utils */
public class U {



    private static final String KEY_LAT = "KEY_LAT";
    private static final String KEY_LON = "KEY_LON";


    public static void saveCurrentLocation(Context c, Location l){
        saveCurrentLocation(c, l.getLatitude(), l.getLongitude());
    }

    public static void saveCurrentLocation(Context c, double lat, double lon){

        c.getSharedPreferences(S.SHARED_PREFS_FILE, c.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LON, Double.doubleToRawLongBits(lon))
                .commit();
    }

    public static Location getCurrentLocation(Context c){

        SharedPreferences sp = c.getSharedPreferences(S.SHARED_PREFS_FILE, c.MODE_PRIVATE);

        Location l = new Location("");
        l.setLatitude(Double.longBitsToDouble(sp.getLong(KEY_LAT, -1)));
        l.setLongitude(Double.longBitsToDouble(sp.getLong(KEY_LON, -1)));

        return l;
    }

    public static File getAppImagesFolder(){
        String rootPath = Environment.getExternalStorageDirectory().toString();
        return new File(rootPath + File.separator +S.APP_IMAGE_DIRECTORY);
    }

    @NonNull
    static String getSearchUrl(String strToSearch, String apiKey, String searchEngineID) {

        String strNoSpaces = strToSearch.replace(" ", "+");

        StringBuilder sbRequest = new StringBuilder(S.SEARCH_API_HOST);
        sbRequest.append("?");
        sbRequest.append(String.format("%s=%s", "q",            strNoSpaces));
        sbRequest.append("&");
        sbRequest.append(String.format("%s=%s", "key",          apiKey));
        sbRequest.append("&");
        sbRequest.append(String.format("%s=%s", "cx",           searchEngineID));
        sbRequest.append("&");
        sbRequest.append(String.format("%s=%s", "alt",          "json"));
        sbRequest.append("&");
        sbRequest.append(String.format("%s=%s", "searchType",   "image"));


        return sbRequest.toString();
    }

    static String getTestSearchJson(Context c) {

        String result = null;

        try {
            Resources res = c.getResources();
            InputStream in_s = res.openRawResource(R.raw.test_response);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            result = new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            Log.e(S.TAG, e.getMessage());
        }

        return result;
    }

    static List<ImageModel> parseSearchJson(Context c, String searchJson){

        List<ImageModel> imagesList = new ArrayList<>();

        try {

            JSONObject joRoot = new JSONObject(searchJson);
            if(joRoot.has("items")){

                JSONArray jaItems = joRoot.getJSONArray("items");

                for(int i = 0; i < jaItems.length(); i++){

                    JSONObject item = jaItems.getJSONObject(i);

                    String imageTitle = null;
                    String imageLink  = null;

                    if(item.has("title"))
                        imageTitle = item.getString("title");
                    if(item.has("link"))
                        imageLink = item.getString("link");

                    if(!TextUtils.isEmpty(imageLink)){

                        long timestamp    = System.currentTimeMillis();
                        Location location = getCurrentLocation(c);

                        ImageModel im = new ImageModel(imageTitle, Uri.parse(imageLink), timestamp, location);

                        imagesList.add(im);
                    }


                }

            }


            System.out.println();




        }
        catch (Exception e) {
            e.printStackTrace();
        }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
        return imagesList;
    }

    public static List<ImageModel> getTestImages(){
        ImageModel[] imageModels = {
                new ImageModel("Afghanistan", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/afghan.gif")),
                new ImageModel("Albania", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/albania.gif")),
                new ImageModel("Algeria", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/algeria.gif")),
                new ImageModel("Andorra", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/andorra.gif")),
                new ImageModel("Angola", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/angola.gif")),
                new ImageModel("Bahamas", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/bahamas.gif")),
                new ImageModel("Bahrain", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/bahrain.gif")),
                new ImageModel("Bangladesh", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/2017-03/banglad.gif")),
                new ImageModel("Barbados", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/barbados.gif")),
                new ImageModel("Belarus", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/belarus.gif")),
                new ImageModel("Belgium", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/belgium.gif")),
                new ImageModel("Cambodia", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/cambodia.gif")),
                new ImageModel("Cape Verde", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/capeverd.gif")),
                new ImageModel("Chile", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/chile.gif")),
                new ImageModel("Comoros", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/comoros.gif")),
                new ImageModel("Costa Rica", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/costaric.gif")),
                new ImageModel("Cuba", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/cuba.gif")),
                new ImageModel("Denmark", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/denmark.gif")),
                new ImageModel("Dominican Republic", Uri.parse("https://www.infoplease.com/sites/infoplease.com/files/public%3A/domrep.gif"))
        };

        return Arrays.asList(imageModels);
    }

    public static List<ImageModel> getImagesFromTestJson(Context c) {
        String response = getTestSearchJson(c);
        return parseSearchJson(c, response);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static List<ImageModel> getImagesFromWeb(Context c, String searchString) {

//        https://www.googleapis.com/customsearch/v1?key=AIzaSyBUfa1YqLO3YLTJFZKRT4JinxHxnTNy4T8&q=flower&searchType=image&fileType=jpg&imgSize=xlarge&alt=json&cx=004160727337875953838:xmj6fvuai_c
        String url = getSearchUrl(searchString, S.API_KEY, S.SEARCH_ENGINE_ID);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            responseStr = response.body().string();
        } catch (IOException e) {
            Log.e(S.TAG, e.getMessage());
            responseStr = "";
        }

        return parseSearchJson(c, responseStr);
    }
}
