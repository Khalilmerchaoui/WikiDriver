package app.m26.wikidriver.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.PhotoViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    private List<String> imgUrlList;

    public ImagesAdapter(Context context, List<String> imgUrlList) {
        this.context = context;
        this.imgUrlList = imgUrlList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.image_item_layout, parent, false);

        ViewHolder holder = new ImagesAdapter.ViewHolder(itemView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)holder.imageView.getDrawable()).getBitmap();
                if(bitmap != null) {
                    Intent intent = new Intent(context, PhotoViewActivity.class);
                    intent.putExtra("image", imgUrlList.get(position));
                    context.startActivity(intent);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        long startTime = System.currentTimeMillis();

        Picasso.with(holder.imageView.getContext())
                .load(imgUrlList.get(position))
                .error(R.drawable.image_not_loaded)
                .into(holder.imageView);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return imgUrlList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPic);

        }
    }
}
