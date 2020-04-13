package com.moeee.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXClippedPane;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.moeee.model.FileDTO;
import com.moeee.task.DownloadThread;
import com.moeee.task.FinishTaskCountService;
import com.moeee.task.ProgressService;
import com.moeee.util.HttpUtil;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang.StringUtils;

/**
 * Title: RootController<br>
 * Description: 主页面控制器<br>
 * Create DateTime: 2020年04月07日<br>
 *
 * @author MoEee
 */
public class RootController {

    /**
     * bv号视频地址正则
     */
    private static final Pattern PATTERN_URL_BV = Pattern
        .compile("^https\\:\\/\\/www\\.bilibili\\.com\\/video\\/BV(\\S+)");
    /**
     * 番剧地址正则 https://www.bilibili.com/bangumi/play/ss32956?t=1352
     */
    private static final Pattern PATTERN_URL_BANGUMI = Pattern
        .compile("^https\\:\\/\\/www\\.bilibili\\.com\\/bangumi\\/play\\/\\S+");
    /**
     * 获取视频信息API(bv号版)
     */
    private static final String API_BILIBILI_BV = "https://api.bilibili.com/x/web-interface/view?bvid=%s";
    /**
     * 视频下载信息API
     */
    private static final String API_BILIBILI_DOWNLOAD = "https://interface.bilibili.com/v2/playurl?%s&sign=%s";
    private static final String PARAMS = "appkey=iVGUTjsxvpLeuDCf&cid=%s&otype=json&qn=%s&type=";
    private static final String SEC = "aHRmhWMLkdeMuILqORnYZocwMBpMEOdt";
    /**
     * 视频质量
     */
    private static final String QUALITY_1080P_PLUS = "112";
    private static final String QUALITY_1080P = "80";
    private static final String QUALITY_720P = "64";
    private static final String QUALITY_480P = "32";
    private static final String QUALITY_360P = "16";

    /**
     * 保存下载的信息
     */
    private static List<JSONObject> downloadInfoList = new ArrayList<>();
    private static List<FileDTO> downloadUrlList = new ArrayList<>();
    /**
     * 文件下载总大小
     */
    private static Long totalLength = 0L;
    /**
     * 清晰度
     */
    private static String quality;
    /**
     * 视频总标题
     */
    private static String title;
    /**
     * 统计下载完成的文件数
     */
    private static AtomicInteger finishedTaskCount = new AtomicInteger(0);

    /**
     * 最顶层pane
     */
    @FXML
    JFXClippedPane pane;
    /**
     * "下载"按钮
     */
    @FXML
    JFXButton btnDownload;
    /**
     * "选择"按钮
     */
    @FXML
    Button btnFileChoose;
    /**
     * 输入地址
     */
    @FXML
    JFXTextField inputUrl;
    /**
     * 文件存放目录
     */
    @FXML
    JFXTextField pathText;

    /**
     * 视频标题
     */
    @FXML
    Label labTitle;
    /**
     * up主名称
     */
    @FXML
    Label labAuthor;
    /**
     * 下载进度条
     */
    @FXML
    ProgressBar progress;
    /**
     * 任务统计
     */
    @FXML
    Label labTaskCount;
    /**
     * 清晰度选择
     */
    @FXML
    JFXRadioButton radio0;
    @FXML
    JFXRadioButton radio1;
    @FXML
    JFXRadioButton radio2;
    @FXML
    JFXRadioButton radio3;
    @FXML
    JFXRadioButton radio4;
    /**
     * 旋转控件
     */
    @FXML
    JFXSpinner spinner;
    /**
     * 是否全集下载
     */
    @FXML
    JFXCheckBox cbDownloadAll;

