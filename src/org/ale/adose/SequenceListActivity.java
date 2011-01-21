package org.ale.adose;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SequenceListActivity extends Activity {
	
	SharedPreferences prefs;
	SequenceAdapter sa;
	List<BrainwaveSequence> lm = new ArrayList<BrainwaveSequence>();
	ListView lv;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.seq_list);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        
        // Brainwave Sequences
        // XXX: Make directory-based
        BrainwaveSequence bs0 = new BrainwaveSequence("meditation.drugs", null);
        bs0.setNameDesc("Meditation", "The classic Meditation sequence, by Mitch Altman");
        lm.add(bs0);
        BrainwaveSequence bs1 = new BrainwaveSequence("stereo_meditation.drugs", null);
        bs1.setNameDesc("Stereoscopic Meditation", "A stereoscopic version of the classic");
        lm.add(bs1);
        BrainwaveSequence bs2 = new BrainwaveSequence("sleep.drugs", null);
        bs2.setNameDesc("Sleep", "A nice sequence before bed, by garagemonkeysan");
        lm.add(bs2);
        BrainwaveSequence bs3 = new BrainwaveSequence("focus.drugs", null);
        bs3.setNameDesc("Focus", "Lots of Alpha and Beta for better focus, by Rich Jones");
        lm.add(bs3);
        BrainwaveSequence bs4 = new BrainwaveSequence("rem.drugs", null);
        bs4.setNameDesc("REM", "Cycle of Theta and Beta, just like REM sleep. by Rich Jones");
        lm.add(bs4);
        BrainwaveSequence bs5 = new BrainwaveSequence("psych.drugs", null);
        bs5.setNameDesc("Psychadelic", "A fast, colorful and trippy sequence, by Saxo.");
        lm.add(bs5);
        BrainwaveSequence bs6 = new BrainwaveSequence("brain_machine.drugs", null);
        bs6.setNameDesc("Brain Machine", "A colorized version of the Meditation sequence.");
        lm.add(bs6);
        BrainwaveSequence bs7 = new BrainwaveSequence("creativity.drugs", null);
        bs7.setNameDesc("Creativity", "A high-beta sequence to induce creative thinking, by Rich Jones.");
        lm.add(bs7);
        BrainwaveSequence bs8 = new BrainwaveSequence("relax.drugs", null);
        bs8.setNameDesc("Relax", "High alpha, low beta leading to theta and delta for ultimate relaxation. By SkipIntro.");
        lm.add(bs8);
        
        lv = (ListView)findViewById(R.id.listview);
        SequenceAdapter sa = new SequenceAdapter(this);
        sa.setList(lm);
        lv.setAdapter(sa);
        final Context c = this;
        lv.setOnItemClickListener(new OnItemClickListener(){
            
            public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
                Intent i = new Intent(c, PlaySound.class);
                BrainwaveSequence t = lm.get(arg2);
                i.putExtra("sequence", t.filename);
                System.out.println("starting activity");
                c.startActivity(i);
                return;
        }

    });
    }
    
    @Override
    public void onStart(){
    	super.onStart();
        
    	
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case 0:
    			return(true);
    	}
    	return(super.onOptionsItemSelected(item));
    }
    
    private class SequenceAdapter extends BaseAdapter{
        
        List<BrainwaveSequence> l;
        Context c;
        private LayoutInflater mInflater;
        private TextView top;
        private TextView bottom;
        
        public SequenceAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            c = context;
        }

        
       public int getCount() {
           // TODO Auto-generated method stub
           return l.size();
       }

       public Object getItem(int position) {
           // TODO Auto-generated method stub
           return null;
       }

       public long getItemId(int position) {
           // TODO Auto-generated method stub
           return 0;
       }

       /**
        * Make a view to hold each row.
        *
        * @see android.widget.ListAdapter#getView(int, android.view.View,
        *      android.view.ViewGroup)
        */
       public View getView(int position, View convertView, ViewGroup parent) {
           
           
           // A ViewHolder keeps references to children views to avoid unneccessary calls
           // to findViewById() on each row.
           ViewHolder holder;

           // When convertView is not null, we can reuse it directly, there is no need
           // to reinflate it. We only inflate a new View when the convertView supplied
           // by ListView is null.
           if (convertView == null) {
               convertView = mInflater.inflate(R.layout.result_row, null);

               // Creates a ViewHolder and store references to the two children views
               // we want to bind data to.
               holder = new ViewHolder();
               holder.text = (TextView) convertView.findViewById(R.id.titletext);
               holder.text2 = (TextView) convertView.findViewById(R.id.detailstext);
               holder.details = (TextView) convertView.findViewById(R.id.mp3_url);
               
               convertView.setTag(holder);
           } else {
               // Get the ViewHolder back to get fast access to the TextView
               // and the ImageView.
               holder = (ViewHolder) convertView.getTag();
           }

           // Bind the data efficiently with the holder.
           holder.text.setText(l.get(position).getName());
           holder.text2.setText(l.get(position).getDescription());
           

           return convertView;
       }

       class ViewHolder {
           TextView text;
           TextView text2;
           TextView url;
           TextView details;
           
       }

       
       public void setList(List<BrainwaveSequence> ml){
           l = ml;
       }
       
        
    }

}