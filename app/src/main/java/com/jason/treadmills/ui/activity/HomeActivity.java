package com.jason.treadmills.ui.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jason.treadmills.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class HomeActivity extends ActionBarActivity {

    @InjectView(R.id.btnHomeAty1) ImageButton btn1;
    @InjectView(R.id.btnHomeAty2) ImageButton btn2;
    @InjectView(R.id.btnHomeAty3) ImageButton btn3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btnHomeAty1) void showToast(){
        startActivity(new Intent(this, TCoverFlowActivity.class));
    }

    @OnClick(R.id.btnHomeAty2) void showToast2(){
        startActivity(new Intent(this, TCarouselActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
