package com.example.dc_admin.mywatcher;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements Watcher
{
    Handler handler = new Handler();
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        doTest();
    }

    private void doTest()
    {
        final Watched watched = new ConcreteWatched();
        watched.add(this);
        Content content = new Content();
        content.setId(1126);
        content.setName("tjd");
        content.setAddress("suzhou");
        doDataChange(watched, content);
        //      // 被观察者可以调用删除方法
        //      watched.remove(watcher2);
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                Content content = new Content();
                content.setName("wy");
                content.setAddress("xuzhou");
                content.setId(410);
                doDataChange(watched, content);
            }
        }, 5000);
    }

    public void doDataChange(Watched watched, Content content)
    {
        watched.notifyWatcher(content);
    }

    @Override
    public void updateNotify(Content content)
    {
        final String s = "mine  id = " + content.getId() + "  name = " + content.getName();
        doShowText(s);
    }

    protected void doShowText(String s)
    {
        tv.setText(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
