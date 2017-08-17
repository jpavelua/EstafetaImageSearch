package com.jpavel.estafetaimagesearch;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 12;

    ImagesListFragment imagesListFragment;

    private ProgressBar pb;

    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private MenuItem menuItemSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


        imagesListFragment = new ImagesListFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.imagesListContainer, imagesListFragment)
                .commit();


        pb = findViewById(R.id.pb);

        mLocationListener = new GPSListener();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        managePermissions(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < permissions.length; i++) {
            String permGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED) ? "YES" : "NO";
            sb.append(permissions[i] + " : " + permGranted);
            sb.append("\n");
        }

//        Toast.makeText(this, "onRequestPermissionsResult:\n" + sb.toString(), Toast.LENGTH_SHORT).show();

        managePermissions(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.estafeta_main_menu, menu);


//        https://developer.android.com/training/search/setup.html
        // Associate searchable configuration with the SearchView
        menuItemSearch = menu.findItem(R.id.action_search);

        SearchView searchView       =  (SearchView) menuItemSearch.getActionView();

        SearchManager searchManager =  (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setQueryHint("Search View Hint");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                boolean writeToStorageGranted = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (!writeToStorageGranted) {
                    showStorageIsDisabledAlert();
                    return false;
                }

//                menuItemSearch.collapseActionView();
                pb.setVisibility(View.VISIBLE);

                new SearchAsyncTask().execute(query);

                return false;
            }

        });

        return true;
    }

    public void managePermissions(boolean atAppStart){

        if(atAppStart){

            boolean fineLocationGranted   = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            boolean coarseLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

            if (fineLocationGranted && coarseLocationGranted) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            } else {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    showGPSAlert();
            }
        }
        else{

            boolean fineLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (fineLocationGranted && coarseLocationGranted) {
                Location lkl = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lkl != null)
                    U.saveCurrentLocation(this, lkl);
                else
                    U.saveCurrentLocation(this, -1, -1);

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    showGPSAlert();
            }
        }

    }

    //<editor-fold desc="Alerts">

    private void showGPSAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void showStorageIsDisabledAlert() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("You did not allow the use of memory to save images. \nImages will not be saved to the card")
                .setCancelable(false)
                .setPositiveButton("Allow storage", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                },
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                    }
                })
                .setNegativeButton("Continue without allow", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        pb.setVisibility(View.GONE);

                        List<ImageModel> imagesList = U.getTestImages();
                        imagesListFragment.updateListFragment(imagesList);
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    //</editor-fold>


    class SearchAsyncTask extends AsyncTask<String, Integer, List<ImageModel>> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        String run(String url) throws IOException {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            }
        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected List<ImageModel> doInBackground(String... urls) {

            List<ImageModel> imagesList = new ArrayList<>();

//            ########  MAKE SEARCH API REQUEST

//            >> Plaint Fake images  [title + links]
//            imagesList = U.getTestImages();

//            >> Test Json from file res/raw/test_response.txt
//            imagesList = U.getImagesFromTestJson(MainActivity.this);

//            >> Obtaining real value
            imagesList = U.getImagesFromWeb(MainActivity.this, urls[0]);


//            ########  LOAD IMAGE from link and SAVE TO STORAGE

            saveLoadedImagesToStorage(imagesList);

            return imagesList;
        }

        @Override
        protected void onPostExecute(List<ImageModel> imagesList) {
            super.onPostExecute(imagesList);

            pb.setVisibility(View.GONE);

            imagesListFragment.updateListFragment(imagesList);
        }


        private void saveLoadedImagesToStorage(List<ImageModel> imagesList){

            File appImagesFolder = U.getAppImagesFolder();
            if (!appImagesFolder.exists())
                appImagesFolder.mkdirs();


            for (ImageModel im : imagesList){


                // TODO add if fole exists


                try {
                    Bitmap theBitmap = Glide.
                            with(MainActivity.this).
                            load(im.uri).
                            asBitmap().
                            into(100, 100). // Width and height
                            get();

                    im.filePath = saveBitmapToFile(theBitmap, appImagesFolder +  "/" + im.title + ".gif");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        }

        private String saveBitmapToFile(Bitmap bitmapImage, String filePath){

            File imageFile = new File(filePath);

            OutputStream fOutputStream = null;

            try {
                fOutputStream = new FileOutputStream(imageFile);

                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

                fOutputStream.flush();
                fOutputStream.close();

                MediaStore.Images.Media.insertImage(getContentResolver(), imageFile.getAbsolutePath(), imageFile.getName(), imageFile.getName());
            } catch (FileNotFoundException e) {
                Log.d(S.TAG, e.getMessage());
            } catch (IOException e) {
                Log.d(S.TAG, e.getMessage());
            }

            return imageFile.getAbsolutePath();
        }
    }

    class GPSListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MainActivity.this, "lat=" + location.getLatitude() + " lon=" + location.getLongitude(), Toast.LENGTH_SHORT).show();

            U.saveCurrentLocation(MainActivity.this, location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    }
}
