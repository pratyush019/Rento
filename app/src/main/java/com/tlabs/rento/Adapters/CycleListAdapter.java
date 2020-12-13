package com.tlabs.rento.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tlabs.rento.Activities.CycleDetails;
import com.tlabs.rento.Helpers.CycleList;
import com.tlabs.rento.R;

import java.util.List;

public class  CycleListAdapter extends RecyclerView.Adapter<CycleListAdapter.ViewHolder> {
    private final LayoutInflater layoutInflater;
    private final List<CycleList> cycleList;
    final Context appContext;
    public CycleListAdapter(Context context, List<CycleList> cycleLists){
        this.layoutInflater=LayoutInflater.from(context);
        this.cycleList =cycleLists;
        appContext=context;
    }

    @NonNull
    @Override
    public CycleListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=layoutInflater.inflate(R.layout.card_design,parent,false);

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        params.height = (parent.getMeasuredHeight() / 2);
        view.setLayoutParams(params);

        return new CycleListAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final CycleListAdapter.ViewHolder holder, final int position) {
        CycleList currentCycle=cycleList.get(position);

        final String brand= currentCycle.getCycleName();
        final String availability=currentCycle.getCycleAvailability();
        final String imageURL=currentCycle.getCycleImageURL();


        Glide.with(appContext)
                .load(Uri.parse(imageURL))
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
                .into(holder.cycleImage);

        holder.brand.setText(brand);
        holder.availability.setText(availability);
        holder.relativeLayout.setOnClickListener(view -> {
            Intent intent=new Intent(appContext, CycleDetails.class);
            intent.putExtra("brand",currentCycle.getCycleName());
            intent.putExtra("image",currentCycle.getCycleImageURL());
            intent.putExtra("available",currentCycle.getCycleAvailability());
            intent.putExtra("note",currentCycle.getCycleNote());
            intent.putExtra("phone",currentCycle.getPhone());
            intent.putExtra("lat",currentCycle.getLat());
            intent.putExtra("lon",currentCycle.getLon());
            intent.putExtra("renterUid",currentCycle.getRenterUid());
           appContext.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return cycleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout relativeLayout;
        final TextView brand;
        final TextView availability;
        final ImageView cycleImage;
        final ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout =itemView.findViewById(R.id.relativeLayout);
            brand=itemView.findViewById(R.id.cycleBrand);
            availability=itemView.findViewById(R.id.availability);
            cycleImage=itemView.findViewById(R.id.cycleImage);
            progressBar=itemView.findViewById(R.id.progress);
        }
    }
}
