package com.moeee.task;

import com.moeee.model.FileDTO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Title: DownloadThread<br>
 * Description: 文件下载线程<br>
 * Create DateTime: 2020年04月10日<br>
 *
 * @author MoEee
 */
public class DownloadThread extends Thread {

    private static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();

    public DownloadThread(FileDTO fileDTO, AtomicInteger workDone, String referer, AtomicInteger finishedTaskCount) {
        this.fileDTO = fileDTO;
        this.workDone = workDone;
        this.referer = referer;
        this.finishedTaskCount = finishedTaskCount;
    }

    private AtomicInteger workDone;
    private FileDTO fileDTO;
    private String referer;
    private AtomicInteger finishedTaskCount;

    @Override
    public void run() {
        // 多线程下载
        Long startIndex = 0L;
        Long endIndex;
        ExecutorService executorService = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 2, TimeUnit.HOURS,
            new LinkedBlockingDeque<>(8));
        Long blockSize = fileDTO.getTotalSize() / THREAD_NUM;
        for (int i = 0; i < THREAD_NUM; i++) {
            if (i == THREAD_NUM - 1) {
                executorService.submit(new SubDownloadThread(fileDTO, workDone, referer, startIndex, fileDTO.getTotalSize()));
            } else {
                endIndex = startIndex + blockSize - 1;
                executorService.submit(new SubDownloadThread(fileDTO, workDone, referer, startIndex, endIndex));
                startIndex += blockSize;
            }
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }
        finishedTaskCount.addAndGet(1);
    }
}
