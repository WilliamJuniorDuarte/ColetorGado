package br.com.wjd.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import br.com.wjd.R;


/**
 * Created by SAG2017 on 10/08/2017.
 */

public class RecyclerViewSeparator extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public RecyclerViewSeparator( Context context ) {
        mDivider = context.getResources().getDrawable( R.drawable.separator_line );
    }

    @Override
    public void onDrawOver( Canvas canvas, RecyclerView parent, RecyclerView.State state ) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        for ( int i = 0; i < parent.getChildCount(); i++ ) {
            View child = parent.getChildAt( i );
            RecyclerView.LayoutParams params = ( RecyclerView.LayoutParams ) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds( left, top, right, bottom );
            mDivider.draw( canvas );
        }
    }

}