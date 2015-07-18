package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ISBNResultParser;
import com.google.zxing.client.result.ParsedResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.squareup.otto.Subscribe;

import java.util.List;

import it.jaschke.alexandria.events.FetchEvent;
import it.jaschke.alexandria.models.Book;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.utils.BusProvider;


public class MainActivity extends AppCompatActivity {


    private static final String EXTRA_IS_SCANNING = "EXTRA_IS_SCANNING";
    public static boolean IS_TABLET = false;
    private static boolean isScanning = false;
    private BroadcastReceiver messageReceiver;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    private final int LOADER_ID = 1;

    private FloatingActionButton addBookButton;

    private View scanLayout;
    private CompoundBarcodeView barcodeScannerView;
    private EditText codeEditText;
    private ProgressBar scanProgressView;

    public ListOfBooksFragment bookListFragment;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                ParsedResult resultParser = ISBNResultParser.parseResult(result.getResult());
                codeEditText.setText(resultParser.getDisplayResult());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main);
        }else {
            setContentView(R.layout.activity_main);
        }

        getSupportActionBar().setElevation(0);
        messageReceiver = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,filter);

        addBookButton = (FloatingActionButton) findViewById(R.id.main_add_book_button);

        bookListFragment = (ListOfBooksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_list_of_books);


        setUpAddButton();
    }

    private void setUpAddButton() {
        scanLayout= findViewById(R.id.scan_layout);
        codeEditText = (EditText) findViewById(R.id.add_code_edit_text);
        barcodeScannerView = (CompoundBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        scanProgressView = (ProgressBar) scanLayout.findViewById(R.id.add_code_progressview);

        barcodeScannerView.decodeSingle(callback);

        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith("978")){
                    ean="978"+ean;
                }
                if(ean.length()<13){
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(MainActivity.this, BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                startService(bookIntent);

                scanProgressView.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);

        if(scanLayout.getVisibility() == View.VISIBLE){
            barcodeScannerView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        BusProvider.getInstance().unregister(this);

        //We close the camera before we try to re open it on configuration change. Otherwise we get
        //an exeception trying to recover the Camera service.
        if(isScanning){
            scanLayout.setVisibility(View.GONE);
            barcodeScannerView.pause();
            addBookButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_content_add));
            getSupportActionBar().setTitle(R.string.app_title);
        }

    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    public void openBookDetails(Book book) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, BookDetailFragment.newInstance(book))
                .commit();

        findViewById(R.id.detail_container).setVisibility(View.VISIBLE);
    }


    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
                scanProgressView.setVisibility(View.GONE);
            }
        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE && getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putBoolean(EXTRA_IS_SCANNING, isScanning);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isScanning = savedInstanceState.getBoolean(EXTRA_IS_SCANNING);

        if(isScanning){
            scanLayout.setVisibility(View.VISIBLE);
            barcodeScannerView.resume();
            addBookButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_done));
            getSupportActionBar().setTitle(R.string.add_book_title);
        }
    }

    public void addBookOnClick(View view){
        if(!isScanning) {
            isScanning = true;
            scanLayout.setVisibility(View.VISIBLE);
            barcodeScannerView.resume();
            addBookButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_done));
            getSupportActionBar().setTitle(R.string.add_book_title);
        }else{
            isScanning = false;
            scanLayout.setVisibility(View.GONE);
            barcodeScannerView.pause();
            addBookButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_content_add));
            getSupportActionBar().setTitle(R.string.app_title);
        }
    }


    @Subscribe
    public void FetchSubscriber(FetchEvent event){
        scanCallback(event);
    }

    private void scanCallback(FetchEvent event) {
        scanProgressView.setVisibility(View.GONE);

        switch (event.getResultCode()){
            case FetchEvent.FETCH_ERROR:
                Toast.makeText(getApplicationContext(), R.string.no_book_found, Toast.LENGTH_SHORT).show();
                return;
            case FetchEvent.FETCH_ALREADY_EXISTS:
                Toast.makeText(getApplicationContext(), R.string.dupe_error, Toast.LENGTH_SHORT).show();
                return;
            case FetchEvent.FETCH_INVALID_ISBN:
                Toast.makeText(getApplicationContext(), R.string.invalid_isbn_error, Toast.LENGTH_SHORT).show();
                return;
            default:
                String bookTitle = event.getTitle();
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.was_added_to_library), bookTitle), Toast.LENGTH_SHORT).show();

                isScanning = false;
                codeEditText.setText("");
                scanLayout.setVisibility(View.GONE);
                barcodeScannerView.pause();
                addBookButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_content_add));

                bookListFragment.loadBooks(null);
        }


    }

}