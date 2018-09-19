package com.example.ran.multitype;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.ran.multitype.model.Animal;
import com.example.ran.multitype.model.Cat;
import com.example.ran.multitype.model.Dog;
import com.example.ran.multitype.adapter.ListAdapter;

import java.util.ArrayList;
import java.util.List;

//todo 防止对应多种delegate
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdapter adapter = new ListAdapter();
        recyclerView.setAdapter(adapter);

        List<Animal> animals = new ArrayList<>();

        animals.add(new Cat("American Curl"));
        animals.add(new Cat("Baliness"));
        animals.add(new Cat("Bengal"));
        animals.add(new Cat("Corat"));
        animals.add(new Cat("Manx"));
        animals.add(new Cat("Nebelung"));
        animals.add(new Dog("Aidi", 1));
        animals.add(new Dog("Chinook"));
        animals.add(new Dog("Appenzeller", 1));
        animals.add(new Dog("Collie"));

        adapter.setItems(animals);
        adapter.notifyDataSetChanged();
    }
}
