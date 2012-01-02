package org.geometerplus.android.fbreader.quickactionbar;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import de.upb.android.reader.R;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * QuickActionBar, shows action list as text.
 */
public class QuickActionBar extends PopupWindow {
	private ZLTextView view;
	private View rootView;
	private ImageView arrowUp;
	private ImageView arrowDown;
	private LayoutInflater inflater;
	private ViewGroup track;
	private ScrollView scroller;
	private View container;
	private OnQuickActionItemClickListener itemClickListener;
	protected WindowManager windowManager;
	protected Drawable background = null;
	protected ZLApplication application;
	
	private List<QuickActionItem> actionItems = new ArrayList<QuickActionItem>();
	
	private boolean didAction;
	
	private int childPos;
    private int insertPos;
    private int animStyle;
    private int orientation;
    private int rootWidth=0;
	
    /**
     * Constructor for horizontal layout
     * 
     * @param context  Context
     */
	public QuickActionBar(Context context, FBReaderApp application) {
		super(context);
		this.view = application.getTextView();
		this.application = application;
		
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		setRootViewId(R.layout.popup_horizontal);
		
		childPos 	= 0;
	}
	
	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	void setRootViewId(int id) {
		rootView	= inflater.inflate(id, null);
		track 		= (ViewGroup) rootView.findViewById(R.id.tracks);

//		arrowDown 	= (ImageView) rootView.findViewById(R.id.arrow_down);
//		arrowUp 	= (ImageView) rootView.findViewById(R.id.arrow_up);

		scroller	= (ScrollView) rootView.findViewById(R.id.scroller);
		
		rootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		setContentView(rootView);
	}
	
	 /**
     * Get action item at an index
     * 
     * @param index  Index of item (position from callback)
     * 
     * @return  Action Item at the position
     */
    public QuickActionItem getActionItem(int index) {
        return actionItems.get(index);
    }
    
    /**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnQuickActionItemClickListener(OnQuickActionItemClickListener listener) {
		itemClickListener = listener;
	}
	
	/**
	 * Add action item
	 * 
	 * @param action  {@link ActionItem}
	 */
	public void addQuickActionItem(final QuickActionItem action) {
		actionItems.add(action);
		
		String title 	= action.getTitle();
		
		container = inflater.inflate(R.layout.action_item_horizontal, null);
		
		TextView text 	= (TextView) container.findViewById(R.id.tv_title);
		
		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}
		
		final int pos = childPos;
		final String actionId = action.getActionId();
		
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				application.doAction(actionId);
				if (action.IsCloseButton) {
					application.hideActivePopup();
					dismiss();
				}
				
//				if (!getActionItem(pos).isSticky()) {  
//                	didAction = true;
//                	
//                    dismiss();
//                }
				
//				if (itemClickListener != null) {
//                    itemClickListener.onItemClick(QuickActionBar.this, pos, actionId);
//                }
//				
//                
			}
		});
		
//		container.setFocusable(true);
//		container.setClickable(true);
			 
		if (childPos != 0) {
            View separator = inflater.inflate(R.layout.horiz_separator, null);
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
            
            separator.setLayoutParams(params);
            separator.setPadding(5, 0, 5, 0);
            
            track.addView(separator, insertPos);
            
            insertPos++;
        }
		
		track.addView(container, insertPos);
		
		childPos++;
		insertPos++;
	}
	
	/**
	 * Add action item with Annotation
	 * 
	 * @param action  {@link ActionItem}
	 */
	public void addQuickActionItemWithAnnotation(final QuickActionItem action, final Annotation annotation) {
		actionItems.add(action);
		
		String title 	= action.getTitle();
		
		container = inflater.inflate(R.layout.action_item_horizontal, null);
		
		TextView text 	= (TextView) container.findViewById(R.id.tv_title);
		
		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}
		
		final int pos = childPos;
		final String actionId = action.getActionId();
		
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				application.doAction(actionId, annotation);
				if (action.IsCloseButton) {
					application.hideActivePopup();
					dismiss();
				}
				
