package com.treehouse.android.rxjavaworkshop;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String LIST = "list";
    private static final String FILTER = "filter";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.spinner)
    Spinner spinner;
    @Bind(R.id.add_todo_input)
    EditText addInput;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerView;

    TodoAdapter adapter;

    TodoListFilter filter;

    CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"All", "Incomplete", "Completed"}));

        if (savedInstanceState == null) {
            TodoList l = new TodoList();
            l.add(new Todo("Sample 1", false));
            l.add(new Todo("Sample 2", true));
            l.add(new Todo("Sample 3", false));

            filter = new TodoListFilter(l, TodoListFilter.ALL);
        } else {
            filter = new TodoListFilter(
                    new TodoList(savedInstanceState.getString(LIST)),
                    savedInstanceState.getInt(FILTER)
            );
        }


        adapter = new TodoAdapter(this, filter);

        // setup the list with the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        subscriptions.add(filter.asObservable()
                .subscribe(adapter));
        subscriptions.add(
                RxView.clicks(findViewById(R.id.btn_add_todo))
                        .map(new Func1<Void, String>() {
                            @Override
                            public String call(Void aVoid) {
                                return addInput.getText().toString();
                            }
                        })
                        .filter(new Func1<String, Boolean>() {
                            @Override
                            public Boolean call(String s) {
                                return !TextUtils.isEmpty(s);
                            }
                        })
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                filter.getList().add(new Todo(s, false));

                                // reset the input box, move focus, and dismiss keyboard
                                addInput.setText("");
                                findViewById(R.id.add_todo_container).requestFocus();
                                dismissKeyboard();
                            }
                        }));


        subscriptions.add(
                RxAdapterView.itemSelections(spinner)
                        .skip(1)
                        .subscribe(filter.asFilterSubscriber()));

        spinner.setSelection(filter.getMode());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST, filter.getList().toString());
        outState.putInt(FILTER, filter.getMode());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addInput.getWindowToken(), 0);
    }

}
