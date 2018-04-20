package project.comp4342.bookshoppj;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.util.TypedValue;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String serverURL;
    public static String currentAccountID;
    public static String currentAccount;
    public static boolean isLogin = false;
    public static BookShopDatabase localDB;

    public SwipeRefreshLayout pageRefresher;
    public ViewPager eventViewer;
    public RecyclerView newPublishedView, bookRecommendView;
    private TextView moreNewPublished, moreBookRecommend;
    private boolean firstLoad = true;
    public Context context = this;
    private Dialog loginPopUp;
    public ImageView userPhoto;
    public TextView helloUser;

    private NavigationView navigationView;
    private View navHead;
    private Menu accountMenu;
    private MenuItem login, register, logout, userInfo;
    private FloatingActionButton shoppingCartFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        serverURL = this.getResources().getString(R.string.serverURL);
        localDB = new BookShopDatabase(this);
//        try {
//            localDB.createDataBase();
//        } catch (IOException ioe) {
//            throw new Error("Unable to create database");
//        }
//        try {
//            localDB.openDataBase();
//        }catch(SQLException sqle){
//            throw sqle;
//        }

        loginPopUp = new Dialog(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shoppingCartFab = (FloatingActionButton) findViewById(R.id.shopping_chart);
        shoppingCartFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, ShoppingCart.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navHead = navigationView.getHeaderView(0);
        userPhoto =  navHead.findViewById(R.id.user_photo);
        helloUser =  navHead.findViewById(R.id.hello_user);
        accountMenu = navigationView.getMenu();
        login = accountMenu.findItem(R.id.login);
        logout = accountMenu.findItem(R.id.logout);
        register = accountMenu.findItem(R.id.register);
        userInfo = accountMenu.findItem(R.id.user_info);
        moreNewPublished = findViewById(R.id.more_new_published);
        moreNewPublished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchNewPublished(MainPage.this);
            }
        });
        moreBookRecommend = findViewById(R.id.more_book_recommend);
        moreBookRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchBookRecommend(MainPage.this);
            }
        });

        //Part of Page Refresher (SwipeRefreshLayout)
        pageRefresher = findViewById(R.id.mainPageRefresher);
        pageRefresher.setDistanceToTriggerSync(750);
        pageRefresher.setColorScheme(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        pageRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        LoadEventPageViewer();
                        LoadNewPublishedBook();
                        LoadBookRecommend();
                    }
                });
                //pageRefresher.setRefreshing(true);    //we don't need to set it ourselves
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        pageRefresher.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        //End Part of Page Refresher (SwipeRefreshLayout)

        LoadEventPageViewer();
        LoadNewPublishedBook();
        LoadBookRecommend();
        firstLoad = false;
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
        if (firstLoad){
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
                //Toast.makeText(MainPage.this, "u", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(MainPage.this, "c", Toast.LENGTH_SHORT).show();
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

        List<String> IDList = new ArrayList<>(), imgURLList = new ArrayList<>(), nameList = new ArrayList<>(), priceList = new ArrayList<>(), authorList = new ArrayList<>();
        JSONArray newPublishData;
        try{
            //new GetBookInfoShort(imgURLList).execute().get();
            newPublishData = new GetDataJsonArray().execute("getBookInfoShort.php").get();
            for (int i =0; i < newPublishData.length();i++){
                JSONArray getData = newPublishData.getJSONArray(i);

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
            }
        }catch (Exception e){
            Log.e("XgetNewPublishedImgURL", e.getMessage());
        }

        BookInfoShortAdapter newPublishedAdapter = new BookInfoShortAdapter(context,IDList, imgURLList, nameList, priceList, authorList);
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

        List<String> IDList = new ArrayList<>(), imgURLListBR = new ArrayList<>(), nameListBR = new ArrayList<>(), priceListBR = new ArrayList<>(), authorListBR = new ArrayList<>();
        JSONArray bookRecommendData;
        try{
            //new GetBookInfoShort(imgURLList).execute().get();
            bookRecommendData = new GetDataJsonArray().execute("getBookRecommendShort.php").get();
            for (int i =0; i < bookRecommendData.length();i++){
                JSONArray getData = bookRecommendData.getJSONArray(i);

                String IDString = getData.getString(0);
                IDList.add(IDString);

                String urlString = getData.getString(1);
                urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
                imgURLListBR.add(urlString);

                String nameString = getData.getString(2);
                nameListBR.add(nameString);

                String priceString = getData.getString(3);
                priceListBR.add(priceString);

                String authorString = getData.getString(4);
                authorListBR.add(authorString);
            }
        }catch (Exception e){
            Log.e("XgetBookRecommendImgURL", e.getMessage());
        }



        BookInfoShortAdapter bookRecommendAdapter = new BookInfoShortAdapter(context,IDList, imgURLListBR, nameListBR, priceListBR, authorListBR);
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
        getMenuInflater().inflate(R.menu.nav_drawer_fake, menu);
        MenuItem searchBut = menu.findItem(R.id.searching);
        searchBut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainPage.this, SearchPage.class);
                MainPage.this.startActivity(intent);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.searching) {
            //Toast.makeText(this, "you suck",Toast.LENGTH_SHORT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //navigationview menu things
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.login) {
            SetUpLoginPopUp(loginPopUp);
        } else if (id == R.id.register) {
            StartRegisterPage();
        } else if (id == R.id.logout) {
            Logout();
        } else if (id == R.id.user_info) {
            StartUserInfoPage();
        } else if (id == R.id.browse_history) {
            if (isLogin)
                OpenBrowseHistory(MainPage.this);
            else
                SetUpLoginPopUp(loginPopUp);
        } else if (id == R.id.new_published) {
            SearchNewPublished(MainPage.this);
        } else if (id == R.id.book_recommended) {
            SearchBookRecommend(MainPage.this);
        } else if (id == R.id.closest_shop) {

        } else if (id == R.id.setting) {
            if (isLogin){
                Intent intent = new Intent(this, SettingPage.class);
                this.startActivity(intent);
            }
            else{
                SetUpLoginPopUp(loginPopUp);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void SetUpLoginPopUp(final Dialog popUp){

        final EditText accountInput, passwordInput;
        Button loginButton, registerButton;

        popUp.setContentView(R.layout.login_pop_up);

        accountInput = popUp.findViewById(R.id.account_input);
        passwordInput = popUp.findViewById(R.id.password_input);
        loginButton = popUp.findViewById(R.id.login_button);
        registerButton = popUp.findViewById(R.id.register_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountText, passwordText;
                accountText = accountInput.getText().toString();
                passwordText = passwordInput.getText().toString();
                try{
                    if ((Integer.parseInt(new CheckLogin().execute(accountText, passwordText).get())) > 0){
                        popUp.dismiss();
                        Logined(accountText);
                    }
                }catch (Exception e){
                    Log.e("checkLoginFail",e.getMessage());
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartRegisterPage();
            }
        });

        popUp.show();

    }

    public void Logined(String accountText){
        isLogin = true;
        currentAccount = accountText;
        login.setVisible(false);
        register.setVisible(false);
        logout.setVisible(true);
        userInfo.setVisible(true);

        helloUser.setText(new String("你好 "+currentAccount));
//        if (MainActivity.userPhoto != null)
//            userPhoto.setImageBitmap(MainActivity.userPhoto);
        try{
            String photoURL = new getUserPhotoURL().execute(accountText).get();
//            Glide.with(navigationView)
//                    .load(photoURL)
//                    .into(userPhoto);
            new imgdler(userPhoto).execute(photoURL).get();
        }catch (Exception e){
            Log.e("getUserPhotoFail",e.getMessage());
        }

    }

    public void Logout(){
        isLogin = false;
        login.setVisible(true);
        register.setVisible(true);
        logout.setVisible(false);
        userInfo.setVisible(false);

        userPhoto.setImageResource(R.mipmap.ic_launcher_round);
        helloUser.setText(this.getResources().getText(R.string.hi_stranger));
    }

    public void OpenBrowseHistory(Context context){
        Intent intent = new Intent(context, BrowseHistory.class);
        context.startActivity(intent);
    }

    public void SearchNewPublished(Context context){
        final Intent intent = new Intent(context, SearchPage.class);
        Bundle bundle = new Bundle();
        bundle.putString("choice", context.getResources().getString(R.string.search_date_new));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void SearchBookRecommend(Context context){
        final Intent intent = new Intent(context, SearchPage.class);
        Bundle bundle = new Bundle();
        bundle.putString("choice", context.getResources().getString(R.string.hot));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void StartRegisterPage(){
        Intent intent = new Intent(MainPage.this, RegiserPage.class);
        startActivity(intent);
    }

    public void StartUserInfoPage(){
        Intent intent = new Intent(MainPage.this, UserInfoPage.class);
        startActivity(intent);
    }

    private class CheckLogin extends AsyncTask<String,String,String>{
        private Context context = MainPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);

        String link = "checkLogin.php";
        //Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pLoading.setMessage("\tLoading...");
            pLoading.setCancelable(false);
            pLoading.show();

        }

        public CheckLogin() {
            link = serverURL+link;
        }

        @Override
        protected String doInBackground(String... strings) {
            String account = strings[0], password = strings[1];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("account", "UTF-8") + "=" +
                        URLEncoder.encode(account, "UTF-8");
                input += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                        URLEncoder.encode(password, "UTF-8");

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
                //return null;
                return new String ("exception");
            }

        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pLoading.dismiss();
            if(result.equals("-1"))
            {
                Toast.makeText(context, "帳戶或密碼錯誤", Toast.LENGTH_LONG).show();

            }else if (result.equals("exception")) {

                Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

            }else {  //bookID > 0
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                currentAccountID = result;
                Toast.makeText(context, "登入成功", Toast.LENGTH_LONG).show();

            }
        }

    }

    private class getUserPhotoURL extends AsyncTask<String,String,String>{
        String link = "getUserPhotoURL.php";
        //Context context;

        public getUserPhotoURL() {
            link = serverURL+link;
        }

        @Override
        protected String doInBackground(String... strings) {
            String account = strings[0];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("account", "UTF-8") + "=" +
                        URLEncoder.encode(account, "UTF-8");

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
                //return null;
                return new String ("exception");
            }

        }
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
}
