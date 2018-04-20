package project.comp4342.bookshoppj;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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

public class BookContext extends AppCompatActivity {

    private boolean firstLoaded = true, programTrigger = false;

    private ProgressBar progressBar;
    private FloatingActionButton shoppingCartFab;
    private Toolbar tool;

    private String bookID, urlString, bookNameString, bookPriceString, bookAuthorString;
    private ImageView bookImg;
    private TextView bookName, bookAuthor, bookPublisher, bookPrice, bookRating, bookBrief;
    private Spinner userRated;
    private ArrayAdapter<CharSequence> userRateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_info_detail);

        tool = (Toolbar) findViewById(R.id.toolbar);
        tool.setTitle("");
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookImg = findViewById(R.id.bookImg_long);
        bookName = findViewById(R.id.bookName_long);
        bookAuthor = findViewById(R.id.bookAuthor_long);
        bookPublisher = findViewById(R.id.bookPublisher_long);
        bookPrice = findViewById(R.id.bookPrice_long);
        bookRating = findViewById(R.id.bookRating_long);
        bookBrief = findViewById(R.id.bookBrief);
        progressBar = findViewById(R.id.imgLoadingBar);
        userRated = findViewById(R.id.yourRating);
        if (MainPage.isLogin){

            userRateList = ArrayAdapter.createFromResource(this, R.array.rating, android.R.layout.simple_spinner_dropdown_item);
            userRated.setAdapter(userRateList);
            userRated.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String ratedContent = userRateList.getItem(position).toString();
                    try{
                        if (firstLoaded){
                            ratedContent = new UpdateBookRating(BookContext.this).execute("updateBookRating.php" ,MainPage.currentAccountID, bookID, "").get();
                            firstLoaded = false;
                            if (ratedContent.equals("-1"))
                                userRated.setSelection(userRateList.getPosition(BookContext.this.getResources().getString(R.string.please_select)));
                            else
                                userRated.setSelection(userRateList.getPosition(ratedContent));
                        }
                        else{
                            new UpdateBookRating(BookContext.this).execute("updateBookRating.php" ,MainPage.currentAccountID, bookID, ratedContent).get();
                            bookRating.setText(new GetAvgRating().execute("getAvgRatingByID.php", bookID).get());
                        }
                    }
                    catch (Exception e){
                        Log.e("updateRatingFail", e.getMessage());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        else {
            userRateList = ArrayAdapter.createFromResource(this, R.array.please_login, android.R.layout.simple_spinner_dropdown_item);
            userRated.setAdapter(userRateList);
        }

        Intent intent = getIntent();
        bookID = intent.getStringExtra("bookID");

        LoadBook(bookID);

        shoppingCartFab = (FloatingActionButton) findViewById(R.id.add_to_shopping_chart);
        shoppingCartFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainPage.localDB.isShoppingCartContain(bookID)){
                    Snackbar.make(view, "此書本已在購物車中", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else{
                    MainPage.localDB.InsertShoppingCart(Integer.parseInt(bookID), urlString, bookNameString, Float.valueOf(bookPriceString), bookAuthorString);
                    Snackbar.make(view, "已加到購物車", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        //add View Count
        if (SettingPage.isBrowseHistorySaved)
            new AddView().execute("addView.php", MainPage.currentAccountID, bookID);
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
            progressBar.setVisibility(View.GONE);
            bmImg.setImageBitmap(result);
        }

    }

    private void LoadBook(String bookID){
        JSONArray searchedBookData;
        try{
            searchedBookData = new GetDataLongJsonArray().execute("getBookDetailByID.php", bookID).get();
            JSONArray getData = searchedBookData.getJSONArray(0);

            urlString = getData.getString(0);
            urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
            //new imgdler(bookImg).execute(urlString).get();
            Glide.with(BookContext.this)
                    .load(urlString)
                    .into(bookImg);
            progressBar.setVisibility(View.GONE);

            bookNameString = getData.getString(1);
            tool.setTitle(bookNameString);
            bookName.setText(this.getResources().getString(R.string.bookname) + bookNameString);

            bookAuthorString = getData.getString(2);
            bookAuthor.setText(this.getResources().getString(R.string.author) + bookAuthorString);

            bookPublisher.setText(this.getResources().getString(R.string.publisher) + getData.getString(3));

            bookPriceString = getData.getString(8);
            bookPrice.setText(this.getResources().getString(R.string.price) + bookPriceString);

            bookRating.setText(this.getResources().getString(R.string.rating) + getData.getString(9));

            String bookContent = getData.getString(10);
            bookContent = bookContent.replace("\\r\\n","\n");
            bookBrief.setText(this.getResources().getString(R.string.brief) + bookContent);

        }catch (Exception e){
            Log.e("XgetNewPublishedImgURL", e.getMessage());
        }
    }

    private class GetDataLongJsonArray extends AsyncTask<String, String, JSONArray> {

        private JSONArray jsonArray = new JSONArray();

        private String link = MainPage.serverURL;
        private String bookID;

        @Override
        protected JSONArray doInBackground(String... strings) {
            try{
                link = link+strings[0];
                bookID = strings[1];

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("bookID", "UTF-8") + "=" +
                        URLEncoder.encode(bookID, "UTF-8");

                //solve ioe exception: unexpected end of stream on com.android.okhttp.address
//                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
//                    conn.setRequestProperty("Connection", "close");
//                }

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(input);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                //StringBuffer sb = new StringBuffer();
                String data;
                while ((data = reader.readLine()) != null){
                    if (data.charAt(0) == '['){
                        //it means it is the json data, not <html>/<body>...
                        jsonArray = new JSONArray(data);
                        break;
                    }
                }
                return jsonArray;
            }catch (Exception e){
                Log.e("GetLongJsonArrayError", e.getMessage());
                return null;
            }
        }
    }

    private class UpdateBookRating extends AsyncTask<String, String, String> {

        private JSONArray jsonArray = new JSONArray();

        private String link = MainPage.serverURL;
        private String userID, bookID, ratings;
        private Context context;

        public UpdateBookRating(Context context){
             this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                link = link+strings[0];
                userID = strings[1];
                bookID = strings[2];
                ratings = strings[3];

                if (ratings.equals(context.getResources().getString(R.string.please_select)))
                    ratings = new String("clean");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
                input += "&" + URLEncoder.encode("bookID", "UTF-8") + "=" +
                        URLEncoder.encode(bookID, "UTF-8");
                input += "&" + URLEncoder.encode("ratings", "UTF-8") + "=" +
                        URLEncoder.encode(ratings, "UTF-8");

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

                while ((data = reader.readLine()) != null){
                    sb.append(data);
                }


                data = sb.toString().split("<body>")[1];//remove prefix (e.g. <body> tag)
                data = data.split("<div")[0];//remove prefix (e.g. <div> tag)
                data = data.split("</body")[0];//remove postfix (e.g. <div> tag)

                return data;

            }catch (Exception e){
                Log.e("GetLongJsonArrayError", e.getMessage());
                return null;
            }
        }

//        @Override
//        protected void onPostExecute(String result) {
//
//            //this method will be running on UI thread
//            if (result.equals("exception")) {
//                Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
//            }
//            else{
//                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
//            }
//        }
    }

    private class GetAvgRating extends AsyncTask<String, String, String> {

        private String link = MainPage.serverURL;
        private String bookID;

        @Override
        protected String doInBackground(String... strings) {
            try{
                link = link+strings[0];
                bookID = strings[1];

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("bookID", "UTF-8") + "=" +
                        URLEncoder.encode(bookID, "UTF-8");

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

                while ((data = reader.readLine()) != null){
                    sb.append(data);
                }


                data = sb.toString().split("<body>")[1];//remove prefix (e.g. <body> tag)
                data = data.split("<div")[0];//remove prefix (e.g. <div> tag)
                data = data.split("</body")[0];//remove postfix (e.g. <div> tag)

                return data;

            }catch (Exception e){
                Log.e("GetLongJsonArrayError", e.getMessage());
                return new String("N/A");
            }
        }
    }

    private class AddView extends AsyncTask<String, String, String> {

        private String link = MainPage.serverURL;
        private String userID, bookID;

        @Override
        protected String doInBackground(String... strings) {
            try{
                link = link+strings[0];
                userID = strings[1];
                bookID = strings[2];


                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
                input += "&" + URLEncoder.encode("bookID", "UTF-8") + "=" +
                        URLEncoder.encode(bookID, "UTF-8");

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

                while ((data = reader.readLine()) != null){
                    sb.append(data);
                }


                data = sb.toString().split("<body>")[1];//remove prefix (e.g. <body> tag)
                data = data.split("<div")[0];//remove prefix (e.g. <div> tag)
                data = data.split("</body")[0];//remove postfix (e.g. <div> tag)

                return data;

            }catch (Exception e){
                Log.e("GetLongJsonArrayError", e.getMessage());
                return new String("N/A");
            }
        }
    }
}
