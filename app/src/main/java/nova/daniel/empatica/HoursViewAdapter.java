package nova.daniel.empatica;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class HoursViewAdapter extends RecyclerView.Adapter<HoursViewAdapter.HoursViewHolder>{

    private List<String> mData;
    private ItemClickListener mClickListener;
    private LayoutInflater mInflater;

    class HoursViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView mNameTextView;

        HoursViewHolder(View view){
            super(view);
            mNameTextView = view.findViewById(R.id.carerName);
            mNameTextView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    HoursViewAdapter(Context context, List<String> data){
        // TODO: pass data to constructor
        mInflater = LayoutInflater.from(context);
        mData = data;
    }

    @NonNull
    @Override
    public HoursViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = mInflater.inflate(R.layout.hoursview_row, viewGroup, false);
        return new HoursViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HoursViewHolder hoursViewHolder, int i) {
        String name = mData.get(i);
        hoursViewHolder.mNameTextView.setText(name);

    }

    @Override
    public int getItemCount() {
        return mData.size(); //TODO total number of rows, like mdata.size
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
