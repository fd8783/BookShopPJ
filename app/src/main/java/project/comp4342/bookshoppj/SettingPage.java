package project.comp4342.bookshoppj;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by fd8783 on 14/4/2018.
 */

public class SettingPage extends AppCompatActivity {

    public static boolean isBrowseHistorySaved = true;

    private Dialog confirmPopUp;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        confirmPopUp = new Dialog(this);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Switch saveHistorySwitch = findViewById(R.id.save_history_switch);
//        saveHistorySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                isBrowseHistorySaved = isChecked;
//            }
//        });
        ConstraintLayout saveHistoryLayout = findViewById(R.id.save_history_layout);
        saveHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBrowseHistorySaved = !isBrowseHistorySaved;
                saveHistorySwitch.setChecked(isBrowseHistorySaved);
            }
        });

        ConstraintLayout clearHistoryLayout = findViewById(R.id.clear_history_layout);
        clearHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowConfirmPopUp();
            }
        });

        saveHistorySwitch.setChecked(isBrowseHistorySaved);
    }

    public void ShowConfirmPopUp(){
        confirmPopUp.setContentView(R.layout.confirm_pop_up);
        confirmButton = confirmPopUp.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new clearBrowseHistory().execute("clearBrowseHistory.php");
            }
        });
        confirmPopUp.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class clearBrowseHistory extends AsyncTask<String,String,String> {
        private Context context = SettingPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        String link;
        //Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pLoading.setMessage("\tclearing...");
            pLoading.setCancelable(false);
            pLoading.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            link = MainPage.serverURL+strings[0];
            String userID = MainPage.currentAccountID;

            try{
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
                Log.e("ClearHistoryError",e.getMessage());
                //return null;
                return new String ("exception");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            pLoading.dismiss();
            if (result.equals("exception")) {
                Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(context, "Browse History Clear", Toast.LENGTH_LONG).show();
            }
        }
    }
}
