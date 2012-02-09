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

public class AnnotationListPopup extends PopupWindow {
	
	private ZLTextView view;
	private View rootView;
	private ImageView arrowUp;
	private ImageView arrowDown;
	private LayoutInflater inflater;
	private ViewGroup annotations;
	private ScrollView scroller;
	private View container;
	private OnQuickActionItemClickListener itemClickListener;
	protected WindowManager windowManager;
	protected Drawable background = null;
	protected ZLApplication application;
	
	private List<AnnotationListItem> actionItems = new ArrayList<AnnotationListItem>();
	
	private boolean didAction;
	
	private int childPos;
    private int insertPos;
    private int animStyle;
    private int orientation;
    private int rootWidth=0;
	
    private Context myContext;
	
	/**
     * Constructor
     * 
     * @param context  Context
     */
	public AnnotationListPopup(Context context, FBReaderApp application) {
		super(context);
		myContext = context;
		this.view = application.getTextView();
		this.application = application;
		
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		setRootViewId(R.layout.annotationlist_popup);
		
		childPos 	= 0;
	}
	
	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	void setRootViewId(int id) {
		rootView	= inflater.inflate(id, null);
		annotations 		= (ViewGroup) rootView.findViewById(R.id.annotations);

		scroller	= (ScrollView) rootView.findViewById(R.id.listscroller);
		
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
    public AnnotationListItem getActionItem(int index) {
        return actionItems.get(index);
    }
    
    public void removeAllActionItems() {
    	actionItems.clear();
    	System.out.println();
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
	public void addQuickActionItem(final AnnotationListItem action, final int x, final int y, final Annotation annotation) {
		actionItems.add(action);
		
		String title 	= action.getTitle();
		
		container = inflater.inflate(R.layout.annotationlist_item, null);
		
		TextView text 	= (TextView) container.findViewById(R.id.annotation_title);
		TextView author = (TextView) container.findViewById(R.id.annotation_author);
		author.setText("Autor: "+annotation.getAuthor().getName());
		TextView updated_at = (TextView) container.findViewById(R.id.annotation_updated_at);
		updated_at.setText(annotation.getUpdatedAt());
		TextView tags = (TextView) container.findViewById(R.id.annotation_tags);
		tags.setText("Tags: "+annotation.getTagsAsString());
		TextView category = (TextView) container.findViewById(R.id.annotation_category);
		category.setText("Kategorie: "+annotation.getCategory());
		
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
				application.doAction(actionId, x, y, annotation);
				if (action.IsCloseButton) {
					application.hideActivePopup();
					dismiss();
				}
			}
		});
		
		if (childPos != 0) {
			View separator = inflater.inflate(R.layout.vertical_separator, null);
            
			Log.v("AnnotationListPopup", "widthbla: "+rootView.getMeasuredWidth());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			
			separator.setLayoutParams(params);
            separator.setPadding(0, 5, 0, 5);
//            annotations.measure(View.MeasureSpec.makeMeasureSpec(rootView.findViewById(R.id.annotations).getWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.UNSPECIFIED);
//            this.setWidth(annotations.getMeasuredWidth());
//            this.setHeight(annotations.getMeasuredHeight());
            
//            this.showAtLocation(rootView.findViewById(R.id.annotations), Gravity.CENTER, 0, 0);
            annotations.addView(separator, insertPos);
            
            insertPos++;
        }
		
		int width = container.getWidth();
		Log.v("AnnotationListPopup", "width: "+width);
		annotations.addView(container, insertPos);
		
		childPos++;
		insertPos++;
	}
	
	public void show (View anchor, int x, int y, ArrayList<Annotation> annotationsOnPosition) {
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
		
		int rootHeight = rootView.getMeasuredHeight();
		
		Log.v("AnnotationListPopup", "width2: "+rootView.getMeasuredWidth());
		
		if (rootWidth == 0) {
			rootWidth = rootView.getMeasuredWidth();
		}
		
		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		int screenHeight	= windowManager.getDefaultDisplay().getHeight();
		
		xPos = (screenWidth / 2) - (rootWidth / 2);
		arrowPos = rootWidth/2;
		
		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;

		boolean onTop = (dyTop > dyBottom) ? true : false;
		
		yPos = y - 15;
		
		this.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		
		this.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		this.update(xPos, yPos, -1, -1);
	}
	
	/**
	 * Listener for item click
	 *
	 */
	public interface OnQuickActionItemClickListener {
		public abstract void onItemClick(AnnotationListPopup source, int pos, String actionId);
	}
}