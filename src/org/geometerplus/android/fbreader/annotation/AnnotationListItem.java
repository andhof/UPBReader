package org.geometerplus.android.fbreader.annotation;

public class AnnotationListItem {
	
	private String title;
	private String actionId = null;
	final boolean IsCloseButton;
	private boolean selected;
	private boolean sticky;
	
	/**
	 * Constructor
	 * 
	 * @param actionId	Action id for case statements
	 * @param title			Title
	 */
	public AnnotationListItem(String actionId, boolean isCloseButton, String title) {
		this.actionId = actionId;
		IsCloseButton = isCloseButton;
		this.title = title;
	}
	
	/**
	 * Constructor
	 */
	public AnnotationListItem() {
		this(null, false, null);
	}
	
	/**
	 * Set action title
	 * 
	 * @param title action title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Get action title
	 * 
	 * @return action title
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
     * Set action id
     * 
     * @param actionId  Action id for this action
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
    
    /**
     * @return  Our action id
     */
    public String getActionId() {
        return actionId;
    }
    
    /**
     * Set sticky status of button
     * 
     * @param sticky  true for sticky, pop up sends event but does not disappear
     */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }
    
    /**
     * @return  true if button is sticky, menu stays visible after press
     */
    public boolean isSticky() {
        return sticky;
    }
    
    /**
	 * Set selected flag;
	 * 
	 * @param selected Flag to indicate the item is selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Check if item is selected
	 * 
	 * @return true or false
	 */
	public boolean isSelected() {
		return this.selected;
	}
}