package com.av.avmessenger;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwindo extends AppCompatActivity {

    private String receiverUid, receiverName, receiverImg;
    private String senderUid, senderImg, roomId;

    private CircleImageView   profileIv;
    private TextView          nameTv;
    private CardView          sendBtn;
    private EditText          inputEt;
    private RecyclerView      rv;
    private messagesAdpter    adapter;
    private ArrayList<msgModelclass> list;

    private FirebaseAuth      auth;
    private FirebaseDatabase  db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo);
        getSupportActionBar().hide();

        db   = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // 1) pull extras
        receiverName = getIntent().getStringExtra("receiverName");
        receiverImg  = getIntent().getStringExtra("receiverImg");
        receiverUid  = getIntent().getStringExtra("receiverUid");

        // 2) get our uid
        if (auth.getCurrentUser() != null) {
            senderUid = auth.getUid();
        } else {
            Toast.makeText(this,
                    "Must be signed in",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3) bind UI
        profileIv = findViewById(R.id.profileimgg);
        nameTv    = findViewById(R.id.recivername);
        sendBtn   = findViewById(R.id.sendbtnn);
        inputEt   = findViewById(R.id.textmsg);
        rv        = findViewById(R.id.msgadpter);

        nameTv.setText(receiverName);
        Picasso.get().load(receiverImg).into(profileIv);

        // 4) build roomId once
        roomId = makeRoomId(senderUid, receiverUid);

        // 5) setup RecyclerView
        list    = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 6) fetch our profilepic then attach adapter+listener
        db.getReference("user")
                .child(senderUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snap) {
                                senderImg = snap.child("profilepic")
                                        .getValue(String.class);

                                adapter = new messagesAdpter(
                                        chatwindo.this,
                                        list,
                                        senderImg,
                                        receiverImg
                                );
                                rv.setAdapter(adapter);

                                // now listen on the same roomId
                                db.getReference("chats")
                                        .child(roomId)
                                        .child("messages")
                                        .addValueEventListener(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(
                                                            @NonNull DataSnapshot ds) {
                                                        list.clear();
                                                        for (DataSnapshot c : ds.getChildren()) {
                                                            list.add(
                                                                    c.getValue(msgModelclass.class));
                                                        }
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                    @Override
                                                    public void onCancelled(
                                                            @NonNull DatabaseError e) {
                                                        Toast.makeText(chatwindo.this,
                                                                "Load failed: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                            }
                            @Override public void onCancelled(
                                    @NonNull DatabaseError e) { }
                        });

        // 7) send => single write under roomId
        sendBtn.setOnClickListener(v -> {
            String txt = inputEt.getText().toString().trim();
            if (txt.isEmpty()) return;
            inputEt.setText("");

            msgModelclass m = new msgModelclass(
                    txt,
                    senderUid,
                    new Date().getTime()
            );

            db.getReference("chats")
                    .child(roomId)
                    .child("messages")
                    .push()
                    .setValue(m);
        });
    }

    /** same room for A↔B no matter order **/
    private String makeRoomId(String a, String b) {
        return (a.compareTo(b) < 0)
                ? a + "_" + b
                : b + "_" + a;
    }
}
