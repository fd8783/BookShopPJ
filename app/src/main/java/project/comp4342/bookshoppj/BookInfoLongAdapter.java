package project.comp4342.bookshoppj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by fd8783 on 13/4/2018.
 */

public class BookInfoLongAdapter extends RecyclerView.Adapter<BookInfoLongAdapter.ViewHolder>  {

    private Context context;
    private String priceText = "", authorText = "", publisherText = "", ratingText = "";
    private List<String> IDList, imgURLList, bookNameList, bookPriceList, bookAuthorList, bookPulisherList, bookRatingList;

    public BookInfoLongAdapter(Context context,List<String> IDList, List<String> imgURLList, List<String> bookNameList, List<String> bookPriceList, List<String> bookAuthorList, List<String> bookPulisherList, List<String> bookRatingList){
        //ID List only use for pass BookID to next page(detail content page) not for display usage, therefore we don't need to define it in holder
        this.IDList = IDList;
        this.imgURLList = imgURLList;
        this.bookNameList = bookNameList;
        this.bookPriceList = bookPriceList;
        this.bookAuthorList = bookAuthorList;
        this.bookPulisherList = bookPulisherList;
        this.bookRatingList = bookRatingList;
        this.context = context;
        priceText = this.context.getResources().getString(R.string.price);
        authorText = this.context.getResources().getString(R.string.author);
        publisherText = this.context.getResources().getString(R.string.publisher);
        ratingText = this.context.getResources().getString(R.string.rating);
    }

    //set up the view
    @Override
    public BookInfoLongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_info_long, parent, false);
        BookInfoLongAdapter.ViewHolder viewHolder = new BookInfoLongAdapter.ViewHolder(view);
        return viewHolder;
    }

    //set up the value in the object(item) of view
    @Override
    public void onBindViewHolder(BookInfoLongAdapter.ViewHolder holder, final int position) {
        holder.bookName_long.setText(bookNameList.get(position));
        holder.bookPrice_long.setText(priceText + bookPriceList.get(position));
        holder.bookAuthor_long.setText(authorText + bookAuthorList.get(position));
        holder.bookPublisher_long.setText(publisherText + bookPulisherList.get(position));
        holder.bookRating_long.setText(ratingText + bookRatingList.get(position));
        try{
//            holder.bookImg_long.setImageBitmap(new ImgDownloader().execute(imgURLList.get(position)).get());
            Glide.with(holder.itemView)
                    .load(imgURLList.get(position))
                    .into(holder.bookImg_long);
        }catch (Exception e){
            Log.e("loadNewPublishedImgFail", e.getMessage());
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
        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return IDList.size();
    }


    //define ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView bookImg_long;
        public TextView bookName_long, bookPrice_long, bookAuthor_long, bookPublisher_long, bookRating_long;
        public ProgressBar progressBar;
        public ViewHolder(View itemView) {
            super(itemView);
            bookImg_long = itemView.findViewById(R.id.bookImg_long);
            bookName_long = itemView.findViewById(R.id.bookName_long);
            bookPrice_long = itemView.findViewById(R.id.bookPrice_long);
            bookAuthor_long = itemView.findViewById(R.id.bookAuthor_long);
            bookPublisher_long = itemView.findViewById(R.id.bookPublisher_long);
            bookRating_long = itemView.findViewById(R.id.bookRating_long);
            progressBar = itemView.findViewById(R.id.imgLoadingBar);
        }
    }

}
