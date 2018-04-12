package project.comp4342.bookshoppj;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EventContext extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_context);

        Toolbar tool = (Toolbar) findViewById(R.id.eventPageToolBar);
        tool.setTitle("活動");
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String imgURL = intent.getStringExtra("ImgURL");
        String description = "";
        ImageView img;
        TextView text;
        img = findViewById(R.id.eventImg);
        text = findViewById(R.id.eventDescription);

        try{
            description = new getEventDescription().execute(imgURL).get();
            new imgdler(img).execute(imgURL).get();
        }
        catch(Exception e){

        }
        text.setText(description);
    }

    @Override
    public void onBackPressed(){
        PageAdapter.eventDescriptionOpened = false;
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class imgdler extends AsyncTask<String,Void, Bitmap> {
           ImageView bmImg;

        public imgdler(ImageView img){
            bmImg=img;
        }

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
            bmImg.setImageBitmap(result);
        }

    }

    private class getEventDescription extends AsyncTask<String,String,String>{
        private String description;

        String link = "geteventdescription.php";
        //Context context;

        //flag 0 means get and 1 means post.(By default it is get.)
        public getEventDescription() {
            link = MainActivity.serverURL+link;
        }

        @Override
        protected  String doInBackground(String... strings) {
            String imgUrl = strings[0];
            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("url", "UTF-8") + "=" +
                        URLEncoder.encode(imgUrl, "UTF-8");

                //solve ioe exception: unexpected end of stream on com.android.okhttp.address
//                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
//                    conn.setRequestProperty("Connection", "close");
//                }

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(input);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String data;

                //seem it can't read text that too long (show nothing)
                while ((data = reader.readLine()) != null){
                    sb.append(data+"\n");
                }

                data = sb.toString().split("<body>")[1];//remove prefix (e.g. <body> tag)
                data = data.split("<div")[0];//remove prefix (e.g. <div> tag)
                data = data.split("</body")[0];//remove postfix (e.g. <div> tag)

                return data;

            }catch (Exception e){
                Log.e("getDatabaseContentError",e.getMessage());
                return null;
                //return new String ("Excepiton:"+e.getMessage());
            }

        }

    }
}
