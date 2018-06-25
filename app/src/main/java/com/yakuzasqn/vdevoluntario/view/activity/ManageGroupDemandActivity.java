package com.yakuzasqn.vdevoluntario.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.hawk.Hawk;
import com.yakuzasqn.vdevoluntario.R;
import com.yakuzasqn.vdevoluntario.adapter.MyPostAdapter;
import com.yakuzasqn.vdevoluntario.model.Group;
import com.yakuzasqn.vdevoluntario.model.Post;
import com.yakuzasqn.vdevoluntario.model.User;
import com.yakuzasqn.vdevoluntario.support.Constants;
import com.yakuzasqn.vdevoluntario.support.FirebaseUtils;
import com.yakuzasqn.vdevoluntario.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ManageGroupDemandActivity extends AppCompatActivity {

    private List<Post> postList;
    private MyPostAdapter adapter;
    private TextView tvNotFound;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group_demand);

        Utils.setBackableToolbar(R.id.mgd_toolbar, "Gerenciar pedidos", ManageGroupDemandActivity.this);

        LinearLayout llNewDemand = findViewById(R.id.mgd_ll_new_demand);
        llNewDemand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageGroupDemandActivity.this, CreatePostActivity.class);
                intent.putExtra("typeOfPost", Constants.DEMAND);
                startActivityForResult(intent, Constants.REQUEST_CODE_POST_SUCCESS);
            }
        });

        /***************************************************************
         Montagem do RecyclerView e do Adapter
         ****************************************************************/

        tvNotFound = findViewById(R.id.mgd_tv_not_found_mp);
        postList = new ArrayList<>();

        final RecyclerView rvMyPost = findViewById(R.id.rv_mgd_post);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(ManageGroupDemandActivity.this);

        rvMyPost.setLayoutManager(mLayoutManager);


        /***************************************************************
         Recuperar dados do Firebase
         ****************************************************************/
        Group group = Hawk.get(Constants.CHOSEN_GROUP);

        dialog = ProgressDialog.show(ManageGroupDemandActivity.this, "", "Carregando postagens, aguarde...", true);
        DatabaseReference mRef = FirebaseUtils.getBaseRef().child("posts");
        Query queryRef = mRef.orderByChild("creatorId").equalTo(group.getId());
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Post post = dados.getValue(Post.class);
                    if (post.getType().equals("DEMAND"))
                        postList.add(post);
                }

                adapter = new MyPostAdapter(ManageGroupDemandActivity.this, postList);
                rvMyPost.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (postList.size() > 0) {
                    rvMyPost.setVisibility(View.VISIBLE);
                    tvNotFound.setVisibility(View.GONE);
                    dialog.dismiss();
                } else {
                    rvMyPost.setVisibility(View.GONE);
                    tvNotFound.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
                Utils.showToast(R.string.toast_failLoadingData, ManageGroupDemandActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Reload to update the data
        if (requestCode == Constants.REQUEST_CODE_POST_SUCCESS){
            finish();
            startActivity(new Intent(ManageGroupDemandActivity.this, ManageGroupDemandActivity.class));
        }
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

}
