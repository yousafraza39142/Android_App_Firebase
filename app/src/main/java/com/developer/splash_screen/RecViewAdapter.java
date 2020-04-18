package com.developer.splash_screen;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.CardViewHolder> {

    List<UserModel> userModels;
    Animation animation;
    public RecViewAdapter(ArrayList<UserModel> userModels) {
        this.userModels = userModels;
        System.out.println("----------------------------------------->" + this.userModels.toString());
    }


    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_holder, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        //____________________GET DATA___________________//
        UserModel data = userModels.get(position);
        String name = data.getName();
        String email = data.getEmail();
        String photoUrl = data.getProfilePic();

        //____________________BIND DATA_________________//
        holder.tv_viewHolder_name.setText(name);
        holder.tv_viewHolder_email.setText(email);
//        Picasso.get().load(Uri.parse(photoUrl)).into(holder.iv_viewHolder_pic);


        animation = AnimationUtils.loadAnimation(holder.cv_viewHolder.getContext(),R.anim.fade_in);

        holder.cv_viewHolder.startAnimation(animation);
        Picasso.get().load(Uri.parse(photoUrl)).fetch(new Callback(){
            @Override
            public void onSuccess() {
                holder.iv_viewHolder_pic.setAlpha(0f);
                Picasso.get().load(Uri.parse(photoUrl)).into(holder.iv_viewHolder_pic);
                holder.iv_viewHolder_pic.animate().setDuration(300).alpha(1f).start();
            }

            @Override
            public void onError(Exception e) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_viewHolder_name, tv_viewHolder_email;
        CircleImageView iv_viewHolder_pic;
        CardView cv_viewHolder;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_viewHolder_name = itemView.findViewById(R.id.tv_viewHolder_name);
            tv_viewHolder_email = itemView.findViewById(R.id.tv_viewHolder_email);
            iv_viewHolder_pic = itemView.findViewById(R.id.iv_viewHolder_pic);
            cv_viewHolder = itemView.findViewById(R.id.cv_viewHolder);
        }


        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(),ProfileActivity.class);
            intent.putExtra("Name",tv_viewHolder_name.getText().toString());
            intent.putExtra("Email",tv_viewHolder_email.getText().toString());
        }
    }
}
