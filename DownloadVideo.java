package org.example;

import java.io.IOException;

public interface DownloadVideo {

    public abstract void DownloadVideoBaseUrl(String Url, String videoId) throws InterruptedException, IOException;

}
