package oms.sns.facebook.ui;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import oms.sns.facebook.R;

public class SNSPreferenceActivity extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        // Load the XML preferences file
	        addPreferencesFromResource(R.xml.sns_preference);   
	}
	
	public boolean onPreferenceChange(Preference pref, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

}
