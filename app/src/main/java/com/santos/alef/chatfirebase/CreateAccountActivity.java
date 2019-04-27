package com.santos.alef.chatfirebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import javax.annotation.Nullable;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText editTexUser, editTextEmail, editTextPassword;
    private Button buttonCreateAccount, buttonPhotoProfile;
    private ImageView imageViewPhotoPerfil;
    private Uri photoPerfil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        editTexUser = findViewById(R.id.editTextUsuario);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonPhotoProfile = findViewById(R.id.buttonPhotoPerfil);
        buttonCreateAccount = findViewById(R.id.buttonLogar);
        imageViewPhotoPerfil = findViewById(R.id.imageViewPhotoPerfil);



        buttonPhotoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0 || data != null) {


            Bitmap bitmap = null;
            try {
                photoPerfil = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoPerfil);
                imageViewPhotoPerfil.setImageBitmap(bitmap);
                buttonPhotoProfile.setAlpha(0);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

    private void createUser()  {
        String user = editTexUser.getText().toString(),
                email = editTextEmail.getText().toString(),
                password = editTextPassword.getText().toString();

        if(!validateInput(user) || !validateInput(email) || validateInput(password)) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Log.i("Teste", task.getResult().getUser().getUid());
                        saveUserInfirebase();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Teste", e.getMessage());
                }
            });
        } else {
            Toast.makeText(this,"Email ou senha invalidos", Toast.LENGTH_SHORT);
        }

    }

    private void saveUserInfirebase() {
        String filename = UUID.randomUUID().toString();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("/images" + filename);
        storageReference.putFile(photoPerfil).
                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.i("Teste", uri.toString());

                                String uid = FirebaseAuth.getInstance().getUid();
                                String userName = editTexUser.getText().toString();
                                String url = uri.toString();

                                FirebaseFirestore.getInstance().collection("user").
                                        document(uid).
                                        set(new User(uid, userName, url)).
                                        addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(CreateAccountActivity.this, ContatctsActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);

                                            }
                                        }).
                                addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("Teste ", e.getMessage());
                                    }
                                });

                            }
                        });

                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Teste",e.getMessage());
                    }
                });
    }

    public static boolean validateInput(String string) {
        if(string == null || string.isEmpty())  return false;
        return true;
    }

}
