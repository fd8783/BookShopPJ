package project.comp4342.bookshoppj;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fd8783 on 4/4/2018.
 */

public class PageAdapter extends PagerAdapter {

    public static boolean eventDescriptionOpened = false;

    private List<String> urlList = new ArrayList<>();

    private int count;
    private LayoutInflater inflater;
    private Context context;
    private Bitmap[] imgBitamp;
    private ImageView imgFromURL;

    public PageAdapter(Context context){
        this.context = context;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        try{
            urlList = new GetEvent().execute().get();    //maybe i should use get()

            count = urlList.size();
            imgBitamp = new Bitmap[count];

            for (int i =0; i< count; i++){
                imgBitamp[i] = new ImgDownloader().execute(urlList.get(i)).get();
                //imgBitamp[i]=img;
            }
        }
        catch (Exception e){
            Toast.makeText(context,"Exception from getting image from url",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View eventlayout = inflater.inflate(R.layout.eventview, null);
        ImageView img = eventlayout.findViewById(R.id.eventImage);
        img.setImageBitmap(imgBitamp[position]);

        eventlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //avoid fast clicking open same intent multiple times
                if (!eventDescriptionOpened) {
                    eventDescriptionOpened = true;
                    final Intent intent = new Intent(v.getContext(), EventContext.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ImgURL", urlList.get(position));
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }

                //Toast.makeText(context ,"you clicked "+position, Toast.LENGTH_SHORT).show();
            }
        });
        ViewPager vp = (ViewPager) container;
        vp.addView(eventlayout, 0);
        return eventlayout;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    private class GetEvent extends AsyncTask<String,String,List<String>>{
        private List<String> urlList = new ArrayList<>();

        String link = MainActivity.serverURL+"getevent.php";
        //Context context;

        //flag 0 means get and 1 means post.(By default it is get.) *update:didn't use flag here
        public GetEvent() {
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
//
//        protected void onPostExecute(String result){
//            Toast.makeText(context,result,Toast.LENGTH_SHORT);
//        }

    }
}
