package local.zipshortcut;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;

public class SettingsActivity extends Activity {
    EditText name;
    EditText[] fields=new EditText[CallSettings.KEYS.length];
    CheckBox consent;
    Spinner icons;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this); scroll.setFitsSystemWindows(true);
        LinearLayout box=new LinearLayout(this); box.setOrientation(1);
        int pad=(int)(24*getResources().getDisplayMetrics().density); box.setPadding(pad,pad,pad,pad);
        TextView title=new TextView(this); title.setText(R.string.configure_dot); title.setTextSize(26); box.addView(title);
        TextView help=new TextView(this); help.setText(R.string.setup_help); box.addView(help);
        int status=getSharedPreferences("shortcut",0).getInt("lastStatus",0);
        if(status!=0) { TextView result=new TextView(this); result.setText(getString(R.string.last_result)+" "+getString(status)); box.addView(result); }
        name=field(box,R.string.dot_name,DotSettings.name(this)); name.setId(99);
        int[] titles={R.string.composer,R.string.menu_label,R.string.sidebar_label,R.string.up_label,R.string.call_label,R.string.connected_label};
        for(int i=0;i<fields.length;i++) { fields[i]=field(box,titles[i],CallSettings.get(this,i)); fields[i].setId(100+i); }
        TextView iconTitle=new TextView(this); iconTitle.setText(R.string.icon); box.addView(iconTitle);
        icons=new Spinner(this); icons.setId(201);
        icons.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,getResources().getStringArray(R.array.icons)));
        for(int i=0;i<IconSettings.CHOICES.length;i++)if(IconSettings.CHOICES[i].id.equals(IconSettings.current(this).id))icons.setSelection(i);
        box.addView(icons);
        consent=new CheckBox(this); consent.setId(200); consent.setText(R.string.consent);
        consent.setChecked(getSharedPreferences("shortcut",0).getBoolean("consent",false)); box.addView(consent);
        button(box,R.string.save,()->save());
        button(box,R.string.save_open,()-> {if(save()) { startActivity(new Intent(this,LauncherActivity.class)); finish(); }});
        button(box,R.string.access,()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        button(box,R.string.cancel,()-> { if(ZipService.instance!=null)ZipService.instance.stop(R.string.cancelled);
            else getSharedPreferences("shortcut",0).edit().remove("startedAtElapsed").remove("armedUntil").apply(); });
        scroll.addView(box); setContentView(scroll);
    }
    EditText field(LinearLayout box,int label,String value) {
        TextView title=new TextView(this); title.setText(label); box.addView(title);
        EditText edit=new EditText(this); edit.setSingleLine(true); edit.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING); edit.setContentDescription(getString(label)); edit.setText(value); box.addView(edit); return edit;
    }
    void button(LinearLayout box,int title,Runnable action) { Button b=new Button(this); b.setText(title); b.setOnClickListener(v->action.run()); box.addView(b); }
    boolean save() {
        String[] values=new String[fields.length]; for(int i=0;i<fields.length;i++)values[i]=fields[i].getText().toString().trim();
        if(name.getText().toString().trim().isEmpty() || !CallSettings.valid(values) || !consent.isChecked()) {
            Toast.makeText(this,R.string.invalid,Toast.LENGTH_LONG).show(); return false;
        }
        if(ZipService.instance!=null && ZipService.instance.running)ZipService.instance.stop(R.string.cancelled);
        android.content.SharedPreferences.Editor edit=getSharedPreferences("shortcut",0).edit();
        for(int i=0;i<values.length;i++)edit.putString(CallSettings.KEYS[i],values[i]);
        edit.putBoolean("consent",true).apply(); DotSettings.save(this,name.getText().toString());
        try { IconSettings.save(this,IconSettings.CHOICES[icons.getSelectedItemPosition()]); }
        catch(IllegalArgumentException | SecurityException e) { Toast.makeText(this,R.string.icon_failed,Toast.LENGTH_LONG).show(); return false; }
        Toast.makeText(this,R.string.saved,Toast.LENGTH_SHORT).show(); return true;
    }
}
