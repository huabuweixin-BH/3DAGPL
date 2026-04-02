package com.threedagpl.common.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.threedagpl.common.utils.http.HttpHelper;
import com.threedagpl.common.constant.Constants;

/**
 * 构建可重复读取 inputStream 的 request
 *
 * @author ruoyi
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper
{
    private static final Logger log = LoggerFactory.getLogger(RepeatedlyRequestWrapper.class);
    
    private final byte[] body;

    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException
    {
        super(request);
        request.setCharacterEncoding(Constants.UTF8);
        response.setCharacterEncoding(Constants.UTF8);

        String bodyString = HttpHelper.getBodyString(request);
        body = bodyString.getBytes(Constants.UTF8);
        
        // 记录 JSON 请求体内容（仅针对/article 接口）
        String uri = request.getRequestURI();
        if (uri.contains("/article") && (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))) {
            log.info("=== 接收到文章提交请求 ===");
            log.info("URI: {}", uri);
            log.info("Method: {}", request.getMethod());
            log.info("请求体内容：{}", bodyString);
            log.info("请求体长度：{} 字节", body.length);
        }
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return bais.read();
            }

            @Override
            public int available() throws IOException
            {
                return body.length;
            }

            @Override
            public boolean isFinished()
            {
                return false;
            }

            @Override
            public boolean isReady()
            {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {

            }
        };
    }
}
