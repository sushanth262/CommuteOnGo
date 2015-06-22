package com.example.unisol.commuteongo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.maps.MapFragment;

public class MainActivity extends Activity {

    Button b1,b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Driver/Passenger button set up
        b1 = (Button) findViewById(R.id.DriverButton);
        b2 = (Button) findViewById(R.id.PassengerButton);

        b1.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(), DriverMapsActivity.class);
                startActivity(intent);
             /*   Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Button 1", Toast.LENGTH_LONG);
                msg.show();*/
            }
        });

        b2.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Button 2", Toast.LENGTH_LONG);
                msg.show();
            }
        });

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
