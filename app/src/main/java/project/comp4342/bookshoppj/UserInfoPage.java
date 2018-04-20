package project.comp4342.bookshoppj;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fd8783 on 16/4/2018.
 */

public class UserInfoPage extends AppCompatActivity {

    private ImageView userPhoto;
    private Bitmap cropPhoto;
    private EditText passwordInput, newPasswordInput, confirmPasswordInput, contactInput, creditCardInput, addressInput;
    private String passwordText, oldPasswordText, newPasswordText, confirmPasswordText, contactText, oldContactText, creditCardText, oldCreditCardText, addressText, oldAddressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_page);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPhoto = findViewById(R.id.click_to_add_photo);
        passwordInput = findViewById(R.id.password_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        contactInput = findViewById(R.id.contact_input);
        creditCardInput = findViewById(R.id.credit_card_input);
        addressInput = findViewById(R.id.address_input);

        ConstraintLayout addPhotoLayout = findViewById(R.id.add_photo_layout);
        addPhotoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, 1);
            }
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleInputData();
            }
        });

        LoadUserInfo();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //get image from user gallery
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                //crop image to square
                int height = selectedImage.getHeight(), width = selectedImage.getWidth();
                int newWidth = (height > width) ? width : height;
                int newHeight = (height > width)? height - ( height - width) : height;
                int cropW = (width - height) / 2;
                cropW = (cropW < 0)? 0: cropW;
                int cropH = (height - width) / 2;
                cropH = (cropH < 0)? 0: cropH;
                cropPhoto = Bitmap.createBitmap(selectedImage, cropW, cropH, newWidth, newHeight);

                userPhoto.setImageBitmap(cropPhoto);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(UserInfoPage.this, "Error", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(UserInfoPage.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void HandleInputData(){
        boolean canFinish = true;

        passwordText = passwordInput.getText().toString();
        newPasswordText = newPasswordInput.getText().toString();
        confirmPasswordText = confirmPasswordInput.getText().toString();
        contactText = contactInput.getText().toString();
        creditCardText = creditCardInput.getText().toString();
        addressText = addressInput.getText().toString();

        if (!passwordText.equals(oldPasswordText)){
            Toast.makeText(UserInfoPage.this, "密碼錯誤", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPasswordText.equals("")){
            if (!newPasswordText.equals(confirmPasswordText)) {
                Toast.makeText(UserInfoPage.this, "兩次輸入密碼不一樣", Toast.LENGTH_LONG).show();
                canFinish = false;
            }
            else{
                //update password
                new UpdatePassword(false).execute("updatePassword.php",MainPage.currentAccountID,newPasswordText);
                Toast.makeText(UserInfoPage.this, "密碼已更新", Toast.LENGTH_LONG).show();
            }
        }

        if (cropPhoto != null){
            new UploadImage(false).execute(MainPage.currentAccount);
            Toast.makeText(UserInfoPage.this, "照片已更新", Toast.LENGTH_LONG).show();
        }

        if (!contactText.equals(oldContactText)){
            if (contactText.length() == 8){
                new UpdateContactNo(false).execute("updateContactNo.php",MainPage.currentAccountID,contactText);
                Toast.makeText(UserInfoPage.this, "電話號碼已更新", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(UserInfoPage.this, "請輸入8位電話號碼", Toast.LENGTH_LONG).show();
                canFinish =false;
            }
        }

        if (!creditCardText.equals(oldCreditCardText)){
            if (creditCardText.length() == 16){
                new UpdateCreditCardNo(false).execute("updateCreditCardNo.php",MainPage.currentAccountID,creditCardText);
                Toast.makeText(UserInfoPage.this, "信用卡號碼已更新", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(UserInfoPage.this, "請輸入16位信用卡號碼", Toast.LENGTH_LONG).show();
                canFinish =false;
            }
        }

        if (!addressText.equals(oldAddressText)){
            new UpdateAddress(false).execute("updateAddress.php", MainPage.currentAccountID,addressText);
            Toast.makeText(UserInfoPage.this, "地址已更新", Toast.LENGTH_LONG).show();
        }

        if (canFinish)
            finish();
    }

    public void LoadUserInfo(){

        JSONArray userInfoData;
        try{
            //new GetBookInfoShort(imgURLList).execute().get();
            userInfoData = new GetDataJsonArrayByUserID().execute("getUserInfo.php", MainPage.currentAccountID).get();
            JSONArray getData = userInfoData.getJSONArray(0);

            oldPasswordText = getData.getString(0);

            String urlString = getData.getString(1);
            urlString = urlString.replace("\\",""); //clear the [", "] and \ (note "\\" means "\")
//            Glide.with(UserInfoPage.this)
 //                   .load(urlString)
 //                   .into(userPhoto);
            new imgdler(userPhoto).execute(urlString).get();

            oldContactText = getData.getString(2);
            contactInput.setText(oldContactText);

            oldAddressText = getData.getString(3);
            addressInput.setText(oldAddressText);

            oldCreditCardText = getData.getString(4);
            creditCardInput.setText(oldCreditCardText);

        }catch (Exception e){
            Log.e("XgetUserPhotoImgURL", e.getMessage());
        }
    }

    private class GetDataJsonArrayByUserID extends AsyncTask<String, String, JSONArray> {

        private JSONArray jsonArray = new JSONArray();

        String link = MainPage.serverURL;

        @Override
        protected JSONArray doInBackground(String... strings) {
            try{
                link = link+strings[0];
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(MainPage.currentAccountID, "UTF-8");

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
                        //Log.e("tgfjdkgjdfkl",urlList.get(0));
                    }
                }
                return jsonArray;
            }catch (Exception e){
                return null;
                //return new String ("Excepiton:"+e.getMessage());
            }
        }
    }

    private class UploadImage extends AsyncTask<String,String,String>{
        private Context context = UserInfoPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link = "uploadImage.php";
        //Context context;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            if (showToast){
                pLoading.setMessage("\tuploading photo...");
                pLoading.setCancelable(false);
                pLoading.show();
            }
        }

        public UploadImage(boolean showToast) {
            link = MainPage.serverURL+link;
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            String account = strings[0];
            String imgBitmap = BitMapToString(cropPhoto);

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("account", "UTF-8") + "=" +
                        URLEncoder.encode(account, "UTF-8");
                input += "&" + URLEncoder.encode("imageBitmap", "UTF-8") + "=" +
                        URLEncoder.encode(imgBitmap, "UTF-8");

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
            if (showToast){
                pLoading.dismiss();
                if (result.equals("exception")) {
                    Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public class UpdateCreditCardNo extends AsyncTask<String,String,String>{
        private Context context = UserInfoPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;
        private String link;
        //Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            if (showToast){
                pLoading.setMessage("\tadding credit card no...");
                pLoading.setCancelable(false);
                pLoading.show();
            }
        }

        public UpdateCreditCardNo(boolean showToast) {
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            link = MainPage.serverURL+strings[0];
            String userID = strings[1];
            String creditCardNo = strings[2];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
                input += "&" + URLEncoder.encode("creditCardNo", "UTF-8") + "=" +
                        URLEncoder.encode(creditCardNo, "UTF-8");

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
                Log.e("UpdateCreditCardError",e.getMessage());
                //return null;
                return new String ("exception");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            if (showToast){
                pLoading.dismiss();
                if (result.equals("exception")) {
                    Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class UpdateAddress extends AsyncTask<String,String,String>{
        private Context context = UserInfoPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link;
        //Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            if (showToast){
                pLoading.setMessage("\tadding address...");
                pLoading.setCancelable(false);
                pLoading.show();
            }
        }

        public UpdateAddress(boolean showToast) {
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            link = MainPage.serverURL+strings[0];
            String userID = strings[1];
            String address = strings[2];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
                input += "&" + URLEncoder.encode("address", "UTF-8") + "=" +
                        URLEncoder.encode(address, "UTF-8");

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
                Log.e("UpdateAddressError",e.getMessage());
                //return null;
                return new String ("exception");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            if (showToast){
                pLoading.dismiss();
                if (result.equals("exception")) {
                    Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class UpdatePassword extends AsyncTask<String,String,String>{
        private Context context = UserInfoPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link;
        //Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            if (showToast){
                pLoading.setMessage("\tchanging password...");
                pLoading.setCancelable(false);
                pLoading.show();
            }
        }

        public UpdatePassword(boolean showToast) {
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            link = MainPage.serverURL+strings[0];
            String userID = strings[1];
            String password = strings[2];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
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
                Log.e("UpdatePasswordError",e.getMessage());
                //return null;
                return new String ("exception");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            if (showToast){
                pLoading.dismiss();
                if (result.equals("exception")) {
                    Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class UpdateContactNo extends AsyncTask<String,String,String>{
        private Context context = UserInfoPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link;
        //Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            if (showToast){
                pLoading.setMessage("\tchanging contact No...");
                pLoading.setCancelable(false);
                pLoading.show();
            }
        }

        public UpdateContactNo(boolean showToast) {
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            link = MainPage.serverURL+strings[0];
            String userID = strings[1];
            String contactNo = strings[2];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("userID", "UTF-8") + "=" +
                        URLEncoder.encode(userID, "UTF-8");
                input += "&" + URLEncoder.encode("contactNo", "UTF-8") + "=" +
                        URLEncoder.encode(contactNo, "UTF-8");

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
                Log.e("UpdateContactNoError",e.getMessage());
                //return null;
                return new String ("exception");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            if (showToast){
                pLoading.dismiss();
                if (result.equals("exception")) {
                    Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                }
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
