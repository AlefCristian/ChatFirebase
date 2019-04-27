package com.santos.alef.chatfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;


import java.util.List;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {
    private GroupAdapter adapter;
    private User user, me;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RecyclerView recyclerView = findViewById(R.id.recycler_chat);
        editTextMessage = findViewById(R.id.editTexMessage);
        Button buttonSend = findViewById(R.id.buttonChat);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        this.user = getIntent().getExtras().getParcelable("user");
        getSupportActionBar().setTitle(user.getUserName());

        adapter = new GroupAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("user").
                document(FirebaseAuth.getInstance().getUid()).
                get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);

                        fechtMessages();
                    }
                });
    }

    private void fechtMessages() {
        if(me != null) {
            String fromId = me.getUserId();
            String toId = user.getUserId();


            FirebaseFirestore.getInstance().
                    collection("/conversations").
                    document(fromId).
                    collection(toId).
                    orderBy("timeStamp", Query.Direction.ASCENDING).
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChangeList = queryDocumentSnapshots.getDocumentChanges();

                            if(documentChangeList != null) {
                                for (DocumentChange doc: documentChangeList) {
                                    if(doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        adapter.add(new MessageItem(message));
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString();
        editTextMessage.setText(null);

        final String fromId = FirebaseAuth.getInstance().getUid();
        final String toId = user.getUserId();
        final long timeStamp = System.currentTimeMillis();

        final Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setTimeStamp(timeStamp);
        message.setText(text);

        if(!message.getText().isEmpty()) {
            FirebaseFirestore.getInstance().collection("/conversations").
                    document(fromId).
                    collection(toId).
                    add(message).
                    addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            //Log.d("Teste", documentReference.getId());
                            Contacts contacts = new Contacts();
                            contacts.setUid(toId);
                            contacts.setUserName(user.getUserName());
                            contacts.setPhotoUrl(user.getUrlPhotoPerfil());
                            contacts.setTimeStamp(message.getTimeStamp());
                            contacts.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages").
                                    document(fromId).
                                    collection("contacts").
                                    document(toId).
                                    set(contacts);
                        }
                    }).
                    addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });

            FirebaseFirestore.getInstance().collection("/conversations").
                    document(toId).
                    collection(fromId).
                    add(message).
                    addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            //Log.d("Teste", documentReference.getId());
                            Contacts contacts = new Contacts();
                            contacts.setUid(toId);
                            contacts.setUserName(user.getUserName());
                            contacts.setPhotoUrl(user.getUrlPhotoPerfil());
                            contacts.setTimeStamp(message.getTimeStamp());
                            contacts.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages").
                                    document(toId).
                                    collection("contacts").
                                    document(fromId).
                                    set(contacts);
                        }
                    }).
                    addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });
        }
    }

    private class MessageItem extends Item<ViewHolder> {

        private final Message message;
        private User userFrom;

        public MessageItem(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView textMessage = viewHolder.itemView.findViewById(R.id.textViewMessageUser);
            final ImageView imageViewUserMessage = viewHolder.itemView.findViewById(R.id.imageViewUserMessage);
            textMessage.setText(message.getText());


            DocumentReference docRef = FirebaseFirestore.getInstance().collection("user").document(message.getFromId());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d("Teste", message.getFromId());
                    userFrom = documentSnapshot.toObject(User.class);
                    Log.d("Teste" ,userFrom.getUrlPhotoPerfil());
                    Picasso.get().load(userFrom.getUrlPhotoPerfil()).into(imageViewUserMessage);
                }
            });

            //Picasso.get().load(userFrom.getUrlPhotoPerfil()).into(imageViewUserMessage);
        }

        @Override
        public int getLayout() {
            return !message.getFromId().equals(FirebaseAuth.getInstance().getUid()) ?
                    R.layout.item_from_message:
                    R.layout.item_to_message;
        }
    }
}
