package project.comp4342.bookshoppj;

import android.app.Dialog;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by fd8783 on 14/4/2018.
 */

public class SettingPage extends AppCompatActivity {

    public static boolean isBrowseHistorySaved = true;

    private Dialog confirmPopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        confirmPopUp = new Dialog(this);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        //need to import android.support.v7.widget.Toolbar instead of android.widget.Toolbar
        setSupportActionBar(tool);
        //set go back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Switch saveHistorySwitch = findViewById(R.id.save_history_switch);
//        saveHistorySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                isBrowseHistorySaved = isChecked;
//            }
//        });
        ConstraintLayout saveHistoryLayout = findViewById(R.id.save_history_layout);
        saveHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBrowseHistorySaved = !isBrowseHistorySaved;
                saveHistorySwitch.setChecked(isBrowseHistorySaved);
            }
        });

        ConstraintLayout clearHistoryLayout = findViewById(R.id.clear_history_layout);
        clearHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowConfirmPopUp();
            }
        });

    }

    public void ShowConfirmPopUp(){
        confirmPopUp.setContentView(R.layout.confirm_pop_up);
        confirmPopUp.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
