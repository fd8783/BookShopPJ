package project.comp4342.bookshoppj;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fd8783 on 18/4/2018.
 */

public class BookShopDatabase extends SQLiteOpenHelper{

    private static String DBName = "bookshop_local";
    private static String shoppingCartTable = "Shopping_Cart", searchRecordTable = "Search_Record";
    private SQLiteDatabase localDB;
    private Context context;

    public BookShopDatabase(Context context) {
        super(context, DBName, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Shopping_Cart" +
                "(cartItemNo INTEGER PRIMARY KEY AUTOINCREMENT, addTime TIMESTAMP DEFAULT (DATETIME ('now', 'localtime')), bookID INT, bookImgURL VARCHAR(65536), bookName VARCHAR(1000), bookPrice FLOAT, bookAuthor VARCHAR(3000))");

        db.execSQL("CREATE TABLE Search_Record " +
                "(recordNo INTEGER PRIMARY KEY AUTOINCREMENT, seacrhContent VARCHAR(1000) NOT NULL, searchTime TIMESTAMP DEFAULT (DATETIME ('now', 'localtime')))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop TABLE IF EXISTS Shopping_Cart");
        db.execSQL("Drop TABLE IF EXISTS Search_Record");
        onCreate(db);
    }

    public Cursor GetAllShoppingCartItem(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select bookID, bookImgURL, bookName, bookPrice, bookAuthor from Shopping_Cart",null);
        return res;
    }

    public void InsertShoppingCart(int bookID, String bookImgURL, String bookName, float bookPrice, String bookAuthor){
        SQLiteDatabase insertDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("bookID", bookID);
        contentValues.put("bookImgURL", bookImgURL);
        contentValues.put("bookName", bookName);
        contentValues.put("bookPrice", bookPrice);
        contentValues.put("bookAuthor", bookAuthor);
        insertDB.insert("Shopping_Cart",null,contentValues);
        Log.e("add_toShoppingCart",Integer.toString(getShoppingCartCount()));
    }

    public int getShoppingCartCount(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor dataSet = db.rawQuery("Select * from Shopping_Cart", null);
        return dataSet.getCount();
    }

    public boolean isShoppingCartContain(String bookID){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select cartItemNo from Shopping_Cart where bookID = ?";
        Cursor dataSet = db.rawQuery(query, new String[] {bookID});
        return (dataSet.getCount() == 0) ? false : true;
    }

    public void DeleteFromShoppingCart(int bookID){
        this.getWritableDatabase().execSQL("Delete from Shopping_Cart where bookID = "+bookID);
    }


}
