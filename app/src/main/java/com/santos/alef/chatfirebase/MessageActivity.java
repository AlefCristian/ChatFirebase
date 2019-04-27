package com.santos.alef.chatfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import javax.annotation.Nullable;

public class MessageActivity extends AppCompatActivity {

    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        RecyclerView recyclerViewContacts = findViewById(R.id.recycler_contacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GroupAdapter();
        recyclerViewContacts.setAdapter(adapter);
        
        verifyAuthentication();

        fetchLastMessage();
    }

    private void fetchLastMessage() {
        String uid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().collection("/last-messages").
                document(uid).
                collection("contacts").
                addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> docs =  queryDocumentSnapshots.getDocumentChanges();
                        if(docs != null) {
                            for(DocumentChange doc: docs) {
                                if(doc.getType() == DocumentChange.Type.ADDED) {
                                    Contacts contacts = doc.getDocument().toObject(Contacts.class);
                                    adapter.add(new ContactItem(contacts));
                                }
                            }
                        }
                    }
                });
    }

    private void verifyAuthentication() {
        if(FirebaseAuth.getInstance().getUid() == null) {
            Intent intent = new Intent(MessageActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contacts:
                Intent intent = new Intent(MessageActivity.this, ContatctsActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                verifyAuthentication();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ContactItem extends Item<ViewHolder> {
        private final Contacts contacts;

        private ContactItem(Contacts contacts) {
            this.contacts =  contacts;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {

            TextView textViewUserName = viewHolder.itemView.findViewById(R.id.textViewUserName);
            TextView textViewLastMessage = viewHolder.itemView.findViewById(R.id.textViewLastMessage);
            ImageView imageViewPhoto = viewHolder.itemView.findViewById(R.id.imageViewUser);

            textViewUserName.setText(contacts.getUserName());
            textViewLastMessage.setText(contacts.getLastMessage());

            Picasso.get().load(contacts.getPhotoUrl()).into(imageViewPhoto);



        }

        @Override
        public int getLayout() {
            return R.layout.item_user_message;
        }
    }
}
