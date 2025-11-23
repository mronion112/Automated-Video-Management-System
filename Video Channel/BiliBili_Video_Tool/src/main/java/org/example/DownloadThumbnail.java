package org.example;

import java.io.IOException;

public interface DownloadThumbnail {

    public abstract void DownloadThumbnailBaseUrl(String Url, String thumbnailId) throws InterruptedException, IOException;

}
