package pisada.fallDetector;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawListAdapter extends BaseAdapter {


	private Context context;
	private List<NavDrawerItem> navDrawerItems;
	private int[] greenIcons;
	private int selectedItem;
	
	public NavDrawListAdapter(Context context, List<NavDrawerItem> navDrawerItems, int[] selectedIcons){
		this.context = context;
		this.navDrawerItems = navDrawerItems;
		this.greenIcons = selectedIcons;
	}
	
	public void selectItem(int selectedItem){
        this.selectedItem = selectedItem;
        notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {       
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater)
					context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.drawer_list_item, null);
		}

		ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

		imgIcon.setImageResource(position == selectedItem ? greenIcons[position] : navDrawerItems.get(position).getIcon());        
		txtTitle.setText(navDrawerItems.get(position).getTitle());
		txtTitle.setTypeface(null, position == selectedItem ? Typeface.BOLD : Typeface.NORMAL);
		txtTitle.setTextColor(position == selectedItem? Color.BLACK : context.getResources().getColor(R.color.black_overlay));

		
		return convertView;
	}
	


}
