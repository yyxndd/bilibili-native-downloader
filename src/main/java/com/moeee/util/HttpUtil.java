package com.moeee.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang.StringUtils;

/**
 * Title: HttpUtil<br>
 * Description: 网络请求工具<br>
 * Create DateTime: 2020年04月07日<br>
 *
 * @author MoEee
 */
public class HttpUtil {

    private static final String ENCODING = "UTF-8";
    private static final int MAX_BYTES = 4096 * 2;

    /**
     * 简易get请求<br>
     *
     * @param uri 资源地址
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: java.lang.String
     */
    public static String doGet(String uri) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(uri);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept", "*/*");
            httpURLConnection.setRequestProperty("Origin", "https://www.bilibili.com");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-cn");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0.1 Safari/605.1.15");
            return readInputStream(httpURLConnection.getInputStream());
        } catch (IOException ex) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return StringUtils.EMPTY;
        }
    }

    /**
     * 获取下载任务连接<br>
     *
     * @param uri        资源地址
     * @param referer    请求来源
     * @param startIndex 分段起
     * @param endIndex   分段止
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: java.net.HttpURLConnection
     */
    public static HttpURLConnection getTaskConnection(String uri, String referer, Long startIndex, Long endIndex)
        throws IOException {
        URL url = new URL(uri);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Accept", "*/*");
        httpURLConnection.setRequestProperty("Origin", "https://www.bilibili.com");
        httpURLConnection.setRequestProperty("Host", "upos-hz-mirrorkodou.acgvideo.com");
        httpURLConnection.setRequestProperty("Accept-Encoding", "br, gzip, deflate");
        httpURLConnection.setRequestProperty("Accept-Language", "zh-cn");
        httpURLConnection.setRequestProperty("Referer", referer);
        httpURLConnection.setRequestProperty("Connection", "keep-alive");
        httpURLConnection.setRequestProperty("Range", String.format("bytes=%d-%d", startIndex, endIndex));
        httpURLConnection.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0.1 Safari/605.1.15");
        return httpURLConnection;
    }

    /**
     * 从流读成字符串<br>
     *
     * @param is
     * @Author: MoEee <br>
     * @Date: 2020/4/13 <br>
     * @return: java.lang.String
     */
    private static String readInputStream(InputStream is) throws IOException {
        byte[] b = new byte[MAX_BYTES];
        StringBuilder builder = new StringBuilder();
        int bytesRead;
        while (true) {
            bytesRead = is.read(b, 0, MAX_BYTES);
            if (bytesRead == -1) {
                return builder.toString();
            }
            builder.append(new String(b, 0, bytesRead, ENCODING));
        }
    }
}
