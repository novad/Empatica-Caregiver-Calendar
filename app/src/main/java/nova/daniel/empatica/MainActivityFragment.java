package nova.daniel.empatica;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements HoursViewAdapter.ItemClickListener{

    RecyclerView hoursRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ArrayList<String> animalNames = new ArrayList<>();
        for (int i=0; i<30; i++)
            animalNames.add(Integer.toString(i));

        hoursRecyclerView = view.findViewById(R.id.hoursView);
        hoursRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(view.getContext());
        hoursRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HoursViewAdapter(view.getContext(), animalNames);
        ((HoursViewAdapter) mAdapter).setClickListener(this);
        hoursRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this.getActivity(), "Toast!!!", Toast.LENGTH_LONG).show();
    }
}

