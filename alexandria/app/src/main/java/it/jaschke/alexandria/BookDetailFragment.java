package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.models.Book;
import it.jaschke.alexandria.services.BookService;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


public class BookDetailFragment extends Fragment {

    private View rootView;
    private Book book;
    private String authors;
    private String categories;
    private CompositeSubscription compositeSubscription;

    public BookDetailFragment(){
    }


    public static BookDetailFragment newInstance(@NonNull Book book){

        BookDetailFragment bookDetailFragment = new BookDetailFragment();

        Bundle args = new Bundle();
        args.putParcelable("book", book);

        bookDetailFragment.setArguments(args);

        return bookDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        compositeSubscription = new CompositeSubscription();

        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(!MainActivity.IS_TABLET){
            book = getActivity().getIntent().getExtras().getParcelable("book");
            loadDetails();
        }else{
            Bundle arguments = getArguments();
            if (arguments != null) {
                book = arguments.getParcelable("book");
                loadDetails();
            }
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.book_detail, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        switch (id){
            case R.id.action_delete:
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, book.getEan());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);

                Toast.makeText(getActivity(), R.string.delted_successfully, Toast.LENGTH_SHORT).show();

                if(!MainActivity.IS_TABLET) {
                    getActivity().finish();
                }else{
                    getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                }

                break;
            case R.id.action_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text)+book.getTitle());
                startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.share_book)));
                break;
        }

        return true;
    }


    @Override
    public void onPause() {
        super.onDestroyView();
        if(MainActivity.IS_TABLET && getActivity().findViewById(R.id.detail_container)!=null){
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscription.unsubscribe();
    }

    public void setBook(Book book){
        this.book = book;
        loadDetails();
    }

    private void loadDetails() {
        Observable<Void> observable = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {

                Cursor cursor = getActivity().getContentResolver().query(AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(book.getEan())), null, null, null, null);
                if(cursor.moveToFirst()) {
                    authors = cursor.getString(cursor.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));


                    categories = cursor.getString(cursor.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
                }
                cursor.close();

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });

        Subscription subscription = AppObservable.bindSupportFragment(this, observable)
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, e.getMessage());
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        bindData();
                    }
                });

        compositeSubscription.add(subscription);
    }

    private void bindData() {

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(book.getTitle());
        if(!book.getSubTitle().isEmpty()){
            titleBuilder.append(" - ");
            titleBuilder.append(book.getSubTitle());
        }
        if(!MainActivity.IS_TABLET) {
            ((CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar))
                    .setTitle(titleBuilder.toString());
        }else{
            try {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(titleBuilder.toString());
            }catch (NullPointerException e){
                Timber.e(e, e.getMessage());
            }
        }

        //((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(book.getSubTitle());

        ((TextView) rootView.findViewById(R.id.description_textview)).setText(book.getDescription());


        String[] authorsArray = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArray.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));

        if(Patterns.WEB_URL.matcher(book.getImageUrl()).matches()){
            Glide.with(this).load(book.getImageUrl()).into((ImageView) rootView.findViewById(R.id.detail_backdrop));
            rootView.findViewById(R.id.detail_backdrop).setVisibility(View.VISIBLE);
        }


        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

    }

}