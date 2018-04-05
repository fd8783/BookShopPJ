package project.comp4342.bookshoppj;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by fd8783 on 5/4/2018.
 */

public class ImgDownloader extends AsyncTask<String,Void, Bitmap> {
 //   ImageView bmImg;

//    public ImgDownloader(ImageView img){
//        bmImg=img;
//    }

    protected Bitmap doInBackground(String... urls){
        String urlGot = urls[0];
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

    protected void onPostExecute(Bitmap result){
        //bmImg.setImageBitmap(result);
    }

}
