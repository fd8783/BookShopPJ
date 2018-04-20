package project.comp4342.bookshoppj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fd8783 on 12/4/2018.
 */

public class BookInfoCartAdapter extends RecyclerView.Adapter<BookInfoCartAdapter.ViewHolder> {

    private Context context;
    private String priceText = "", authorText = "", stockText = "", smallCountTextString = "";
    private List<String> IDList, imgURLList, bookNameList, bookPriceList, bookAuthorList, bookStockList;
    private float price;

    public BookInfoCartAdapter(Context context, List<String> IDList, List<String> imgURLList, List<String> bookNameList, List<String> bookPriceList, List<String> bookAuthorList, List<String> bookStockList){
        //ID List only use for pass BookID to next page(detail content page) not for display usage, therefore we don't need to define it in holder
        this.IDList = IDList;
        this.imgURLList = imgURLList;
        this.bookNameList = bookNameList;
        this.bookPriceList = bookPriceList;
        this.bookAuthorList = bookAuthorList;
        this.bookStockList = bookStockList;
        this.context = context;
        priceText = this.context.getResources().getString(R.string.price);
        authorText = this.context.getResources().getString(R.string.author);
        stockText = context.getResources().getString(R.string.stock);
        smallCountTextString = context.getResources().getString(R.string.small_count);

        ShoppingCart.amountList = new ArrayList<>();
    }

    //set up the view
    @Override
    public BookInfoCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_info_cart, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //set up the value in the object(item) of view
    @Override
    public void onBindViewHolder(final BookInfoCartAdapter.ViewHolder holder, final int position) {
        holder.bookName_short.setText(bookNameList.get(position));
        holder.bookPrice_short.setText(priceText + bookPriceList.get(position));
        holder.bookAuthor_short.setText(authorText + bookAuthorList.get(position));
        holder.bookStock_short.setText(stockText + bookStockList.get(position));

        ShoppingCart.amountList.add("1");
        try{
//            holder.bookImg_short.setImageBitmap(new ImgDownloader().execute(imgURLList.get(position)).get());
            Glide.with(holder.itemView)
                    .load(imgURLList.get(position))
                    .into(holder.bookImg_short);
        }catch (Exception e){
            Log.e("loadCartBookImgFail", e.getMessage());
        }
        holder.setIsRecyclable(false);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PageAdapter.eventDescriptionOpened) {
                    PageAdapter.eventDescriptionOpened = true;
                    final Intent intent = new Intent(v.getContext(), BookContext.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("bookID", IDList.get(position));
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            }
        });

        holder.deleteImag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = Float.valueOf(bookPriceList.get(position))*Integer.parseInt(holder.amountInput.getText().toString());
                ShoppingCart.total_price-=price;
                ShoppingCart.UpdateTotalPrice(false);
                MainPage.localDB.DeleteFromShoppingCart(Integer.parseInt(IDList.get(position)));
                //removeAt(position);
            }
        });

        price = Float.valueOf(bookPriceList.get(position))*Integer.parseInt(holder.amountInput.getText().toString());
        ShoppingCart.total_price+=price;
        ShoppingCart.UpdateTotalPrice();
        holder.smallCountText.setText(smallCountTextString + Float.toString(price));
        holder.amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ShoppingCart.total_price-=price;
                if (s.length() == 0){
                    price = 0;
                    ShoppingCart.amountList.set(position, new String("0"));
                }
                else{
                    price = Float.valueOf(bookPriceList.get(position))*Integer.parseInt(s.toString());
                    ShoppingCart.amountList.set(position, s.toString());
                }

                ShoppingCart.total_price+=price;
                holder.smallCountText.setText(smallCountTextString + Float.toString(price));
                ShoppingCart.UpdateTotalPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return IDList.size();
    }


    //define ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView bookImg_short, deleteImag;
        public TextView bookName_short, bookPrice_short, bookAuthor_short, bookStock_short, smallCountText;
        public EditText amountInput;
        public ProgressBar progressBar;
        public ViewHolder(View itemView) {
            super(itemView);
            bookImg_short = itemView.findViewById(R.id.bookImg_short);
            bookName_short = itemView.findViewById(R.id.bookName_short);
            bookPrice_short = itemView.findViewById(R.id.bookPrice_short);
            bookAuthor_short = itemView.findViewById(R.id.bookAuthor_short);
            bookStock_short = itemView.findViewById(R.id.stock_text);
            progressBar = itemView.findViewById(R.id.imgLoadingBar);
            amountInput = itemView.findViewById(R.id.amount_input);
            smallCountText = itemView.findViewById(R.id.small_count_text);
            deleteImag = itemView.findViewById(R.id.delete_button);
        }
    }

    public void removeAt(int position) {
        IDList.remove(position);
        imgURLList.remove(position); bookNameList.remove(position); bookPriceList.remove(position); bookAuthorList.remove(position); bookStockList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, IDList.size());
        notifyDataSetChanged();
    }
}
