package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.Callable1;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.TaskCompletionSource;
import com.google.firebase.tasks.Tasks;

import java.util.concurrent.ExecutionException;

/**
 * Created 6/13/2017.
 */

public class TaskSingleValueEventFor {
    private DatabaseReference ref;
    private Callable1<DataSnapshot> onCompleteListener = new Callable1<DataSnapshot>() {
        @Override
        public void call(DataSnapshot arg) {
            //noinspection HardCodedStringLiteral
            System.out.println("onCompleteListener:"+arg.toString());
        }
    };

    public TaskSingleValueEventFor() {
    }

    public TaskSingleValueEventFor(DatabaseReference ref) {
        this.ref = ref;
    }

    public TaskSingleValueEventFor setRef(DatabaseReference ref) {
        this.ref = ref;
        return this;
    }

    public TaskSingleValueEventFor addOnCompleteListener(Callable1<DataSnapshot> listener) {
        onCompleteListener = listener;
        return this;
    }

    public void start() {
        if(ref == null) {
            //noinspection HardCodedStringLiteral
            System.err.println("TaskSingleValueEventFor: ref is not defined.");
            Thread.dumpStack();
            return;
        }
        final TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tcs.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                tcs.setException(databaseError.toException());
                databaseError.toException().printStackTrace();
            }
        });
        Task<DataSnapshot> task = tcs.getTask();
        try {
            Tasks.await(task);
            DataSnapshot dataSnapshot = task.getResult();
            onCompleteListener.call(dataSnapshot);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            onCompleteListener.call(null);
        }
    }

}
