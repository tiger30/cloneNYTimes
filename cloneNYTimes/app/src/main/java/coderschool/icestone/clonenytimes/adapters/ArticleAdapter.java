package coderschool.icestone.clonenytimes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import coderschool.icestone.clonenytimes.R;
import coderschool.icestone.clonenytimes.models.Article;

/**
 * Created by IceStone on 3/17/2016.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder>  {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView headline;

        public ViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.ivImage);
            headline = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }

    // Store a member variable for the contacts
    private List<Article> mArticles;
    private Context context;

    // Pass in the contact array into the constructor
    public ArticleAdapter(List<Article> articles) {
        mArticles = articles;
    }

    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View articleView = inflater.inflate(R.layout.item_article_result, parent, false);
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(articleView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ArticleAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Article article = mArticles.get(position);
        // clear out recycled image
        viewHolder.thumbnail.setImageResource(0);
        viewHolder.headline.setText(article.getHeadline());

        String thumbnail = article.getThumbnail();
        if (!TextUtils.isEmpty(thumbnail)) {
            viewHolder.thumbnail.setVisibility(View.VISIBLE);
            Picasso.with(context).load(thumbnail).into(viewHolder.thumbnail);
        } else {
            viewHolder.thumbnail.setVisibility(View.GONE);
        }
    }

    public void swap(List<Article> articles){
        mArticles.clear();
        mArticles.addAll(articles);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }
}