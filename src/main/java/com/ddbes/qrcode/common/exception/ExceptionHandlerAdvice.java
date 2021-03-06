package com.ddbes.qrcode.common.exception;

import com.ddbes.qrcode.common.model.R;
import com.ddbes.qrcode.common.model.Result;
import com.ddbes.qrcode.common.util.JsonKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by daitian on 2018/4/17.
 */
@ControllerAdvice
@ResponseBody
public class ExceptionHandlerAdvice implements ResponseBodyAdvice {
    private Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);
    private ThreadLocal<Object> modelHolder = new ThreadLocal<>();


    //自定义异常
    @ExceptionHandler(MyException.class)
    public Result myTestException(MyException e, HttpServletRequest request) {
        logger.error("{}", request.getRequestURI());
        return new Result(e.getR());
    }

//    @ExceptionHandler(RequiredTypeException.class)
//    public Result requiredTypeException(RequiredTypeException e, HttpServletRequest request) {
//        logger.error("{}", request.getRequestURI());
//        return new Result(R.TOKEN_FAIL);
//    }

    //@valid校验参数不正确异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result MethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        StringBuffer sb = new StringBuffer("请求参数错误");
        errors.forEach((k) -> sb.append(" | ").append(k.getDefaultMessage()));
        logger.error("uri={} | requestBody={}", request.getRequestURI(), JsonKit.toJson(modelHolder.get()));
        logger.error("{}", sb.toString());
        return new Result(sb.toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleException(HttpMessageNotReadableException e, HttpServletRequest request) {
        logger.error("uri={} | {}", request.getRequestURI(), "JSON格式错误!");
        return new Result("JSON格式错误!");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        logger.error("uri={} | requestBody={}", request.getRequestURI(), JsonKit.toJson(modelHolder.get()), e);
        return new Result(R.NET_ERROR);
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        modelHolder.set(webDataBinder.getTarget());
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        modelHolder.remove();
        return o;
    }
}
