package teamqitalach.pillapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by Qinghao on 4/27/2015.
 * From http://stackoverflow.com/questions/20015463/defining-custom-checkbox-in-android
 */
public class CustomCheckBox extends CheckBox {

    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setButtonDrawable(new StateListDrawable());
    }
    @Override
    public void setChecked(boolean t){
        if(t)
        {
            this.setBackgroundResource(R.drawable.checkbox_background);
            this.setTextColor(Color.WHITE);
        }
        else
        {
            this.setBackgroundColor(Color.TRANSPARENT);
            this.setTextColor(Color.BLACK);
        }
        super.setChecked(t);
    }
}
