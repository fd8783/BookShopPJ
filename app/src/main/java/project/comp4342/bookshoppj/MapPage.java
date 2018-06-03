package project.comp4342.bookshoppj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class MapPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_page);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
