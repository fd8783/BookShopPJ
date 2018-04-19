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
 * Created by fd8783 on 12/4/2018.
 */

public class BookInfoShortAdapter extends RecyclerView.Adapter<BookInfoShortAdapter.ViewHolder> {

    private Context context;
    private String priceText = "", authorText = "";
    private List<String> IDList, imgURLList, bookNameList, bookPriceList, bookAuthorList;

    public BookInfoShortAdapter(Context context,List<String> IDList, List<String> imgURLList, List<String> bookNameList, List<String> bookPriceList, List<String> bookAuthorList){
        //ID List only use for pass BookID to next page(detail content page) not for display usage, therefore we don't need to define it in holder
        this.IDList = IDList;
        this.imgURLList = imgURLList;
        this.bookNameList = bookNameList;
        this.bookPriceList = bookPriceList;
        this.bookAuthorList = bookAuthorList;
        this.context = context;
        priceText = this.context.getResources().getString(R.string.price);
        authorText = this.context.getResources().getString(R.string.author);
    }

    //set up the view
    @Override
    public BookInfoShortAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_info_short, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //set up the value in the object(item) of view
    @Override
    public void onBindViewHolder(BookInfoShortAdapter.ViewHolder holder, final int position) {
        holder.bookName_short.setText(bookNameList.get(position));
        holder.bookPrice_short.setText(priceText + bookPriceList.get(position));
        holder.bookAuthor_short.setText(authorText + bookAuthorList.get(position));
        try{
//            holder.bookImg_short.setImageBitmap(new ImgDownloader().execute(imgURLList.get(position)).get());
            Glide.with(holder.itemView)
                    .load(imgURLList.get(position))
                    .into(holder.bookImg_short);
        }catch (Exception e){
            Log.e("loadNewPublishedImgFail", e.getMessage());
        }
        holder.setIsRecyclable(false);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PageAdapter.eventDescriptionOpened) {
                    PageAdapter.eventDescriptionOpened = true;
                    final Intent intent = new Intent(v.getContext(), EventContext.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ImgURL", imgURLList.get(position));
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            }
        });
        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return imgURLList.size();
    }


    //define ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView bookImg_short;
        public TextView bookName_short, bookPrice_short, bookAuthor_short;
        public ProgressBar progressBar;
        public ViewHolder(View itemView) {
            super(itemView);
            bookImg_short = itemView.findViewById(R.id.bookImg_short);
            bookName_short = itemView.findViewById(R.id.bookName_short);
            bookPrice_short = itemView.findViewById(R.id.bookPrice_short);
            bookAuthor_short = itemView.findViewById(R.id.bookAuthor_short);
            progressBar = itemView.findViewById(R.id.imgLoadingBar);
        }
    }

}
