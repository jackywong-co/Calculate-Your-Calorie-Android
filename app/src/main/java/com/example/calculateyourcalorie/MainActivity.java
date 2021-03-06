package com.example.calculateyourcalorie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calculateyourcalorie.RoomDataBase.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int ADD_ITEM_REQUEST = 1;
    private ItemViewModel itemViewModel;

    private TextView textViewProfileTarget;

    private Intent intent;
    private Bundle bundle;
    private String ProfileTarget;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TARGET = "target";
    private int mainTarget;
    ProgressBar adbCalorieLimitBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView counter = (TextView) findViewById(R.id.toolbar_calories);

        adbCalorieLimitBar = (ProgressBar) findViewById(R.id.add_bar_CalorieLimitBar);
        intent = this.getIntent();
        bundle = intent.getExtras();
        textViewProfileTarget = findViewById(R.id.mainTarget);

        if (bundle != null) {
            textViewProfileTarget.setText(bundle.getString("target"));
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TARGET, bundle.getString("target"));
            editor.apply();

        }
        loadData();
        if (textViewProfileTarget.getText().toString() == "") {
            mainTarget = 0;
        } else {
            mainTarget = Integer.parseInt(textViewProfileTarget.getText().toString());
        }
        adbCalorieLimitBar.setMax(mainTarget);

        // livedata sum calories
        itemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        itemViewModel.getTotalCalories().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                counter.setText(getString(R.string.ateCar) + integer);
                int x;
                x = mainTarget - integer;
                textViewProfileTarget.setText(getString(R.string.rest) + x);
                adbCalorieLimitBar.setProgress(x);
            }
        });

        // onClick floating action button
        FloatingActionButton FloatingActionButton = findViewById(R.id.fab);
        FloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddItemActivity.class);
                startActivityForResult(i, ADD_ITEM_REQUEST);
            }
        });
        setUpNavigationView();
        setUpRecyclerview();
    }

    public void setUpNavigationView() {
        // set toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        // NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        // set listener when selected item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Profile:
                        Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(profile);
                        break;
                    case R.id.Settings:
                        Intent setting = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(setting);
                        break;
                }
                return false;
            }
        });
    }

    // add data in database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {
            String date = data.getStringExtra(AddItemActivity.EXTRA_DATE);
            String period = data.getStringExtra(AddItemActivity.EXTRA_PERIOD);
            String category = data.getStringExtra(AddItemActivity.EXTRA_CATEGORY);
            String foodname = data.getStringExtra(AddItemActivity.EXTRA_FOODNAME);
            int calories = data.getIntExtra(AddItemActivity.EXTRA_CALORIES, 1);

            Item item = new Item(date, period, category, foodname, calories);
            itemViewModel.insert(item);

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void setUpRecyclerview() {
        // recyclerview
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final ItemAdapter adapter = new ItemAdapter();
        recyclerView.setAdapter(adapter);

        itemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(@NonNull List<Item> items) {
                // update recyclerview
                adapter.setItems(items);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                itemViewModel.delete(adapter.getItemAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

    }

    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_items:
                itemViewModel.deleteAllItems();
                Toast.makeText(MainActivity.this, "All item deleted", Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        ProfileTarget = sharedPreferences.getString(TARGET, "");
        textViewProfileTarget.setText(ProfileTarget);
    }
}