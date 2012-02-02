package org.geometerplus.android.fbreader.annotation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.geometerplus.android.fbreader.annotation.model.Annotation;

import de.upb.android.reader.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CommentAdapter extends BaseAdapter implements OnItemClickListener {
	private Context context;
	
	private List<Annotation> listOfComments;

    public CommentAdapter(Context context, List<Annotation> listOfComments) {
        this.context = context;
        this.listOfComments = listOfComments;
    }
	
	@Override
	public int getCount() {
		return listOfComments.size();
	}

	@Override
	public Annotation getItem(int position) {
		return listOfComments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
//		Annotation entry = listOfComments.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.annotation_comment_row, null);
        }
        
        Annotation comment = listOfComments.get(position);
        
        TextView tvNumber = (TextView) convertView.findViewById(R.id.comment_number);
        tvNumber.setText("#" + (position+1));
        
        TextView tvAuthor = (TextView) convertView.findViewById(R.id.comment_author);
        tvAuthor.setText(comment.getAuthor().getName());
        
        TextView tvUpdatedAt = (TextView) convertView.findViewById(R.id.comment_updated_at);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzzz");
        Date date = null;
        try {
        	String dateString;
			if (comment.getUpdatedAt().isEmpty()) {
				date = new Date(comment.getModified());
				TextView tvLocal = (TextView) convertView.findViewById(R.id.comment_local);
				tvLocal.setText(R.string.shownote_local);
			} else {
				date = format.parse(comment.getUpdatedAt());
			}
        	tvUpdatedAt.setText(date.toString());
        	
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        
        TextView tvContent = (TextView) convertView.findViewById(R.id.comment_content);
        tvContent.setText(comment.getAnnotationContent().getAnnotationText());
        
        return convertView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}