package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.Runnable1;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.TaskCompletionSource;
import com.google.firebase.tasks.Tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created 6/13/2017.
 */

public class TaskSingleValueEventFor {
    private DatabaseReference ref;
    private Runnable1<DataSnapshot> onCompleteListener = new Runnable1<DataSnapshot>() {
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

    public TaskSingleValueEventFor addOnCompleteListener(Runnable1<DataSnapshot> listener) {
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
        Common.log("TSVE", "--debug-- start001:"+tcs);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Common.log("TSVE", "--debug-- start010:"+tcs);
                tcs.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Common.log("TSVE", "--debug-- start011");
                tcs.setException(databaseError.toException());
                databaseError.toException().printStackTrace();
            }
        });
        Common.log("TSVE", "--debug-- start002:"+ref);
        Task<DataSnapshot> task = tcs.getTask();
        Common.log("TSVE", "--debug-- start003:"+task);
        try {
//            Tasks.await(task);
            Tasks.await(task, 10, TimeUnit.SECONDS);
            Common.log("TSVE", "--debug-- start004");
            DataSnapshot dataSnapshot = task.getResult();
            Common.log("TSVE", "--debug-- start005:"+dataSnapshot);
            onCompleteListener.call(dataSnapshot);
            Common.log("TSVE", "--debug-- start006");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Common.log("TSVE", "--debug-- start007");
            e.printStackTrace();
            Common.log("TSVE", "--debug-- start008");
            onCompleteListener.call(null);
            Common.log("TSVE", "--debug-- start009");
        }
    }

}
