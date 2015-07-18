package it.jaschke.alexandria.api;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import it.jaschke.alexandria.BookDetailActivity;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.models.Book;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookHolder> {

    private Context context;
    private final ArrayList<Book> objects;

    public BookListAdapter(Context context, ArrayList<Book> objects){
        this.context = context;
        this.objects = objects;
    }


    @Override
    public BookHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_list, viewGroup, false);
        return new BookHolder(view);
    }

    @Override
    public void onBindViewHolder(BookHolder bookHolder, int position) {
        final Book book = objects.get(position);

        bookHolder.bookTitle.setText(book.getTitle());
        bookHolder.bookSubTitle.setText(book.getSubTitle());

        bookHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!MainActivity.IS_TABLET) {
                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra("book", book);
                    context.startActivity(intent);
                }else{
                    MainActivity activity = (MainActivity) context;
                    activity.openBookDetails(book);
                }
            }
        });

        Glide.with(context).load(book.getImageUrl()).into(bookHolder.bookCover);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public class BookHolder extends RecyclerView.ViewHolder{

        public View root;
        public  ImageView bookCover;
        public  TextView bookTitle;
        public  TextView bookSubTitle;

        public BookHolder(View itemView) {
            super(itemView);

            root = itemView;
            bookCover = (ImageView) itemView.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) itemView.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) itemView.findViewById(R.id.listBookSubTitle);
        }
    }


}
