package com.yakuzasqn.vdevoluntario.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.glide.slider.library.svg.GlideApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.orhanobut.hawk.Hawk;
import com.yakuzasqn.vdevoluntario.R;
import com.yakuzasqn.vdevoluntario.model.Group;
import com.yakuzasqn.vdevoluntario.model.Post;
import com.yakuzasqn.vdevoluntario.model.User;
import com.yakuzasqn.vdevoluntario.support.Constants;
import com.yakuzasqn.vdevoluntario.support.FirebaseUtils;
import com.yakuzasqn.vdevoluntario.util.Utils;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class UpdatePostActivity extends AppCompatActivity implements Validator.ValidationListener {

    @Order(1)
    @NotEmpty(message = "Campo obrigatório")
    private EditText etPostTitle;

    @Order(2)
    @NotEmpty(message = "Campo obrigatório")
    private EditText etPostDescription;

    private ImageView postPhoto;

    private String title, description;

    private Post post;

    private Validator validator;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_post);

        Utils.setBackableToolbar(R.id.upo_toolbar, "", UpdatePostActivity.this);

        postPhoto = findViewById(R.id.upo_post_photo);
        etPostTitle = findViewById(R.id.upo_post_title);
        etPostDescription = findViewById(R.id.upo_post_description);
        Button btnPublish = findViewById(R.id.upo_publish);

        post = Hawk.get(Constants.CHOSEN_POST);

        GlideApp.with(getApplicationContext()).load(post.getUrlImage()).centerCrop().into(postPhoto);
        etPostTitle.setText(post.getTitle());
        etPostDescription.setText(post.getDescription());

        validator = new Validator(this);
        validator.setValidationListener(this);

        postPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = etPostTitle.getText().toString();
                description = etPostDescription.getText().toString();
                validator.validate();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidationSucceeded() {
        if (postPhoto.isSelected()){
            uploadPostPhoto();
        } else {
            post.setTitle(title);
            post.setDescription(description);
            updatePostDatabase(post);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            view.requestFocus();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Snackbar.make(error.getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadPostPhoto(){
        dialog = ProgressDialog.show(UpdatePostActivity.this, "", "Fazendo upload da foto, aguarde...", true);
        String timestamp = Utils.getCurrentTimestamp();

        StorageReference mStoreRef = FirebaseUtils.getFirebaseStorageReference()
                .child("userProfilePhoto/post_" + timestamp + ".jpg");

        postPhoto.setDrawingCacheEnabled(true);
        postPhoto.buildDrawingCache();

        Bitmap bitmap = postPhoto.getDrawingCache();

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        byte[] data = byteArray.toByteArray();

        UploadTask uploadTask = mStoreRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showToast(e.getMessage(), UpdatePostActivity.this);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();

                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                post.setUrlImage(downloadUrl.toString());
                post.setTitle(title);
                post.setDescription(description);

                updatePostDatabase(post);
            }
        });
    }

    private void updatePostDatabase(Post post){
        try{
            dialog = ProgressDialog.show(UpdatePostActivity.this, "", "Atualizando postagem, aguarde...", true);

            DatabaseReference mRef = FirebaseUtils.getBaseRef().child("posts");

            mRef.child(post.getId()).setValue(post);

            Utils.showToast(R.string.toast_updated, UpdatePostActivity.this);
            dialog.dismiss();

            finish();
        } catch(Exception e){
            Utils.showToast(getString(R.string.toast_errorCreateUser), UpdatePostActivity.this);
            e.printStackTrace();
        }
    }

    private void openGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, Constants.REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK){
            Uri selectedImage = data.getData();
            GlideApp.with(getApplicationContext()).load(selectedImage).centerCrop().into(postPhoto);
            postPhoto.setSelected(true);
        }
    }
}