    /**
     * 初始化<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    public void init() {
        labTaskCount.setVisible(Boolean.FALSE);
    }

    /**
     * 点击下载<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    public void download() {
        if (!beforeDownload()) {
            return;
        }
        showSpinner();
        // 检测目录,因为可能手动改了路径
        File file = new File(pathText.getText());
        if (!file.exists()) {
            if (!file.mkdir()) {
                hideSpinner();
                errorAlert("文件地址有误，请重新选择");
                return;
            }
        }
        if (!file.isDirectory()) {
            hideSpinner();
            errorAlert("请选择一个文件夹");
            return;
        }
        setQuality();
        paddingDownloadInfo();
        // 下载文件统计
        FinishTaskCountService finishTaskCountService = new FinishTaskCountService(finishedTaskCount, downloadUrlList.size());
        labTaskCount.textProperty().bind(finishTaskCountService.valueProperty());
        finishTaskCountService.start();
        labTaskCount.setVisible(Boolean.TRUE);
        AtomicInteger workDone = new AtomicInteger(0);
        ExecutorService executorService = new ThreadPoolExecutor(5, 5, 30, TimeUnit.MINUTES, new LinkedBlockingDeque<>(64));
        for (FileDTO fileDTO : downloadUrlList) {
            executorService.submit(new DownloadThread(fileDTO, workDone, inputUrl.getText(), finishedTaskCount));
        }
        executorService.shutdown();
        // 在一个progressBar总计显示下载进度
        calculateProgress(workDone);
    }

    /**
     * 1、对url进行检查<br>
     * 2、处理url为理想格式<br>
     * 3、获取下载信息<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: Boolean
     */
    private Boolean beforeDownload() {
        downloadInfoList.clear();
        downloadUrlList.clear();
        String requestUrl = inputUrl.getText();
        // 判断是否是b站的番剧连接
        boolean isBangumi = PATTERN_URL_BANGUMI.matcher(requestUrl).matches();
        if (isBangumi) {
            errorAlert("目前暂不支持番剧下载\n"
                + "观看B站正版番剧请前往www.bilibili.com");
            return Boolean.FALSE;
        }
        // 判断是否是b站的连接
        boolean isBv = PATTERN_URL_BV.matcher(requestUrl).matches();
        // 从搜索栏进入的地址处理
        if (requestUrl.contains("?from=")) {
            requestUrl = requestUrl.substring(0, requestUrl.indexOf("?from="));
        }
        // 从手机分享地址点击打开网页的地址处理
        if (requestUrl.contains("/?p=")) {
            requestUrl = requestUrl.replace("/?p=", "?p=");
        }
        // 从历史记录打开的地址处理
        if (requestUrl.contains("?t=")) {
            requestUrl = requestUrl.substring(0, requestUrl.indexOf("?t="));
        }
        // 区别是单p下载还是全集下载
        if (cbDownloadAll.isSelected()) {
            if (requestUrl.contains("?p=")) {
                requestUrl = requestUrl.substring(0, requestUrl.indexOf("?p="));
            }
        }
        // 是否是分p
        boolean containsP = requestUrl.contains("?p=");
        if (!isBv) {
            errorAlert("请复制粘贴使用完整B站视频链接\n"
                + "如果还是不能识别，请联系作者");
            return Boolean.FALSE;
        }
        String infoUrl;
        // BV号
        String bvId;
        int p = 0;
        if (containsP) {
            bvId = requestUrl.substring(requestUrl.indexOf("BV") + 2, requestUrl.indexOf("?p="));
            p = Integer.parseInt(requestUrl.substring(requestUrl.indexOf("?p=") + 3));
        } else {
            bvId = requestUrl.substring(requestUrl.indexOf("BV") + 2);
        }
        infoUrl = String.format(API_BILIBILI_BV, bvId);
        String respond = HttpUtil.doGet(infoUrl);
        // 增加链接获取信息失败后的信息反馈
        if (StringUtils.isBlank(respond) || !JSONObject.parseObject(respond).getString("code").equals("0")) {
            errorAlert("获取视频失败");
            labTitle.setText(StringUtils.EMPTY);
            labAuthor.setText(StringUtils.EMPTY);
            return Boolean.FALSE;
        }
        JSONObject respondJson = JSONObject.parseObject(respond);
        JSONObject data = respondJson.getJSONObject("data");
        title = data.getString("title");
        JSONArray pages = data.getJSONArray("pages");
        if (containsP) {
            // 获取单p信息
            downloadInfoList.add(pages.getJSONObject(p - 1));
            String subTitle = pages.getJSONObject(p - 1).getString("part");
            // 显示标题+分p标题
            labTitle.setText(String.join(" ", title, subTitle));
        } else {
            // 获取全集下载信息
            downloadInfoList.addAll(pages.toJavaList(JSONObject.class));
            // 显示标题
            labTitle.setText(title);
        }
        labAuthor.setText(data.getJSONObject("owner").getString("name"));
        return Boolean.TRUE;
    }

