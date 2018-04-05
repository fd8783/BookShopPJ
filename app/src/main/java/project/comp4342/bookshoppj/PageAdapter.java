package project.comp4342.bookshoppj;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by fd8783 on 4/4/2018.
 */

public class PageAdapter extends PagerAdapter {

    private int count;
    private LayoutInflater inflater;
    private Context context;
    private Bitmap[] imgBitamp;
    private ImageView imgFromURL;

    public PageAdapter(Context context){
        this.context = context;
        count = 5;
        imgBitamp = new Bitmap[count];

        try{
            //String link = "http://fd8783.000webhostapp.com/image/event/bookfair2015.jpg";
            String link = "https://i.imgur.com/uegEhQb.jpg";

            for (int i =0; i< count; i++){
                imgBitamp[i] = new ImgDownloader().execute(link).get();
                //imgBitamp[i]=img;
            }
        }
        catch (Exception e){
            Toast.makeText(context,"Exception from getting image from url",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View eventlayout = inflater.inflate(R.layout.eventview, null);
        ImageView img = eventlayout.findViewById(R.id.eventImage);
        //new ImgDownloader(img).execute("https://i.imgur.com/1wBp1Vj.png");
        img.setImageBitmap(imgBitamp[position]);
        //img.setImageBitmap(bmp);

        eventlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context ,"you clicked "+position, Toast.LENGTH_SHORT).show();
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

    private class imgdler{
        String url;
        public imgdler(String url){
            this.url = url;

        }
        public Bitmap getBitmap(){
            String urlGot = url;
            Bitmap bmp = null;

            try{
                InputStream in = new URL(urlGot).openStream();
                bmp = BitmapFactory.decodeStream(in);
            }
            catch (Exception e){
                Log.e("Error getting url",e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
    }
}
