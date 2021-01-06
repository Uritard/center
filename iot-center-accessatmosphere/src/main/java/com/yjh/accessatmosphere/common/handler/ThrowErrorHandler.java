package com.yjh.accessatmosphere.common.handler;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.util.Scanner;

public class ThrowErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        // 返回false表示不管response的status是多少都返回没有错
        // 这里可以自己定义那些status code你认为是可以抛Error
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // 这里面可以实现你自己遇到了Error进行合理的处理
        try(Scanner scanner = new Scanner(response.getBody()).useDelimiter("\\A")){
            String stringResponse = scanner.hasNext() ? scanner.next() : "";
            if(stringResponse.matches(".*XXX.*")){
                throw new MyException(stringResponse);
            }
            else{
                super.handleError(response);
            }
        }
    }

}
