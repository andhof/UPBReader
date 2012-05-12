package org.geometerplus.android.fbreader.annotation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.zlibrary.core.util.ZLColor;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import de.upb.android.reader.R;

public class AnnotationAdapter extends BaseAdapter implements OnClickListener {
	private Context context;
	private ListView listView;
	
	private List<Annotation> listOfAnnotations;
	
    private int selectedIndex = -1;

    public AnnotationAdapter(Context context, List<Annotation> listOfAnnotations, ListView listView) {
        this.context = context;
        this.listOfAnnotations = listOfAnnotations;
        this.listView = listView;
    }
	
	@Override
	public int getCount() {
		return listOfAnnotations.size();
	}

	@Override
	public Annotation getItem(int position) {
		return listOfAnnotations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setSelectedIndex(int index) {
	    //some range-checks, maybe
	    selectedIndex = index;
	    //invalidate
	    notifyDataSetChanged();
	}
	
	public int getSelectedIndex() {
	    return selectedIndex;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup) {
		View row = convertView;
        ViewHolder holder;
		if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.annotationlist_item, viewGroup, false);
            
            holder = new ViewHolder();

            holder.radioBtn = (RadioButton) row.findViewById(R.id.radiobtn);
            
            holder.text 	= (TextView) row.findViewById(R.id.annotation_title);
            holder.author = (TextView) row.findViewById(R.id.annotation_author);
    		
    		holder.updated_at = (TextView) row.findViewById(R.id.annotation_updated_at);
    		holder.tags = (TextView) row.findViewById(R.id.annotation_tags);
    		holder.category = (TextView) row.findViewById(R.id.annotation_category);

            row.setTag(holder);
        } else {
        	holder = (ViewHolder) row.getTag();
        }
		
		Annotation annotation = listOfAnnotations.get(position);
		holder.author.setText(context.getText(R.string.shownote_author)+annotation.getAuthor().getName());
		holder.updated_at.setText(annotation.getUpdatedAt());
		holder.tags.setText(context.getText(R.string.shownote_tags)+annotation.getTagsAsString());
		holder.category.setText(context.getText(R.string.shownote_category)+annotation.getCategory());
		
		row.setClickable(true);
		row.setFocusable(true);
		RadioButton radiobtn = (RadioButton) row.findViewById(R.id.radiobtn);
		
		if (position == selectedIndex) {
			radiobtn.setChecked( true );
		}
		else {
			radiobtn.setChecked( false );
		}
		
		row.setOnClickListener(new OnClickListener() {
			
	        @Override
	        public void onClick(View v) {
//	            v.setBackgroundColor(android.R.color.white);
//	            v.setSelected(true);
//	            new AlertDialog.Builder(context).setTitle("touched"+position).show();
//	        	if (lastchecked >= 0) {
//	        		listView.setItemChecked(lastchecked, false);
//	        	}
//	        	new AlertDialog.Builder(context).setTitle("checkeditems: "+listView.getCheckedItemPosition()).show();
//	        	ZLColor color = new ZLColor(255,	0, 0);
//	        	v.setBackgroundColor(color.getIntValue());
//	        	CheckedTextView checkBox = (CheckedTextView) v.findViewById(R.id.checkstate);
//	    		checkBox.setChecked(true);
//	    		listView.setSelection(position);
//	    		lastchecked = position;
	        	((AnnotationListActivity) context).findViewById(R.id.show_annotation_button).setEnabled(true);
	        	((AnnotationListActivity) context).findViewById(R.id.edit_annotation_button).setEnabled(true);
	        	((AnnotationListActivity) context).findViewById(R.id.remove_annotation_button).setEnabled(true);
	    		setSelectedIndex(position);
	        }

	    });
        
        
		
//        TextView tvNumber = (TextView) convertView.findViewById(R.id.comment_number);
//        tvNumber.setText("#" + (position+1));
//        
//        TextView tvAuthor = (TextView) convertView.findViewById(R.id.comment_author);
//        tvAuthor.setText(annotation.getAuthor().getName());
//        
//        TextView tvUpdatedAt = (TextView) convertView.findViewById(R.id.comment_updated_at);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzzz");
//        Date date = null;
//        try {
//        	String dateString;
//			if (annotation.getUpdatedAt().isEmpty()) {
//				date = new Date(annotation.getModified());
//				TextView tvLocal = (TextView) convertView.findViewById(R.id.comment_local);
//				tvLocal.setText(R.string.shownote_local);
//			} else {
//				date = format.parse(annotation.getUpdatedAt());
//			}
//        	tvUpdatedAt.setText(date.toString());
//        	
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//        
//        
//        TextView tvContent = (TextView) convertView.findViewById(R.id.comment_content);
//        tvContent.setText(annotation.getAnnotationContent().getAnnotationText());
		
//		CheckedTextView checkBox = (CheckedTextView) row.findViewById(R.id.checkstate);
//		checkBox.setChecked(false);
		
        return row;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	static class ViewHolder {
        ImageView icon;
        RadioButton radioBtn;
        TextView text;
		TextView author;
		TextView updated_at;
		TextView tags;
		TextView category;
    }
}