package geniussoft.subodh.sudeep.textscanner;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import codeville.rashmi.sudeep.paperscan.R;


public class Details extends ActionBarActivity {

    TextView name,type,site;
    String name_txt, type_txt,site_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entity_details);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name_txt = extras.getString("name");
            type_txt = extras.getString("type");
            site_txt = extras.getString("site");
        }

        site = (TextView)findViewById(R.id.e_site);
        name = (TextView)findViewById(R.id.e_name);
        type = (TextView)findViewById(R.id.e_type);

        site.setText(site_txt);
        name.setText(name_txt);
        type.setText(type_txt);
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
