package com.moeee.task;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Title: FinishTaskCountService<br>
 * Description: 绑定显示文件下载总数UI的线程<br>
 * Create DateTime: 2020年04月10日<br>
 *
 * @author MoEee
 */
public class FinishTaskCountService extends Service {

    public FinishTaskCountService(AtomicInteger finishedTaskCount, Integer totalSize) {
        this.finishedTaskCount = finishedTaskCount;
        this.totalSize = totalSize;
    }

    private AtomicInteger finishedTaskCount;
    private Integer totalSize;


    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected String call() throws InterruptedException {
                while (finishedTaskCount.intValue() != totalSize) {
                    updateValue(String.format("%d/%d", finishedTaskCount.intValue(), totalSize));
                    Thread.sleep(500);
                }
                return String.format("%d个视频下载完成!", totalSize);
            }
        };
    }
}
