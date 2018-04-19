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
                "(cartItemNo INTEGER PRIMARY KEY AUTOINCREMENT, bookID INT, amount INT DEFAULT 1, addTime TIMESTAMP DEFAULT (DATETIME ('now', 'localtime')))");

        db.execSQL("CREATE TABLE Search_Record " +
                "(recordNo INTEGER PRIMARY KEY AUTOINCREMENT, seacrhContent VARCHAR(1000) NOT NULL, searchTime TIMESTAMP DEFAULT (DATETIME ('now', 'localtime')))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop TABLE IF EXISTS Shopping_Cart");
        db.execSQL("Drop TABLE IF EXISTS Search_Record");
        onCreate(db);
    }

    public void InsertShoppingCart(int bookID){
        SQLiteDatabase insertDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("bookID", bookID);
        insertDB.insert("Shopping_Cart",null,contentValues);
    }

    public int getShoppingCartCount(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor dataSet = db.rawQuery("Select * from Shopping_Cart", null);
        return dataSet.getCount();
    }

    public void DeleteFromShoppingCart(int bookID){
        this.getWritableDatabase().execSQL("Delete from Shopping_Cart where bookID = "+bookID);
    }

}
