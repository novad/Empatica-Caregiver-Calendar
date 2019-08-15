package nova.daniel.empatica.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Caregiver;

public class CaregiverAdapter extends RecyclerView.Adapter<CaregiverAdapter.ViewHolder> {

    private List<Caregiver> caregiversList = Collections.emptyList();
    private Context mContext;
    private boolean mSort = false;

    private onItemClickListener caregiverSelectedListener;

    public CaregiverAdapter(Context context) {
        mContext = context;
        caregiverSelectedListener = (onItemClickListener) context;
    }

    @Override
    public CaregiverAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.caregiver_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CaregiverAdapter.ViewHolder holder, int position) {
        String name = caregiversList.get(position).mFirstName;
        String lastName = caregiversList.get(position).mLastName;

        holder.mPosition = position;
        holder.firstNameTextView.setText(String.format("%s%s", name.substring(0, 1).toUpperCase(), name.substring(1)));
        holder.lastNameTextView.setText(String.format("%s%s", lastName.substring(0, 1).toUpperCase(), lastName.substring(1)));

        String picUrl = caregiversList.get(position).mPictureURL;

        GlideApp.with(mContext)
                .load(picUrl)
                .placeholder(R.drawable.ic_person_outline_white_24dp)
                .centerCrop()
                .transform(new RoundedCornersTransformation(45, 2))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return caregiversList.size();
    }

    public void doSort() {
        this.mSort = !mSort;
        setCaregiversList(this.caregiversList);
    }

    public void setCaregiversList(List<Caregiver> caregiversList) {
        if(mSort) {
            Collections.sort(caregiversList, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    Caregiver p1 = (Caregiver) o1;
                    Caregiver p2 = (Caregiver) o2;
                    return p1.mLastName.compareToIgnoreCase(p2.mLastName);
                }
            });
        }
        this.caregiversList = caregiversList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        int mPosition;
        TextView firstNameTextView;
        TextView lastNameTextView;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.caregiver_firstname_textView);
            lastNameTextView = itemView.findViewById(R.id.caregiver_lastname_textView);
            imageView = itemView.findViewById(R.id.caregiverlist_pic);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(caregiverSelectedListener != null){
                caregiverSelectedListener.onItemClick(caregiversList.get(mPosition), mPosition);
            }
        }
    }

    public interface onItemClickListener {
        public void onItemClick(Caregiver caregiver, int position);
    }
}
