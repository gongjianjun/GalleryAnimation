package io.github.francisshi.galleryanimation;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import io.github.francisshi.galleryanimation.adapter.GridViewAdapter;


public class MainActivity extends FragmentActivity {

    private GridView mGridView;
    private GridViewAdapter mGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridView = (GridView) this.findViewById(R.id.gridView);
        mGridViewAdapter = new GridViewAdapter(this);
        mGridView.setAdapter(mGridViewAdapter);

        int horizontalSpacing = mGridView.getHorizontalSpacing();
        int verticalSpacing = mGridView.getVerticalSpacing();
        mGridViewAdapter.setSpacing(horizontalSpacing,verticalSpacing);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
