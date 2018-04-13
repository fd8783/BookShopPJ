package project.comp4342.bookshoppj;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.util.TypedValue;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class NavDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public ViewPager eventViewer;
    public RecyclerView newPublishedView, bookRecommendView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LoadEventPageViewer();
        LoadNewPublishedBook();
        LoadBookRecommend();
    }

    public void LoadEventPageViewer(){
        //Part of PagerViewer
        eventViewer = findViewById(R.id.eventViewer);
        PageAdapter pageAdapter = new PageAdapter(this);
        eventViewer.setAdapter(pageAdapter);

        int eventCount = pageAdapter.getCount();
        final RadioGroup radioButList = findViewById(R.id.radioButList);

        //add radio button into the radio group
        RadioButton radioBut;
        int dp10ToPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10, getResources().getDisplayMetrics());
        for(int i =0; i<eventCount;i++){
            radioBut = new RadioButton(this);
            radioBut.setButtonDrawable(R.drawable.radio_custom);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            radioBut.setPadding(dp10ToPx,0,0,dp10ToPx);
            //disable receive clicking from user
            radioBut.setEnabled(false);
            radioButList.addView(radioBut);
        }
        if (eventCount > 0){
            radioBut = (RadioButton) radioButList.getChildAt(0);
            radioBut.setChecked(true);
        }

        eventViewer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            RadioButton radioButNC;
            @Override
            public void onPageSelected(int position) {
                radioButNC = (RadioButton) radioButList.getChildAt(position);
                radioButNC.setChecked(true);
                //Toast.makeText(NavDrawer.this, "u", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //cause lag, don't know why
//        radioButList.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) { //start from 1??? wtf????
//                eventViewer.setCurrentItem(checkedId-1);
//                Toast.makeText(NavDrawer.this, "c", Toast.LENGTH_SHORT).show();
//            }
//        });

        //end Part of PageViewer
    }

    public void LoadNewPublishedBook(){
        //Part of newPublished (RecyclerView)
        //set up the RecyclerView first
        newPublishedView = findViewById(R.id.newPublished);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newPublishedView.setLayoutManager(layoutManager);
        newPublishedView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));
        newPublishedView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        newPublishedView.getRecycledViewPool().setMaxRecycledViews(0,10);

        List<String> imgURLList = new ArrayList<>(), nameList = new ArrayList<>(), priceList = new ArrayList<>(), authorList = new ArrayList<>();
        JSONArray newPublishData;
        try{
            //new GetBookInfoShort(imgURLList).execute().get();
            newPublishData = new getDataJsonArray().execute("getBookInfoShort.php").get();
            for (int i =0; i < newPublishData.length();i++){
                JSONArray getData = newPublishData.getJSONArray(i);

                String urlString = getData.getString(0);
                urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
                imgURLList.add(urlString);

                String nameString = getData.getString(1);
                nameList.add(nameString);

                String priceString = getData.getString(2);
                priceList.add(priceString);

                String authorString = getData.getString(3);
                authorList.add(authorString);
            }
        }catch (Exception e){
            Log.e("XgetNewPublishedImgURL", e.getMessage());
        }

        BookInfoShortAdapter newPublishedAdapter = new BookInfoShortAdapter(imgURLList, nameList, priceList, authorList);
        newPublishedView.setAdapter(newPublishedAdapter);

        //End Part of newPublished (RecyclerView)
    }

    public void LoadBookRecommend(){
        //Part of bookRecommend (RecyclerView)
        //set up the RecyclerView first
        bookRecommendView = findViewById(R.id.recommandBook);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bookRecommendView.setLayoutManager(layoutManager);
        bookRecommendView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));
        bookRecommendView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        bookRecommendView.getRecycledViewPool().setMaxRecycledViews(0,10);

        List<String> imgURLListBR = new ArrayList<>(), nameListBR = new ArrayList<>(), priceListBR = new ArrayList<>(), authorListBR = new ArrayList<>();
        JSONArray bookRecommendData;
        try{
            //new GetBookInfoShort(imgURLList).execute().get();
            bookRecommendData = new getDataJsonArray().execute("getBookRecommendShort.php").get();
            for (int i =0; i < bookRecommendData.length();i++){
                JSONArray getData = bookRecommendData.getJSONArray(i);

                String urlString = getData.getString(0);
                urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
                imgURLListBR.add(urlString);

                String nameString = getData.getString(1);
                nameListBR.add(nameString);

                String priceString = getData.getString(2);
                priceListBR.add(priceString);

                String authorString = getData.getString(3);
                authorListBR.add(authorString);
            }
        }catch (Exception e){
            Log.e("XgetBookRecommendImgURL", e.getMessage());
        }



        BookInfoShortAdapter bookRecommendAdapter = new BookInfoShortAdapter(imgURLListBR, nameListBR, priceListBR, authorListBR);
        bookRecommendView.setAdapter(bookRecommendAdapter);

        //End Part of bookRecommend (RecyclerView)
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetBookInfoShort extends AsyncTask<String,String,List<String>>{
        private List<String> urlList = new ArrayList<>(), nameList = new ArrayList<>(), priceList = new ArrayList<>();

        String link = MainActivity.serverURL+"getBookInfoShort.php";
        //Context context;

        //flag 0 means get and 1 means post.(By default it is get.) *update:didn't use flag here
        public GetBookInfoShort(List<String> urlList) {
            this.urlList = urlList;
            //this.context = context;
            //currently it should become http://tommyhui.tech/getevent.php
            //link = context.getString(R.string.serverURL)+link;
        }

        @Override
        protected  List<String> doInBackground(String... strings) {
            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                //StringBuffer sb = new StringBuffer();
                String data;
                JSONObject dataJson;
                while ((data = reader.readLine()) != null){
                    if (data.charAt(0) == '['){
                        //it means it is the json data, not <html>/<body>...
                        JSONArray getJson = new JSONArray(data);
                        for (int i =0; i < getJson.length();i++){
                            String urlString = getJson.getString(i);
                            urlString = urlString.substring(2,urlString.length()-2).replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
                            urlList.add(urlString);
                        }
                        //Log.e("tgfjdkgjdfkl",urlList.get(0));
                    }
                }

                return urlList;

            }catch (Exception e){
                return null;
                //return new String ("Excepiton:"+e.getMessage());
            }
        }

        protected void onPostExecute(List<String> result){
            this.urlList = result;
        }
    }
}