    /**
     * 选择文件保存路径<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    public void chooseFile() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择文件存放文件夹");
        File file = chooser.showDialog(pane.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!file.exists() && !file.isDirectory()) {
            errorAlert("请选择一个文件夹");
        } else {
            // 存入路径
            pathText.setText(file.getAbsolutePath());
            // 允许点击下载
            btnDownload.setDisable(Boolean.FALSE);
        }
    }

    /**
     * 组装具体下载信息<br>
     *
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    private void paddingDownloadInfo() {
        for (JSONObject jsonObject : downloadInfoList) {
            String downloadUrl = String.format(PARAMS, jsonObject.get("cid"), quality, quality);
            String sign = getMd5(downloadUrl + SEC);
            downloadUrl = String.format(API_BILIBILI_DOWNLOAD, downloadUrl, sign);
            JSONObject respondJson = JSONObject.parseObject(HttpUtil.doGet(downloadUrl));
            String subFix;
            // 后缀优先mp4
            if (respondJson.getString("accept_format").contains("mp4")) {
                subFix = ".mp4";
            } else {
                subFix = ".flv";
            }
            JSONObject respond = respondJson.getJSONArray("durl").getJSONObject(0);
            // 文件名称
            String name = jsonObject.getString("part") + subFix;
            // 文件总大小
            totalLength += respond.getLong("size");
            FileDTO fileDTO = new FileDTO(String.join("/", pathText.getText(), name), respond.getString("url"),
                respond.getLong("size"));
            downloadUrlList.add(fileDTO);
        }
    }

    /**
     * 设置清晰度<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    private void setQuality() {
        if (radio0.isSelected()) {
            quality = QUALITY_1080P_PLUS;
        } else if (radio1.isSelected()) {
            quality = QUALITY_1080P;
        } else if (radio2.isSelected()) {
            quality = QUALITY_720P;
        } else if (radio3.isSelected()) {
            quality = QUALITY_480P;
        } else {
            quality = QUALITY_360P;
        }
    }

    /**
     * 计算下载进度<br>
     *
     * @param workDone 已下载的文件大小
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    private void calculateProgress(AtomicInteger workDone) {
        progress.setVisible(Boolean.TRUE);
        ProgressService progressService = new ProgressService(workDone, totalLength);
        progress.progressProperty().bind(progressService.progressProperty());
        progressService.setOnSucceeded(event -> {
            spinner.setVisible(Boolean.FALSE);
            finishedTaskCount.set(0);
            totalLength = 0L;
            // 打开文件夹
            try {
                java.awt.Desktop.getDesktop().open(new File(pathText.getText()));
            } catch (IOException e) {
                errorAlert("打开文件夹失败，请手动打开文件夹");
            }
        });
        progressService.start();
    }

    /**
     * 转圈圈<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    private void showSpinner() {
        spinner.setVisible(Boolean.TRUE);
    }

    /**
     * 不转圈了<br>
     *
     * @param
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    private void hideSpinner() {
        spinner.setVisible(Boolean.FALSE);
    }

    /**
     * 报错提示<br>
     *
     * @param info
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: void
     */
    private void errorAlert(String info) {
        JFXAlert alert = new JFXAlert();
        alert.setTitle("错误");
        alert.setHeaderText(info);
        alert.show();
    }

    /**
     * 获取md5摘要<br>
     *
     * @param str
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: java.lang.String
     */
    private String getMd5(String str) {
        String result = "";
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(str.getBytes("UTF8"));
            byte[] digest = m.digest();
            for (int i = 0; i < digest.length; i++) {
                result += Integer.toHexString((0x000000ff & digest[i]) | 0xffffff00).substring(6);
            }
        } catch (Exception e) {
            errorAlert(e.getMessage());
        }
        return result;
    }
}
