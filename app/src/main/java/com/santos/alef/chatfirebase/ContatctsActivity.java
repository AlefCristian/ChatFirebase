package com.santos.alef.chatfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import javax.annotation.Nullable;

public class ContatctsActivity extends AppCompatActivity {

    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatcts);

        RecyclerView rv = findViewById(R.id.recycler_contacts);

        adapter = new GroupAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent intent = new Intent(ContatctsActivity.this, ChatActivity.class);
                UserItem userItem = (UserItem) item;
                intent.putExtra("user", userItem.user);

                startActivity(intent);
            }
        });

        fetchUsers();
    }

    private void fetchUsers() {
        FirebaseFirestore.getInstance().collection("/user").
                addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null) {
                            Log.e("Teste", e.getMessage(), e);
                            return;
                        }

                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot doc : docs) {
                            User user = doc.toObject(User.class);
                            adapter.add(new UserItem(user));
                        }
                    }
                });
    }

    private class UserItem extends Item<ViewHolder> {
        private final User user;

        private UserItem(User user) {
            this.user = user;
        }


        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView textView = viewHolder.itemView.findViewById(R.id.textViewUserName);
            ImageView imgUser = viewHolder.itemView.findViewById(R.id.imageViewUser);

            Picasso.get().load(user.getUrlPhotoPerfil()).into(imgUser);
            textView.setText(user.getUserName());
        }

        @Override
        public int getLayout() {
            return R.layout.item_user;
        }
    }
}
