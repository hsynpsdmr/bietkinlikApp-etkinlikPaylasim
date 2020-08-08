package com.example.bietkinlik;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.renderscript.Script;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


public class ChatActivity extends AppCompatActivity {
    ImageButton btn_Gonder;
    EditText edtText_Mesaj;
    TextView tvTopic;
    ListView lvChat;
    ArrayList<String> listChat =new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    private DatabaseReference dRef;
    private String UserId;
    String msg_key,parentNode,Control,msgTopic,UserName;


    Object uName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btn_Gonder=findViewById(R.id.btn_Gonder);
        edtText_Mesaj=findViewById(R.id.edtText_Mesaj);
        tvTopic=findViewById(R.id.textView);
        lvChat=findViewById(R.id.lvChat);


        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,listChat);
        lvChat.setAdapter(arrayAdapter);
        parentNode=getIntent().getExtras().get("parent_key").toString();
        msgTopic=getIntent().getExtras().get("topic").toString();
        Control= Objects.requireNonNull(getIntent().getExtras().get("control")).toString();
        tvTopic.setText(msgTopic);
        getUserName();

        if(Control.equals("map")){
            UserName=getIntent().getExtras().get("username").toString();
            dRef= FirebaseDatabase.getInstance().getReference().child("Activitys").child(parentNode).child("Chat").child(UserName);
        }else {
            dRef = FirebaseDatabase.getInstance().getReference().child("Activitys").child(UserId).child("Chat").child(parentNode);
        }

        btn_Gonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object>map=new HashMap<String, Object>();
                msg_key=dRef.push().getKey();
                dRef.updateChildren(map);
                DatabaseReference dRef2= dRef.child(msg_key);
                Map<String,Object> map2=new HashMap<String, Object>();
                map2.put("msg",edtText_Mesaj.getText().toString());
                map2.put("user",uName);
                dRef2.updateChildren(map2);
                edtText_Mesaj.setText("");
            }
        });

        dRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateChat(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateChat(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public  void updateChat(DataSnapshot dataSnapshot){
        String msg,user,conversation;
        Iterator i=dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            msg=(String) ((DataSnapshot)i.next()).getValue();
            user=(String) ((DataSnapshot)i.next()).getValue();
            conversation=user+" : "+msg;
            listChat.add(conversation);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void getUserName(){
        dRef = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId);
        final Query userQuery = dRef.orderByChild("username");

        userQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                uName=dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}