package com.moeee.task;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Title: ProgressService<br>
 * Description: 绑定显示进度条UI的线程<br>
 * Create DateTime: 2020年04月07日<br>
 *
 * @author MoEee
 */
public class ProgressService extends Service {

    public ProgressService(AtomicInteger workDone, Long totalSize) {
        this.workDone = workDone;
        this.totalSize = totalSize;
    }

    private AtomicInteger workDone;
    private Long totalSize;


    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Integer call() throws Exception {
                //保存文件
                while (workDone.intValue() < totalSize) {
                    updateProgress((int) (workDone.intValue() * 1.0 / totalSize * 100), 100);
                    Thread.sleep(500);
                }
                updateProgress((int) (workDone.intValue() * 1.0 / totalSize * 100), 100);
                return 0;
            }
        };
    }
}
