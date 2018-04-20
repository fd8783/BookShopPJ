package project.comp4342.bookshoppj;

import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v7.widget.SearchView;
import android.widget.Spinner;
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

public class SearchPage extends AppCompatActivity{

    public RecyclerView searchedBookView;
    private FloatingActionButton shoppingCartFab;

    private Spinner resultReference, rankReference;
    private ArrayAdapter<CharSequence> resultReferenceList, rankReferenceList;
    private String searchContent = "", searchColumn = "", rankColumn = "";

    private MenuItem searchBut;
    private SearchView searchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_book);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shoppingCartFab = (FloatingActionButton) findViewById(R.id.shopping_chart);
        shoppingCartFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchPage.this, ShoppingCart.class);
                startActivity(intent);
            }
        });

        searchedBookView = findViewById(R.id.show_searched_book);

        resultReference = findViewById(R.id.result_reference);
        resultReferenceList = ArrayAdapter.createFromResource(this, R.array.result_according_to_what, android.R.layout.simple_spinner_dropdown_item);
        resultReference.setAdapter(resultReferenceList);
        resultReference.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Search(searchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rankReference = findViewById(R.id.rank_reference);
        rankReferenceList = ArrayAdapter.createFromResource(this, R.array.rank_according_to_what, android.R.layout.simple_spinner_dropdown_item);
        rankReference.setAdapter(rankReferenceList);
        rankReference.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Search(searchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //set up first search
        Intent intent = getIntent();
        rankColumn = intent.getStringExtra("choice");
        if (!(rankColumn == null)){
            rankReference.setSelection(rankReferenceList.getPosition(rankColumn));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public String getSearchColumnString(String choice){
        if (choice.equals(this.getResources().getString(R.string.book_name))){
            return new String("b.bookName");
        }else if (choice.equals(this.getResources().getString(R.string.author_))){
            return new String("b.author");
        }else if (choice.equals(this.getResources().getString(R.string.publisher_))){
            return new String("b.publisher");
        }
        return null;
    }

    public String getRankColumnString(String choice){
        if (choice.equals(this.getResources().getString(R.string.search_date_new))){
            return new String("b.publishDate desc");
        }else if (choice.equals(this.getResources().getString(R.string.hot))){
            return new String("v.totalView desc");
        }else if (choice.equals(this.getResources().getString(R.string.price_high))){
            return new String("b.price desc");
        }else if (choice.equals(this.getResources().getString(R.string.price_low))){
            return new String("b.price");
        }else if (choice.equals(this.getResources().getString(R.string.rating_high))){
            return new String("v.avgRating desc");
        }else if (choice.equals(this.getResources().getString(R.string.rating_low))){
            return new String("v.avgRating");
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        searchBut = menu.findItem(R.id.searching);
        searchView = (SearchView) searchBut.getActionView();

//        searchView.setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Intent intent = new Intent(searchView.getContext(), SearchPage.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("ImgURL",query);
//                intent.putExtras(bundle);
//                searchView.getContext().startActivity(intent);
                Search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                if(newText.length()==0){
//                    Search(newText);
//                }
                Search(newText);
                return false;
            }
        });

        return true;
    }

    public void Search(String text){
        searchColumn = getSearchColumnString(resultReferenceList.getItem(resultReference.getSelectedItemPosition()).toString());
        rankColumn = getRankColumnString(rankReferenceList.getItem(rankReference.getSelectedItemPosition()).toString());
        LoadSearchedBook(text, searchColumn, rankColumn);
    }

    private void LoadSearchedBook(String searchContent, String searchColumn, String rankColumn){
        //set up the RecyclerView first
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        searchedBookView.setLayoutManager(layoutManager);
        searchedBookView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));
        searchedBookView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        searchedBookView.getRecycledViewPool().setMaxRecycledViews(0,10);

        List<String> IDList = new ArrayList<>(), imgURLList = new ArrayList<>(), nameList = new ArrayList<>(),
                priceList = new ArrayList<>(), authorList = new ArrayList<>(), publisherList = new ArrayList<>(), ratingList = new ArrayList<>();
        JSONArray searchedBookData;
        try{
            searchedBookData = new GetDataLongJsonArray().execute("getBookInfoLong.php",searchContent,searchColumn,rankColumn).get();
            for (int i =0; i < searchedBookData.length();i++){
                JSONArray getData = searchedBookData.getJSONArray(i);

                String IDString = getData.getString(0);
                IDList.add(IDString);

                String urlString = getData.getString(1);
                urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
                imgURLList.add(urlString);

                String nameString = getData.getString(2);
                nameList.add(nameString);

                String priceString = getData.getString(3);
                priceList.add(priceString);

                String authorString = getData.getString(4);
                authorList.add(authorString);

                String publisherString = getData.getString(5);
                publisherList.add(publisherString);

                String ratingString = getData.getString(6);
                ratingList.add(ratingString);
            }
        }catch (Exception e){
            Log.e("XgetNewPublishedImgURL", e.getMessage());
        }

        BookInfoLongAdapter searchedBookAdapter = new BookInfoLongAdapter(SearchPage.this ,IDList, imgURLList, nameList, priceList, authorList, publisherList, ratingList);
        searchedBookView.setAdapter(searchedBookAdapter);
    }

    private class GetDataLongJsonArray extends AsyncTask<String, String, JSONArray> {

        private JSONArray jsonArray = new JSONArray();

        String link = MainPage.serverURL;
        String searchContent, searchColumn, rankColumn;

        @Override
        protected JSONArray doInBackground(String... strings) {
            try{
                link = link+strings[0];
                searchContent = strings[1];
                searchColumn = strings[2];
                rankColumn = strings[3];

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("searchContent", "UTF-8") + "=" +
                        URLEncoder.encode(searchContent, "UTF-8");
                input += "&" + URLEncoder.encode("searchColumn", "UTF-8") + "=" +
                        URLEncoder.encode(searchColumn, "UTF-8");
                input += "&" + URLEncoder.encode("rankColumn", "UTF-8") + "=" +
                        URLEncoder.encode(rankColumn, "UTF-8");

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
                JSONObject dataJson;
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
//    protected void onPostExecute(JSONArray result){
//        this.jsonArray = result;
//    }
    }
}
