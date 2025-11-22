package org.example;

import java.io.IOException;

public interface DownloadVideo {

    public abstract void DownloadVideoBaseUrl(String Url) throws InterruptedException, IOException;

}
