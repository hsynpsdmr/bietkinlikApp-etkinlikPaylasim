package com.example.bietkinlik;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConversationsActivity extends AppCompatActivity {

    ListView lvConversation;
    ArrayList<String> listConversation =new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    String msgTopic;
    private String UserId;
    private DatabaseReference dRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        lvConversation=findViewById(R.id.lvConversation);

        UserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,listConversation);
        lvConversation.setAdapter(arrayAdapter);

        dRef= FirebaseDatabase.getInstance().getReference().child("Activitys").child(UserId).child("Chat");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set =new HashSet<String>();
                Iterator i=dataSnapshot.getChildren().iterator();

                while (i.hasNext()){
                    set.add(((DataSnapshot)i.next()).getKey());
                }
                arrayAdapter.clear();
                arrayAdapter.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        lvConversation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(),ChatActivity.class);
                i.putExtra("parent_key",((TextView)view).getText().toString());
                i.putExtra("control","dm");
                i.putExtra("topic",(String)msgTopic);
                startActivity(i);
            }
        });

        MsgTopic();
    }

    private void MsgTopic(){
        dRef = FirebaseDatabase.getInstance().getReference().child("Activitys");
        dRef.child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("activity"))) {
                    msgTopic = Objects.requireNonNull(dataSnapshot.child("activity").getValue()).toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}