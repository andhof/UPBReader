package org.geometerplus.android.fbreader.annotation;

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
	public Object getItem(int position) {
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
        TextView tvContact = (TextView) convertView.findViewById(R.id.tvContact);
        tvContact.setText("BlaBla");

        TextView tvPhone = (TextView) convertView.findViewById(R.id.tvMobile);
        tvPhone.setText("BlaBla2");

        TextView tvMail = (TextView) convertView.findViewById(R.id.tvMail);
        tvMail.setText("BlaBla3");

        return convertView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}