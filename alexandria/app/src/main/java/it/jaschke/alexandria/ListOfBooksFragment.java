package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.events.AndroidBus;
import it.jaschke.alexandria.events.BookDeletedEvent;
import it.jaschke.alexandria.models.Book;
import it.jaschke.alexandria.utils.BusProvider;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class ListOfBooksFragment extends Fragment {

    private BookListAdapter bookListAdapter;
    private RecyclerView bookList;
    private ArrayList<Book> bookObjects = new ArrayList<>();
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;
    private CompositeSubscription compositeSubscription;

    private final int LOADER_ID = 10;

    private final TextWatcher queryWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length() == 0 ){
                loadBooks(null);
            }else{
                loadBooks(s.toString());
            }

        }
    };

    public ListOfBooksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        bookListAdapter = new BookListAdapter(getActivity(), bookObjects);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        bookList = (RecyclerView) rootView.findViewById(R.id.listOfBooks);
        if(!MainActivity.IS_TABLET) {
            bookList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        }else{
            bookList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        }
        bookList.setAdapter(bookListAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        searchText = (EditText) getActivity().findViewById(R.id.searchText);
        searchText.addTextChangedListener(queryWatcher);

    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);

        loadBooks(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getInstance().unregister(this);

    }

    @Subscribe
    public void deleteBook(BookDeletedEvent event){
        loadBooks(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscription.unsubscribe();
    }

    public void loadBooks(@Nullable final String query){

        Observable<ArrayList<Book>> bookObservable = Observable.create(new Observable.OnSubscribe<ArrayList<Book>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Book>> subscriber) {
                ArrayList<Book> results = new ArrayList<>();

                final String selection = query != null ? AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? " : null;
                String[] queryArgs = query != null ? new String[]{"%"+query+"%", "%"+query+"%"} : null;

                Cursor cursor = getActivity().getContentResolver().query(
                        AlexandriaContract.BookEntry.CONTENT_URI,
                        null,
                        selection,
                        queryArgs,
                        null
                );

                for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                    String ean = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
                    String title = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                    String subTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
                    String description = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.DESC));
                    String imageUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

                    Book book = new Book(ean, title, subTitle, description, imageUrl);

                    results.add(book);
                }

                subscriber.onNext(results);
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io());

        Subscription subscription = AppObservable.bindSupportFragment(this, bookObservable)
                .subscribe(new Subscriber<ArrayList<Book>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ArrayList<Book> books) {
                        bookObjects.clear();
                        bookObjects.addAll(books);
                        bookListAdapter.notifyDataSetChanged();
                    }
                });

        compositeSubscription.add(subscription);
    }
}
