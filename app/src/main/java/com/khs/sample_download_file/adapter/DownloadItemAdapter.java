package com.khs.sample_download_file.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.khs.sample_download_file.R;

import java.util.List;

/**
 * Created by khs on 12/22/2020.
 */

public class DownloadItemAdapter extends BaseAdapter {

    private List<DownloadItem> downloadItemList = null;

    private Context context = null;

    public DownloadItemAdapter(Context ctx, List<DownloadItem> listViewItemDtoList) {
        this.context = ctx;
        this.downloadItemList = listViewItemDtoList;
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(downloadItemList!=null) {
            ret = downloadItemList.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int itemIndex) {
        Object ret = null;
        if(downloadItemList!=null) {
            ret = downloadItemList.get(itemIndex);
        }
        return ret;
    }

    @Override
    public long getItemId(int itemIndex) {
        return itemIndex;
    }

    @Override
    public View getView(int itemIndex, View convertView, ViewGroup viewGroup) {

        DownloadItemViewHolder viewHolder = null;

        if(convertView != null) {
            viewHolder = (DownloadItemViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.item_listview, null);

            CheckBox listItemCheckbox = (CheckBox)convertView.findViewById(R.id.list_view_item_checkbox);
            TextView listItemText = (TextView)convertView.findViewById(R.id.list_view_item_text);

            viewHolder = new DownloadItemViewHolder(convertView);
            viewHolder.setItemCheckbox(listItemCheckbox);
            viewHolder.setItemTextView(listItemText);
            convertView.setTag(viewHolder);
        }

        DownloadItem downloadItem = downloadItemList.get(itemIndex);
        viewHolder.getItemCheckbox().setChecked(downloadItem.isChecked());
        viewHolder.getItemTextView().setText(downloadItem.getItemUri().toString());

        return convertView;
    }
}
