package com.legal.adviser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.legal.adviser.Adapters.MessgaeAdapter;
import com.legal.adviser.ClientSide.ClientChat;
import com.legal.adviser.Models.Chat;
import com.legal.adviser.databinding.ActivityLawyerChatBinding;

public class LawyerChat extends AppCompatActivity {

    ActivityLawyerChatBinding binding;

    String ClientID = "";
    String name = "";
    String image = "";
    String phone = "";

    FirebaseAuth auth;

    List<Chat> list;
    RecyclerView recyclerView;
    MessgaeAdapter adapter;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyer_chat);

        binding = ActivityLawyerChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            ClientID = bundle.getString("ClientID");
            name = bundle.getString("name");
            image = bundle.getString("image");
            phone = bundle.getString("phone");
        }

        auth = FirebaseAuth.getInstance();

        Glide.with(this)
                .load(image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.personIMG);

        binding.personName.setText(name);

        binding.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = binding.yourMsg.getText().toString().trim();

                if(!msg.isEmpty()){
                    sendMessage(auth.getCurrentUser().getUid(),
                            ClientID,
                            msg);
                }
                else
                    Toast.makeText(LawyerChat.this, "Please Write Message", Toast.LENGTH_SHORT).show();

                binding.yourMsg.setText("");
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.chatCustomerRecycler.setLayoutManager(layoutManager);
        binding.chatCustomerRecycler.setHasFixedSize(true);

        readMessage(auth.getCurrentUser().getUid(), ClientID, image);
    }

    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }
    private void readMessage(final String myID, final String userID, final String imgURL){
        list = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String message = snapshot.child("message").getValue().toString();
                    String receiver = snapshot.child("receiver").getValue().toString();
                    String sender = snapshot.child("sender").getValue().toString();
                    if(receiver.equals(myID) && sender.equals(userID) ||
                            receiver.equals(userID) && sender.equals(myID)){
                        list.add(new Chat(sender,receiver,message));
                    }
                    SharedPreferences shared = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);

                    adapter = new MessgaeAdapter(LawyerChat.this,list,imgURL,shared.getString("IMAGE",""));
                    binding.chatCustomerRecycler.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}