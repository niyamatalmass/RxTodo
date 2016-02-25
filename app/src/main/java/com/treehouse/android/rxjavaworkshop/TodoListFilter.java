package com.treehouse.android.rxjavaworkshop;


import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public class TodoListFilter implements Action1<Todo> {

    public static final int ALL = 0;
    public static final int INCOMPLETE = 1;
    public static final int COMPLETE = 2;

    private int filterMode = ALL;

    private TodoList list = new TodoList();

    Observable<TodoList> observable;
    Action1<Integer> filterSubscriber;

    public TodoListFilter(TodoList l, int mode) {
        list = l;
        filterMode = mode;

        observable = Observable.create(new Observable.OnSubscribe<TodoList>() {
            @Override
            public void call(final Subscriber<? super TodoList> subscriber) {
                subscriber.onNext(getFilteredData());

                list.setListener(new TodoListener() {
                    @Override
                    public void onTodoListChanged(TodoList updatedList) {
                        list = updatedList;
                        subscriber.onNext(getFilteredData());
                    }
                });

                filterSubscriber = new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        filterMode = integer;
                        subscriber.onNext(getFilteredData());
                    }
                };
            }
        });
    }

    public Observable<TodoList> asObservable() {
        return observable;
    }

    public Action1<Integer> asFilterSubscriber() {
        return filterSubscriber;
    }

    public TodoList getList() {
        return list;
    }

    public int getMode() {
        return filterMode;
    }

    @Override
    public void call(Todo todo) {
        list.toggle(todo);
    }

    public TodoList getFilteredData() {
        switch (filterMode) {
            case ALL:
                return list;
            case INCOMPLETE:
                TodoList incompleteOnly = new TodoList();
                for (int i = 0; i < list.size(); i++) {
                    Todo item = list.get(i);
                    if (!item.isCompleted) {
                        incompleteOnly.add(item);
                    }
                }
                return incompleteOnly;
            case COMPLETE:
                TodoList completedOnly = new TodoList();
                for (int i = 0; i < list.size(); i++) {
                    Todo item = list.get(i);
                    if (item.isCompleted) {
                        completedOnly.add(item);
                    }
                }
                return completedOnly;
            default:
                return list;
        }
    }

}
