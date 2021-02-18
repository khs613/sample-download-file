package com.khs.sample_download_file.adapter;

import android.net.Uri;

/**
 * Created by khs on 12/22/2020.
 */

public class DownloadItem {

    private boolean checked = false;

    private Uri itemText;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Uri getItemUri() {
        return itemText;
    }

    public void setItemUri(Uri itemText) {
        this.itemText = itemText;
    }
}
