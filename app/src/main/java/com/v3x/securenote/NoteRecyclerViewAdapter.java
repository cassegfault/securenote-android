package com.v3x.securenote;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.v3x.securenote.models.NoteContent.NoteItem;

import java.util.List;


public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {
    private List<NoteItem> mDataset;
    public Context mContext;

    public NoteRecyclerViewAdapter(List<NoteItem> myDataset, Context ctx) {
        mDataset = myDataset;
        mContext = ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NoteRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_note, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mBodyView.setText(mDataset.get(position).body);
        holder.mTitleView.setText(mDataset.get(position).title);
        holder.noteId = mDataset.get(position).id;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mBodyView;
        public String noteId = "";
        public NoteItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.item_title);
            mBodyView = (TextView) view.findViewById(R.id.item_body);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(v.getContext(), EditorActivity.class);
                    mIntent.putExtra("id",noteId);
                    mContext.startActivity(mIntent);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mBodyView.getText() + "'";
        }
    }
}