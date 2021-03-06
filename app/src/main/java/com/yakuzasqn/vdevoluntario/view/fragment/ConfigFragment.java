package com.yakuzasqn.vdevoluntario.view.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.glide.slider.library.svg.GlideApp;
import com.google.firebase.auth.FirebaseAuth;
import com.orhanobut.hawk.Hawk;
import com.yakuzasqn.vdevoluntario.R;
import com.yakuzasqn.vdevoluntario.model.User;
import com.yakuzasqn.vdevoluntario.support.Constants;
import com.yakuzasqn.vdevoluntario.support.FirebaseUtils;
import com.yakuzasqn.vdevoluntario.view.activity.CreatePostActivity;
import com.yakuzasqn.vdevoluntario.view.activity.LoginActivity;
import com.yakuzasqn.vdevoluntario.view.activity.ManagePostActivity;
import com.yakuzasqn.vdevoluntario.view.activity.UpdateUserActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragment extends Fragment {

    public ConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_config, container, false);

        LinearLayout llVolunteerData = v.findViewById(R.id.config_ll_volunteer_data);
        LinearLayout llManageVolunteerPost = v.findViewById(R.id.config_ll_manage_volunteer_post);
        LinearLayout llSignOut = v.findViewById(R.id.config_ll_sign_out);
        CircleImageView userPhoto = v.findViewById(R.id.config_user_photo);
        TextView userName = v.findViewById(R.id.config_user_name);

        User user = Hawk.get(Constants.USER_SESSION);

        if (user != null){
            if (user.getPicture() != null)
                GlideApp.with(getActivity()).load(user.getPicture()).centerCrop().into(userPhoto);
            userName.setText(user.getName());
        }

        llVolunteerData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNextActivity(Constants.USER_DATA_ACTIVITY);
            }
        });

        llManageVolunteerPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNextActivity(Constants.MANAGE_POST_ACTIVITY);
            }
        });

        llSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseAuth mAuth = FirebaseUtils.getFirebaseAuth();
                mAuth.signOut();

                openNextActivity(Constants.LOGIN_ACTIVITY);
            }
        });

        return v;
    }

    private void openNextActivity(int activityName){
        Intent intent = null;

        switch (activityName){
            case Constants.USER_DATA_ACTIVITY:
                intent = new Intent(getActivity(), UpdateUserActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_MAIN);
                break;
            case Constants.MANAGE_POST_ACTIVITY:
                intent = new Intent(getActivity(), ManagePostActivity.class);
                break;
            case Constants.LOGIN_ACTIVITY:
                intent = new Intent(getActivity(), LoginActivity.class);
                Hawk.delete(Constants.USER_SESSION);
                getActivity().finish();
                break;
        }
        startActivity(intent);
    }

}
