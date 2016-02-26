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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String LIST = "list";
    private static final String FILTER = "filter";

    TodoList list;
    int filterPosition;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.spinner)
    Spinner spinner;
    @Bind(R.id.add_todo_input)
    EditText addInput;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerView;

    // used to handle unsubscription during teardown of Activity
    CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // retrieve the saved list and filter or use defaults
        if (savedInstanceState == null) {
            list = new TodoList();

            // add some sample items
            list.add(new Todo("Sample 1", true));
            list.add(new Todo("Sample 2", false));
            list.add(new Todo("Sample 3", false));

            filterPosition = FilterPositions.ALL;
        } else {
            list = new TodoList(savedInstanceState.getString(LIST));
            filterPosition = savedInstanceState.getInt(FILTER);
        }

        /*
            The adapter listens to changes from the list Observable.
            The list listens to changes from the Adapter.
         */
        TodoAdapter adapter = new TodoAdapter(this, list);

        // setup the list with the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        // combine filter and todolist
        subscriptions.add(
                Observable.combineLatest(
                        RxAdapterView.itemSelections(spinner).skip(1),
                        list.asObservable(),
                        new Func2<Integer, TodoList, List<Todo>>() {
                            @Override
                            public List<Todo> call(Integer integer, TodoList list) {
                                switch (integer) {
                                    case FilterPositions.INCOMPLETE:
                                        return list.getIncomplete();
                                    case FilterPositions.COMPLETE:
                                        return list.getComplete();
                                    default:
                                        return list.getAll();
                                }
                            }
                        }
                ).subscribe(adapter)
        );

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
                                list.add(new Todo(s, false));

                                // reset the input box, move focus, and dismiss keyboard
                                addInput.setText("");
                                findViewById(R.id.add_todo_container).requestFocus();
                                dismissKeyboard();
                            }
                        }));


        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"All", "Incomplete", "Completed"}));
        spinner.setSelection(filterPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST, list.toString());
        outState.putInt(FILTER, spinner.getSelectedItemPosition());
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
