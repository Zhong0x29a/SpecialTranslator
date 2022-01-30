package cc0x29a.specialtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ScrollView svSelectLang;
    RadioGroup rgLangGroup;
    RadioButton rbSelectAuto,rbSelectEn,rbSelectCn;

    Button btnSelectSouLang,btnSelectDesLang;
    Button btnSwapSDLang;

    EditText etTextToTrans;
    Button btnStartTrans;
    TextView tvTextOutput;

//    Button btnMenu;
    Button btnUseBaidu,btnUseYoudao;

    String fromLang,toLang;
    String API;

    // languages set
    HashMap id2lang=new HashMap<Integer,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // todo: save "last status"
        // set default sourceLang & DestinyLang
        fromLang="en";
        toLang="zh";
        API="baidu";

        // Select Language section.
        id2lang.put(R.id.mainAct_Select_Auto,"auto");
        id2lang.put(R.id.mainAct_Select_English,"en");
        id2lang.put(R.id.mainAct_Select_Chinese,"zh");

        svSelectLang=findViewById(R.id.mainAct_SelectLang);
        rgLangGroup=findViewById(R.id.mainAct_LangRadioGroup);
        rbSelectAuto=findViewById(R.id.mainAct_Select_Auto);
        rbSelectEn=findViewById(R.id.mainAct_Select_English);
        rbSelectCn=findViewById(R.id.mainAct_Select_Chinese);

        // Click to chang language section.
        btnSelectSouLang=findViewById(R.id.mainAct_SouLang);
        btnSelectDesLang=findViewById(R.id.mainAct_DesLang);
        btnSwapSDLang=findViewById(R.id.mainAct_SwpSDLang);

        // Inout and output section.
        etTextToTrans=findViewById(R.id.mainAct_TextToTrans);
        btnStartTrans=findViewById(R.id.mainAct_StartTrans);
        tvTextOutput=findViewById(R.id.mainAct_TextOutput);

        // TopBar section.
//        btnMenu=findViewById(R.id.mainAct_MenuBtn);
        btnUseBaidu=findViewById(R.id.mainAct_MenuUseBaiduAPI);
        btnUseYoudao=findViewById(R.id.mainAct_MenuUseYoudaoAPI);

        // Bind Listener.

        rgLangGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            // set the lang.
            try {
                if (svSelectLang.getTag().toString().equals("SourceLang")) {
                    fromLang = Objects.requireNonNull(id2lang.get(i)).toString();
                    btnSelectSouLang.setText(fromLang);
                } else if (svSelectLang.getTag().toString().equals("DestinyLang")) {
                    toLang = Objects.requireNonNull(id2lang.get(i)).toString();
                    btnSelectDesLang.setText(toLang);
                }
            }catch (NullPointerException e){
                System.err.println(e.getMessage());
            }

            // hide the scroll view
            showOrHideRadioGroupAuto("no");
//            setRadioGroupChecked(false,fromLang);
        });

        // click to select another language.
        btnSelectSouLang.setOnClickListener(cliListenerChangeLang);
        btnSwapSDLang.setOnClickListener(cliListenerChangeLang);
        btnSelectDesLang.setOnClickListener(cliListenerChangeLang);

        // open/close menu
        findViewById(R.id.mainAct_MenuBtn).setOnClickListener(view -> {
            openOrCloseMenu();
        });
        findViewById(R.id.mainAct_TopBarMenu).setOnClickListener(view -> {
            openOrCloseMenu();
        });

        // select API
        btnUseBaidu.setOnClickListener(cliListenerChangeAPI);
        btnUseYoudao.setOnClickListener(cliListenerChangeAPI);

        // Start Translation.
        btnStartTrans.setOnClickListener(view -> new Thread( () -> {
            String textToTrans=etTextToTrans.getText().toString();

            String textOutput;
            switch (API) {
                case "baidu":
                    textOutput = TranslateAPI.BaiduAPI.translate(fromLang, toLang, textToTrans);
                    break;
                case "youdao":
                    textOutput = TranslateAPI.YouDaoAPI.translate(fromLang, toLang, textToTrans);
                    break;
                default:
                    textOutput = TranslateAPI.BaiduAPI.translate(fromLang, toLang, textToTrans);
            }
            setTvTextOutputCont(textOutput);
        }).start());

        new Thread(()->{
            String[] strArr=EveryDayAPI.EverydaySentence.fetchEverydaySentence();
            setTvTextStcEvrDay(strArr[0],strArr[1]);
        }).start();

    }

    //
    @SuppressLint("NonConstantResourceId")
    View.OnClickListener cliListenerChangeLang = view -> {
        switch (view.getId()){
            case R.id.mainAct_SouLang:
                // edit the tag
                svSelectLang.setTag("SourceLang");
                showOrHideRadioGroupAuto("from");
                break;
            case R.id.mainAct_SwpSDLang:
                if( !(fromLang.equals("auto")) ){
                    svSelectLang.setVisibility(View.GONE);
                    svSelectLang.setTag(null);
                    String temp = fromLang;
                    fromLang = toLang;
                    toLang = temp;
                }
                break;
            case R.id.mainAct_DesLang:
                svSelectLang.setTag("DestinyLang");
                showOrHideRadioGroupAuto("to");
                break;
        }
        btnSelectSouLang.setText(fromLang);
        btnSelectDesLang.setText(toLang);
    };

    View.OnClickListener cliListenerChangeAPI = view -> {
        switch (view.getId()){
            case R.id.mainAct_MenuUseBaiduAPI:
                API="baidu";
                break;
            case R.id.mainAct_MenuUseYoudaoAPI:
                API="youdao";
                break;
            default:
                API="baidu";
        }
        openOrCloseMenu();
    };

    // Show/hide Checkbox.
    //todo: may no use? or simplify?
    /**
     * use to set the visibility of the radio group.
     * @param isDestLang    if true, set "auto" uncheckable.
     * @param checkedOne    set which "were" selected.
     */
    public void setRadioGroupChecked(boolean isDestLang,String checkedOne){
        rbSelectAuto.setClickable(!isDestLang);
        switch (checkedOne){
            case "auto":
                rbSelectAuto.setChecked(true);
                break;
            case "en":
                rbSelectEn.setChecked(true);
                break;
            case "zh":
                rbSelectCn.setChecked(true);
                break;
        }
    }

    public void showOrHideRadioGroupAuto(String fromOrTo){
        if(svSelectLang.getVisibility()==View.VISIBLE){
            svSelectLang.setVisibility(View.GONE);
        }else{
            if(fromOrTo.equals("from")) {
                setRadioGroupChecked(false, fromLang);
            }else if(fromOrTo.equals("to")){
                setRadioGroupChecked(true,toLang);
            }
            svSelectLang.setVisibility(View.VISIBLE);
        }
    }

    public void openOrCloseMenu(){
//        mainAct_mainContainLayout
        LinearLayout l=findViewById(R.id.mainAct_TopBarMenu);
        if(l.getVisibility()==View.VISIBLE){
            l.setVisibility(View.GONE);
        }else{
            l.setVisibility(View.VISIBLE);
        }
    }

    // Set the Output view new content
    public void setTvTextOutputCont(String text){
        MainActivity.this.runOnUiThread(() -> tvTextOutput.setText(text+"\n\n--by "+API));
    }

    // Display the Everyday Sentence.
    public void setTvTextStcEvrDay(String textEng,String textZh){
        MainActivity.this.runOnUiThread(() -> {
            TextView tv=findViewById(R.id.mainAct_StcEvrDay);
            tv.setText("Everyday sentence: \n\n    "+textEng+"\n    "+textZh);
        });
    }

}