package com.example.lab5_starter;

import static com.google.firebase.firestore.FieldValue.delete;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;
    private Button delCityButton;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    private int current_pos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("Cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }

        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);
        delCityButton = findViewById(R.id.del_button);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        // select item functionality
        cityListView.setChoiceMode(1);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Update background
                view.setBackgroundColor(Color.parseColor("#E0E0FF"));
                // set new position
                if (current_pos == -1) {
                    current_pos = position;
                    // code for handling previously selected item
                } else if (current_pos != position) {
                    // restore old background
                    View prev = parent.getChildAt(current_pos);
                    prev.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    current_pos = position;
                }
            }
        });

        // Long click to edit
        cityListView.setLongClickable(true);
        cityListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // lab code
                City city = cityArrayAdapter.getItem(i);
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(),"City Details");
                return false;
            }
        });

        delCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_pos != -1) {
                    City city = cityArrayList.remove(current_pos);
                    cityArrayAdapter.notifyDataSetChanged();
                    current_pos = -1;

                    // Update firestore db
                    citiesRef
                            .document(city.getName())
                            .delete();

                }
            }
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        //DocumentReference docRef = citiesRef.document(city.getName());
        //docRef.set(city);
        citiesRef
                .document(city.getName())
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "DocumentSnapshot successfully written!");
                    }
                });
    }
}
