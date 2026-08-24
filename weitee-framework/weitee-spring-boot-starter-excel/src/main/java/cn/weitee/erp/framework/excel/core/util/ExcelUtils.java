package cn.weitee.erp.framework.excel.core.util;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import cn.weitee.erp.framework.common.util.http.HttpUtils;
import cn.weitee.erp.framework.common.util.spring.SpringUtils;
import cn.weitee.erp.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import cn.weitee.erp.framework.excel.core.handler.SelectSheetWriteHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel 工具类
 *
 * @author WeTai
 */
public class ExcelUtils {

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param <T>       泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        // 先生成完整文件，加密成功后才提交 HTTP 响应，避免返回部分明文。
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FastExcelFactory.write(outputStream, head)
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .registerWriteHandler(new SelectSheetWriteHandler(head)) // 基于固定 sheet 实现下拉框
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .sheet(sheetName).doWrite(data);
        byte[] content = outputStream.toByteArray();
        content = protect(content, filename, "application/vnd.ms-excel");
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.getOutputStream().write(content);
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        // 参考 https://t.zsxq.com/zM77F 帖子，增加 try 处理，兼容 windows 场景
        try (InputStream inputStream = file.getInputStream()) {
            return read(inputStream, head);
        }
    }

    public static <T> List<T> read(InputStream inputStream, Class<T> head) throws IOException {
        return FastExcelFactory.read(inputStream, head, null)
                .autoCloseStream(false)
                .doReadAllSync();
    }

    private static byte[] protect(byte[] content, String filename, String contentType) {
        Map<String, FileExportProtector> protectors;
        try {
            protectors = SpringUtils.getApplicationContext().getBeansOfType(FileExportProtector.class);
        } catch (IllegalStateException | NullPointerException ex) {
            // Excel 工具可独立运行，未启动 Spring 容器时不启用扩展保护。
            return content;
        }
        if (protectors.isEmpty()) {
            return content;
        }
        // 保护失败必须向上抛出，不能降级返回明文。
        return protectors.values().iterator().next().protect(content, filename, contentType);
    }

}
