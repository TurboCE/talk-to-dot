package local.zipshortcut;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;

public class SettingsActivity extends Activity {
    EditText name,entry;
    EditText[] fields=new EditText[CallSettings.KEYS.length];
    CheckBox consent;
    Spinner icons;
    TextView inspection,lastResult;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this); scroll.setFitsSystemWindows(true);
        LinearLayout box=new LinearLayout(this); box.setOrientation(1);
        int pad=(int)(24*getResources().getDisplayMetrics().density); box.setPadding(pad,pad,pad,pad);
        TextView title=new TextView(this); title.setText(R.string.configure_dot); title.setTextSize(26); box.addView(title);
        button(box,R.string.apply_fold_profile,()-> {
            if(!CallSettings.applyObservedFoldProfile(this)) {Toast.makeText(this,R.string.invalid,Toast.LENGTH_LONG).show();return;}
            if(ZipService.instance!=null && ZipService.instance.running)ZipService.instance.stop(ResultStatus.CANCELLED);
            name.setText("dot");entry.setText("Dot");
            for(int i=0;i<fields.length;i++)fields[i].setText(CallSettings.OBSERVED_FOLD_LABELS[i]);
            consent.setChecked(false);Toast.makeText(this,R.string.saved_disabled,Toast.LENGTH_LONG).show();
        });
        TextView help=new TextView(this); help.setText(R.string.setup_help); box.addView(help);
        lastResult=new TextView(this);box.addView(lastResult);
        inspection=new TextView(this);inspection.setContentDescription(getString(R.string.inspect_report));
        inspection.setTextIsSelectable(true);box.addView(inspection);
        name=field(box,R.string.dot_name,DotSettings.name(this)); name.setId(99);
        entry=field(box,R.string.dot_entry,getSharedPreferences("shortcut",0).getString("dotEntry",""));entry.setId(106);
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
        button(box,R.string.save_open,()-> {if(save() && consent.isChecked()) { startActivity(new Intent(this,LauncherActivity.class)); finish(); }});
        button(box,R.string.inspect_controls,()-> {
            if(ZipService.instance==null) {Toast.makeText(this,R.string.inspect_enable,Toast.LENGTH_LONG).show();return;}
            Intent chat=getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
            if(chat==null) {Toast.makeText(this,R.string.missing_app,Toast.LENGTH_LONG).show();return;}
            ZipService.instance.inspectOnce();startActivity(chat);
        });
        button(box,R.string.inspect_menu,()-> {
            if(ZipService.instance==null) {Toast.makeText(this,R.string.inspect_enable,Toast.LENGTH_LONG).show();return;}
            Intent chat=getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
            if(chat==null) {Toast.makeText(this,R.string.missing_app,Toast.LENGTH_LONG).show();return;}
            ZipService.instance.inspectMenuOnce(entry.getText().toString().trim().isEmpty()?name.getText().toString():entry.getText().toString());startActivity(chat);
        });
        button(box,R.string.inspect_entry,()-> {
            if(ZipService.instance==null) {Toast.makeText(this,R.string.inspect_enable,Toast.LENGTH_LONG).show();return;}
            Intent chat=getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
            if(chat==null) {Toast.makeText(this,R.string.missing_app,Toast.LENGTH_LONG).show();return;}
            ZipService.instance.inspectEntryOnce();startActivity(chat);
        });
        button(box,R.string.access,()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        button(box,R.string.cancel,()-> { if(ZipService.instance!=null)ZipService.instance.stop(ResultStatus.CANCELLED);
            else CallSettings.clearRequest(getSharedPreferences("shortcut",0).edit()).commit(); });
        scroll.addView(box); setContentView(scroll);
    }
    @Override protected void onResume() {
        super.onResume();
        if(lastResult!=null) {
            int status=ResultStatus.message(getSharedPreferences("shortcut",0).getString("lastResult",""));
            lastResult.setText(status==0?"":getString(R.string.last_result)+" "+getString(status));
            lastResult.setVisibility(status==0?android.view.View.GONE:android.view.View.VISIBLE);
        }
        if(inspection!=null) {
            inspection.setText(ZipService.inspectionReport);
            inspection.setVisibility(ZipService.inspectionReport.isEmpty()?android.view.View.GONE:android.view.View.VISIBLE);
            if(!ZipService.inspectionReport.isEmpty())inspection.post(()-> {
                inspection.setFocusableInTouchMode(true);inspection.requestFocus();
                inspection.requestRectangleOnScreen(new android.graphics.Rect(0,0,inspection.getWidth(),inspection.getHeight()),false);
            });
        }
    }
    EditText field(LinearLayout box,int label,String value) {
        TextView title=new TextView(this); title.setText(label); box.addView(title);
        EditText edit=new EditText(this); edit.setSingleLine(true); edit.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING); edit.setContentDescription(getString(label)); edit.setText(value); box.addView(edit); return edit;
    }
    void button(LinearLayout box,int title,Runnable action) { Button b=new Button(this); b.setText(title); b.setOnClickListener(v->action.run()); box.addView(b); }
    boolean save() {
        String[] values=new String[fields.length]; for(int i=0;i<fields.length;i++)values[i]=fields[i].getText().toString().trim();
        if(!CallSettings.store(this,name.getText().toString(),values,consent.isChecked(),entry.getText().toString())) {
            Toast.makeText(this,R.string.invalid,Toast.LENGTH_LONG).show(); return false;
        }
        if(ZipService.instance!=null && ZipService.instance.running)ZipService.instance.stop(ResultStatus.CANCELLED);
        if(!consent.isChecked()) {
            Toast.makeText(this,CallSettings.complete(values,name.getText().toString(),entry.getText().toString())?R.string.saved_disabled:R.string.consent_revoked,Toast.LENGTH_LONG).show(); return true;
        }
        try { IconSettings.save(this,IconSettings.CHOICES[icons.getSelectedItemPosition()]); }
        catch(IllegalArgumentException | SecurityException e) { Toast.makeText(this,R.string.icon_failed,Toast.LENGTH_LONG).show(); return false; }
        Toast.makeText(this,R.string.saved,Toast.LENGTH_SHORT).show(); return true;
    }
}
