package project.comp4342.bookshoppj;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fd8783 on 13/4/2018.
 */

public class ShoppingCart extends AppCompatActivity{

    public static float total_price = 0;
    public static List<String> amountList = new ArrayList<>();

    List<String> IDList = new ArrayList<>(), imgURLList = new ArrayList<>(), nameList = new ArrayList<>(),
            priceList = new ArrayList<>(), authorList = new ArrayList<>(), stockList = new ArrayList<>();

    private RecyclerView showCartBookView;

    private EditText amountInput;
    private static TextView totalCountText;
    private static String totalCountString;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_cart);

        total_price = 0;

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showCartBookView = findViewById(R.id.show_cart_book);
        totalCountText = findViewById(R.id.total_count_text);
        totalCountString = getResources().getString(R.string.total_count);
        payButton = findViewById(R.id.pay_button);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainPage.isLogin){
                    try{
                        String check = new checkAbleToPay().execute("checkAbleToPay.php").get();
                        if (check.equals("1")){
                            String orderNo = new getMaxOrderNo().execute("getMaxOrderNo.php").get();
                            orderNo = Integer.toString(Integer.parseInt(orderNo)+1);
                            for (int i = 0; i< showCartBookView.getAdapter().getItemCount(); i++){
                                if (Integer.parseInt(amountList.get(i)) > 0){
                                    new addBuyRecord().execute("addBuyRecord.php", orderNo, IDList.get(i), amountList.get(i)).get();
                                    MainPage.localDB.DeleteFromShoppingCart(Integer.parseInt(IDList.get(i)));
                                }
                            }
                            Snackbar.make(v, getResources().getString(R.string.pay_done), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            Toast.makeText(ShoppingCart.this,getResources().getString(R.string.pay_done),Toast.LENGTH_SHORT);
                            finish();
                        }
                        else{
                            Snackbar.make(v, getResources().getString(R.string.please_add_info_first), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            Toast.makeText(ShoppingCart.this,getResources().getString(R.string.please_add_info_first),Toast.LENGTH_SHORT);
                        }
                    }catch (Exception e){
                        Log.e("checkPayAbleError",e.getMessage());
                    }
                }
                else{
                    Snackbar.make(v, getResources().getString(R.string.please_login_first), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Toast.makeText(ShoppingCart.this,getResources().getString(R.string.please_login_first),Toast.LENGTH_LONG);
                }
            }
        });

        LoadSearchedBook();

        UpdateTotalPrice();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer_fake, menu);
        MenuItem searchBut = menu.findItem(R.id.searching);
        searchBut.setIcon(R.drawable.ic_refresh_black_24dp);
        searchBut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Refresh();
                return false;
            }
        });

        return true;
    }

    private void LoadSearchedBook(){
        //set up the RecyclerView first
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        showCartBookView.setLayoutManager(layoutManager);
        showCartBookView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));
        showCartBookView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        Cursor res = MainPage.localDB.GetAllShoppingCartItem();
        while (res.moveToNext()){
            IDList.add(res.getString(0));

            String urlString = res.getString(1);
            urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
            imgURLList.add(urlString);

            nameList.add(res.getString(2));
            priceList.add(res.getString(3));
            authorList.add(res.getString(4));

            try{
                stockList.add(new getBookStock().execute("getStockByID.php", IDList.get(IDList.size()-1)).get());
            }catch (Exception e){
                Log.e("getStockFail",e.getMessage());
            }
        }

        BookInfoCartAdapter cartBookAdapter = new BookInfoCartAdapter(ShoppingCart.this ,IDList, imgURLList, nameList, priceList, authorList, stockList);
        showCartBookView.setAdapter(cartBookAdapter);
    }

    public static void UpdateTotalPrice(){
        totalCountText.setText(totalCountString + Float.valueOf(total_price));
    }

    public static void UpdateTotalPrice(boolean check){
        if (!check)
            totalCountText.setText(new String("請刷新頁面"));
    }

    public void Refresh(){
        finish();
        startActivity(getIntent());
    }

    private class getBookStock extends AsyncTask<String,String,String>{
        private String link, bookID;

        @Override
        protected  String doInBackground(String... strings) {
            try{
                link = MainPage.serverURL+strings[0];
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

                //seem it can't read text that too long (show nothing)
                while ((data = reader.readLine()) != null){
                    sb.append(data);
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

    private class checkAbleToPay extends AsyncTask<String,String,String>{
        private String link, userID;

        @Override
        protected  String doInBackground(String... strings) {
            try{
                link = MainPage.serverURL+strings[0];
                userID = MainPage.currentAccountID;
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");

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
                    sb.append(data);
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

    private class getMaxOrderNo extends AsyncTask<String,String,String>{
        private String link;

        @Override
        protected  String doInBackground(String... strings) {
            try{
                link = MainPage.serverURL+strings[0];
                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                //solve ioe exception: unexpected end of stream on com.android.okhttp.address
//                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
//                    conn.setRequestProperty("Connection", "close");
//                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String data;

                //seem it can't read text that too long (show nothing)
                while ((data = reader.readLine()) != null){
                    sb.append(data);
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

    private class addBuyRecord extends AsyncTask<String,String,String>{
        private String link, orderNo, userID, bookID, amount;

        @Override
        protected  String doInBackground(String... strings) {
            try{
                link = MainPage.serverURL+strings[0];
                userID = MainPage.currentAccountID;
                orderNo = strings[1];
                bookID = strings[2];
                amount = strings[3];
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("orderNo", "UTF-8") + "=" +
                        URLEncoder.encode(orderNo, "UTF-8");
                input += "&" + URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
                input += "&" + URLEncoder.encode("bookID", "UTF-8") + "=" +
                        URLEncoder.encode(bookID, "UTF-8");
                input += "&" + URLEncoder.encode("amount", "UTF-8") + "=" +
                        URLEncoder.encode(amount, "UTF-8");

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
                    sb.append(data);
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
