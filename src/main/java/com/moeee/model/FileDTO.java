package com.moeee.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Title: FileDTO<br>
 * Description: 文件DTO<br>
 * Create DateTime: 2020年04月07日<br>
 *
 * @author MoEee
 */
@Data
@AllArgsConstructor
public class FileDTO {

    /**
     * 文件保存地址
     */
    private String path;

    /**
     * 下载url
     */
    private String url;

    /**
     * 分段大小
     */
    private Long totalSize;
}
