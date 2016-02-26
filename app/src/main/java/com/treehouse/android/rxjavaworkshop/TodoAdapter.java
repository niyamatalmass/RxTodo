package com.treehouse.android.rxjavaworkshop;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.jakewharton.rxbinding.widget.RxCompoundButton;

import java.util.Collections;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoHolder> implements Action1<List<Todo>> {

    LayoutInflater inflater;

    List<Todo> data = Collections.emptyList();

    // the Action to get called for onNext() of the check changed Subscription
    Action1<Todo> subscriber;

    public TodoAdapter(Activity activity, Action1<Todo> checkChangedSubscriber) {
        inflater = LayoutInflater.from(activity);
        subscriber = checkChangedSubscriber;
    }

    @Override
    public TodoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoHolder(inflater.inflate(R.layout.item_todo, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(TodoHolder holder, int position) {
        final Todo todo = data.get(position);
        holder.checkbox.setText(todo.description);
        holder.checkbox.setChecked(todo.isCompleted);

        /* Subscribe to the changes of the CheckBox. We skip the first one because it gets
            called with the initial value, we only want to take action on changes.
         */
        holder.subscription = RxCompoundButton.checkedChanges(holder.checkbox)
                .skip(1)
                .map(new Func1<Boolean, Todo>() {
                    @Override
                    public Todo call(Boolean aBoolean) {
                        // the list wants to know what item changed, so map this into the Todo item
                        return todo;
                    }
                })
                .subscribe(subscriber); // subscribe with each bind
    }

    @Override
    public void onViewDetachedFromWindow(TodoHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // unsubscribe if we are being removed
        holder.subscription.unsubscribe();
    }

    @Override
    public void call(List<Todo> list) {
        data = list;
        notifyDataSetChanged();
    }

    public class TodoHolder extends RecyclerView.ViewHolder {

        public CheckBox checkbox;
        public Subscription subscription;

        public TodoHolder(View itemView) {
            super(itemView);
            checkbox = (CheckBox) itemView;
        }
    }
}
