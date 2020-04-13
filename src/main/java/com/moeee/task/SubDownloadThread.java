package com.moeee.task;

import com.moeee.model.FileDTO;
import com.moeee.util.HttpUtil;
import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Title: SubDownloadThread<br>
 * Description: 多线程下载线程<br>
 * Create DateTime: 2020年04月10日<br>
 *
 * @author MoEee
 */
public class SubDownloadThread extends Thread {

    public SubDownloadThread(FileDTO fileDTO, AtomicInteger workDone, String referer, Long startIndex, Long endIndex) {
        this.fileDTO = fileDTO;
        this.workDone = workDone;
        this.referer = referer;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    private AtomicInteger workDone;
    private FileDTO fileDTO;
    private String referer;
    private Long startIndex;
    private Long endIndex;

    @Override
    public void run() {
        try {
            HttpURLConnection httpURLConnection = HttpUtil
                .getTaskConnection(fileDTO.getUrl(), referer, startIndex, endIndex);
            RandomAccessFile raf = new RandomAccessFile(fileDTO.getPath(), "rwd");
            raf.seek(startIndex);
            BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
            byte[] buf = new byte[8192];
            int length;
            //保存文件
            while ((length = bis.read(buf)) != -1) {
                raf.write(buf, 0, length);
                workDone.addAndGet(length);
            }
            raf.close();
            bis.close();
        } catch (Exception e) {
        }
    }
}