//				if (!getActionItem(pos).isSticky()) {  
//                	didAction = true;
//                	
//                    dismiss();
//                }
				
//				if (itemClickListener != null) {
//                    itemClickListener.onItemClick(QuickActionBar.this, pos, actionId);
//                }
//				
//                
			}
		});
		
//		container.setFocusable(true);
//		container.setClickable(true);
			 
		if (childPos != 0) {
            View separator = inflater.inflate(R.layout.horiz_separator, null);
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
            
            separator.setLayoutParams(params);
            separator.setPadding(5, 0, 5, 0);
            
            track.addView(separator, insertPos);
            
            insertPos++;
        }
		
		track.addView(container, insertPos);
		
		childPos++;
		insertPos++;
	}
	
	/**
	 * Show quickactionbar. Popup is automatically positioned.
	 * 
	 */
	public void show (View anchor) {
		//#################################
		if (rootView == null) 
			throw new IllegalStateException("setContentView was not called with a view to display.");
	
//		onShow();

		if (background == null) 
			this.setBackgroundDrawable(new BitmapDrawable());
		else 
			this.setBackgroundDrawable(background);

		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setTouchable(true);
//		this.setFocusable(true);
//		this.setOutsideTouchable(false);

		this.setContentView(rootView);
		//#################################
		
		int xPos, yPos, arrowPos;
		
		didAction = false;
		
		int[] location = new int[2];
	
		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());
		System.out.println(location[0]);
		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		int rootHeight = rootView.getMeasuredHeight();
		
		if (rootWidth == 0) {
			rootWidth = rootView.getMeasuredWidth();
		}
		
		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		int screenHeight	= windowManager.getDefaultDisplay().getHeight();
		
		xPos = (screenWidth / 2) - (rootWidth / 2);
//		arrowPos = rootWidth/2;
		
		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;

		boolean onTop = (dyTop > dyBottom) ? true : false;
		
		yPos = view.getSelectionStartY() - 15;
		
//		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
		
//		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		this.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		
		this.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		this.update(xPos, yPos, -1, -1);
	}
	
	public void show (View anchor, int x, int y) {
		//#################################
		if (rootView == null) 
			throw new IllegalStateException("setContentView was not called with a view to display.");
	
		if (background == null) 
			this.setBackgroundDrawable(new BitmapDrawable());
		else 
			this.setBackgroundDrawable(background);

		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setTouchable(true);

		this.setContentView(rootView);
		//#################################
		
		int xPos, yPos, arrowPos;
		
		didAction = false;
		
		int[] location = new int[2];
	
		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());
		System.out.println(location[0]);
		
		rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	
		int width = this.getWidth();
		Log.v("QuickActionBar", "width2: "+rootView.getMeasuredWidth());
		
		int rootHeight = rootView.getMeasuredHeight();
		
		if (rootWidth == 0) {
			rootWidth = rootView.getMeasuredWidth();
		}
		
		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		int screenHeight	= windowManager.getDefaultDisplay().getHeight();
		
		xPos = (screenWidth / 2) - (rootWidth / 2);
//		arrowPos = rootWidth/2;
		
		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;

		boolean onTop = (dyTop > dyBottom) ? true : false;
		
		yPos = y - 15;
		
//		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
		
		this.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		
		this.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		this.update(xPos, yPos, -1, -1);
	}
	
	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
//	private void showArrow(int whichArrow, int requestedX) {
//        final View showArrow = (whichArrow == R.id.arrow_up) ? arrowUp : arrowDown;
//        final View hideArrow = (whichArrow == R.id.arrow_up) ? arrowDown : arrowUp;
//
//        final int arrowWidth = arrowUp.getMeasuredWidth();
//
//        showArrow.setVisibility(View.VISIBLE);
//        
//        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
//       
//        param.leftMargin = requestedX - arrowWidth / 2;
//        
//        hideArrow.setVisibility(View.INVISIBLE);
//    }
	
	/**
	 * Listener for item click
	 *
	 */
	public interface OnQuickActionItemClickListener {
		public abstract void onItemClick(QuickActionBar source, int pos, String actionId);
	}

}

