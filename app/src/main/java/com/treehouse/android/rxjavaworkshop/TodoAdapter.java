package com.treehouse.android.rxjavaworkshop;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.jakewharton.rxbinding.widget.RxCompoundButton;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoHolder> implements Action1<TodoList> {

    LayoutInflater inflater;

    TodoList data = new TodoList();
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

        holder.subscription = RxCompoundButton.checkedChanges(holder.checkbox)
                .skip(1)
                .map(new Func1<Boolean, Todo>() {
                    @Override
                    public Todo call(Boolean aBoolean) {
                        return todo;
                    }
                })
                .subscribe(subscriber);
    }

    @Override
    public void onViewDetachedFromWindow(TodoHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.subscription.unsubscribe();
    }

    @Override
    public void call(TodoList list) {
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
