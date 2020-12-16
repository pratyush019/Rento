package com.tlabs.rento.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tlabs.rento.Activities.Approval;
import com.tlabs.rento.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
private final LayoutInflater layoutInflater;
final Context appContext;
final  String zone;
    private final List<String> requesterUid;
public RequestAdapter(Context context, List<String> requesterUid,String zone){
        this.layoutInflater=LayoutInflater.from(context);
        appContext=context;
        this.requesterUid=requesterUid;
        this.zone=zone;
        }

@NonNull
@Override
public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=layoutInflater.inflate(R.layout.request_list,parent,false);
        return new RequestAdapter.ViewHolder(view);

        }

@Override
public void onBindViewHolder(@NonNull final RequestAdapter.ViewHolder holder, final int position) {
        String Uid=requesterUid.get(position);
        String[] details=new String[2];

    FirebaseDatabase.getInstance().getReference("users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            details[0]=snapshot.child("name").getValue().toString();
            if (snapshot.hasChild("image"))
                details[1]=snapshot.child("image").getValue().toString();
            else details[1]=null;

            holder.name.setText(details[0]);

            if (details[1]!=null)
                Glide.with(appContext)
                        .load(Uri.parse(details[1]))
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(holder.image);
            else holder.progressBar.setVisibility(View.GONE);


        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });





        holder.linearLayout.setOnClickListener(view -> {
            Intent intent=new Intent(appContext, Approval.class);
            intent.putExtra("requesterUid",Uid);
            intent.putExtra("zone",zone);
        appContext.startActivity(intent);
        });


        }

@Override
public int getItemCount() {
        return requesterUid.size();
        }

public static class ViewHolder extends RecyclerView.ViewHolder {
    final LinearLayout linearLayout;
    final TextView name;
    final ImageView image;
    final ProgressBar progressBar;
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        linearLayout =itemView.findViewById(R.id.linearLayout);
        name=itemView.findViewById(R.id.name);
        image=itemView.findViewById(R.id.image);
        progressBar=itemView.findViewById(R.id.progressbar);
    }
}

}

