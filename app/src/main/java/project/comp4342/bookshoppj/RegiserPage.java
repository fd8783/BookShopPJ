package project.comp4342.bookshoppj;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Output;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.net.ssl.HttpsURLConnection;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by fd8783 on 16/4/2018.
 */

public class RegiserPage extends AppCompatActivity {

    private ImageView userPhoto;
    private Bitmap cropPhoto;
    private EditText accountInput, passwordInput, confirmPasswordInput, contactInput, creditCardInput, addressInput;
    private String accountText, passwordText, confirmPasswordText, contactText, creditCardText, addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPhoto = findViewById(R.id.click_to_add_photo);
        accountInput = findViewById(R.id.account_input);
        passwordInput = findViewById(R.id.password_input);
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

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleInputData();
            }
        });

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
                Toast.makeText(RegiserPage.this, "Error", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(RegiserPage.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void HandleInputData(){

        accountText = accountInput.getText().toString();
        passwordText = passwordInput.getText().toString();
        confirmPasswordText = confirmPasswordInput.getText().toString();
        contactText = contactInput.getText().toString();
        creditCardText = creditCardInput.getText().toString();
        addressText = addressInput.getText().toString();

        if (accountText.equals("")){
            Toast.makeText(RegiserPage.this, "請輸入帳號", Toast.LENGTH_LONG).show();
            return;
        } else if (accountText.length() < 6){
            Toast.makeText(RegiserPage.this, "帳號長度須為6-20位", Toast.LENGTH_LONG).show();
            return;
        } else if (passwordText.equals("")){
            Toast.makeText(RegiserPage.this, "請輸入密碼", Toast.LENGTH_LONG).show();
            return;
        } else if (passwordText.length() < 6){
            Toast.makeText(RegiserPage.this, "密碼長度須至少6位", Toast.LENGTH_LONG).show();
            return;
        } else if (!passwordText.equals(confirmPasswordText)){
            Toast.makeText(RegiserPage.this, "兩次輸入密碼不一樣", Toast.LENGTH_LONG).show();
            return;
        } else if (contactText.equals("")){
            Toast.makeText(RegiserPage.this, "請輸入電話號碼", Toast.LENGTH_LONG).show();
            return;
        } else if (contactText.length() < 8){
            Toast.makeText(RegiserPage.this, "請輸入8位電話號碼", Toast.LENGTH_LONG).show();
            return;
        }

        try{
            if (new Register(true).execute(accountText,passwordText,contactText).get().equals("account exist")){
                return;
            }
        }
        catch (Exception e){
            Log.e("register error",e.getMessage());
        }


        if (cropPhoto != null){
            new UploadImage(false).execute(accountText);
        }

        if (!creditCardText.equals("")){
            if (creditCardText.length() == 16){
                new addCreditCardNo(false).execute(accountText,creditCardText);
            }
            else{
                Toast.makeText(RegiserPage.this, "請輸入16位信用卡號碼", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (!addressText.equals("")){
            new addAddress(false).execute(accountText,addressText);
        }

        finish();

    }

    private class Register extends AsyncTask<String,String,String>{
        private Context context = RegiserPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link = "register.php";
        //Context context;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            if (showToast){
                pLoading.setMessage("\tregistering...");
                pLoading.setCancelable(false);
                pLoading.show();
            }
        }

        public Register(boolean showToast) {
            link = MainPage.serverURL+link;
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            String account = strings[0], password = strings[1], contactNo = strings[2];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("account", "UTF-8") + "=" +
                        URLEncoder.encode(account, "UTF-8");
                input += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                        URLEncoder.encode(password, "UTF-8");
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
                Log.e("getDatabaseContentError",e.getMessage());
                //return null;
                return new String ("exception");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            if (showToast)
                pLoading.dismiss();
            if (result.equals("exception")) {
                Toast.makeText(context, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UploadImage extends AsyncTask<String,String,String>{
        private Context context = RegiserPage.this;
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

    public class addCreditCardNo extends AsyncTask<String,String,String>{
        private Context context = RegiserPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link = "addCreditCard.php";
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

        public addCreditCardNo(boolean showToast) {
            link = MainPage.serverURL+link;
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            String account = strings[0];
            String creditCardNo = strings[1];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("account", "UTF-8") + "=" +
                        URLEncoder.encode(account, "UTF-8");
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
                Log.e("InsertContentError",e.getMessage());
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

    public class addAddress extends AsyncTask<String,String,String>{
        private Context context = RegiserPage.this;
        private ProgressDialog pLoading = new ProgressDialog(context);
        private boolean showToast;

        String link = "addAddress.php";
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

        public addAddress(boolean showToast) {
            link = MainPage.serverURL+link;
            this.showToast = showToast;
        }

        @Override
        protected String doInBackground(String... strings) {
            String account = strings[0];
            String address = strings[1];

            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                String input  = URLEncoder.encode("account", "UTF-8") + "=" +
                        URLEncoder.encode(account, "UTF-8");
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
                Log.e("InsertContentError",e.getMessage());
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
}
