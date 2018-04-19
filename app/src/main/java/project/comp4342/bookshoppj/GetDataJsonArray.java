package project.comp4342.bookshoppj;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fd8783 on 12/4/2018.
 */

public class GetDataJsonArray extends AsyncTask<String, String, JSONArray> {

    private JSONArray jsonArray = new JSONArray();

    String link = MainPage.serverURL;

//    public GetDataJsonArray(JSONArray jsonArray) {
//        this.jsonArray = jsonArray;
//    }

    @Override
    protected JSONArray doInBackground(String... strings) {
        try{
            link = link+strings[0];
            URL url = new URL(link);
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //StringBuffer sb = new StringBuffer();
            String data;
            JSONObject dataJson;
            while ((data = reader.readLine()) != null){
                if (data.charAt(0) == '['){
                    //it means it is the json data, not <html>/<body>...
                    jsonArray = new JSONArray(data);
                    break;
                    //Log.e("tgfjdkgjdfkl",urlList.get(0));
                }
            }
            return jsonArray;
        }catch (Exception e){
            return null;
            //return new String ("Excepiton:"+e.getMessage());
        }
    }
//    protected void onPostExecute(JSONArray result){
//        this.jsonArray = result;
//    }
}
