package nova.daniel.empatica.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Caregiver;

/**
 * Adapter for the RecyclerView that displays a list of caregivers.
 * Name and profile picture are displayed in each view.
 */
@SuppressWarnings("unchecked")
public class CaregiverAdapter extends RecyclerView.Adapter<CaregiverAdapter.ViewHolder> {

    private List<Caregiver> caregiversList = Collections.emptyList();
    private Context mContext;
    private boolean mSort = false;

    private onItemClickListener caregiverSelectedListener;

    public CaregiverAdapter(Context context) {
        mContext = context;
        caregiverSelectedListener = (onItemClickListener) context;
    }

    @NonNull
    @Override
    public CaregiverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.caregiver_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaregiverAdapter.ViewHolder holder, int position) {
        String name = caregiversList.get(position).mFirstName;
        String lastName = caregiversList.get(position).mLastName;

        holder.mPosition = position;
        // All names are saved in lower case, capitalize them
        holder.firstNameTextView.setText(Utils.capitalizeString(name));
        holder.lastNameTextView.setText(Utils.capitalizeString(lastName));

        // Glide API call to fetch the caregiver's picture
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

    /**
     * Toggles if the list of caregivers is to be sorted.
     */
    public void doSort() {
        this.mSort = !mSort;
        setCaregiversList(this.caregiversList);
    }

    /**
     * Sort the list of caregivers alphabetically by last names.
     * Overwrites the existing caregivers list and triggers the data set notification.
     *
     * @param caregiversList List of caregivers.
     */
    public void setCaregiversList(List<Caregiver> caregiversList) {
        if(mSort) {
            Collections.sort(caregiversList, (Comparator) (o1, o2) -> {
                Caregiver p1 = (Caregiver) o1;
                Caregiver p2 = (Caregiver) o2;
                return p1.mLastName.compareToIgnoreCase(p2.mLastName);
            });
        }
        this.caregiversList = caregiversList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder consists of the caregiver's full first and last name, capitalized.
     * Finally, a thumbnail picture of the caregiver.
     * <p>
     * Each view has an onclick listener, used as a callback for the selected caregiver.
     */
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

        /**
         * Onclick event for the selected caregiver, calls the implemented interface passing the selected
         * {@link Caregiver} object as parameter.
         * @param v View
         */
        @Override
        public void onClick(View v) {
            if(caregiverSelectedListener != null){
                caregiverSelectedListener.onCaregiverClick(caregiversList.get(mPosition), mPosition);
            }
        }
    }

    /**
     * Interface that responds to clicking events for each caregivers row.
     */
    public interface onItemClickListener {
        void onCaregiverClick(Caregiver caregiver, int position);
    }
}
