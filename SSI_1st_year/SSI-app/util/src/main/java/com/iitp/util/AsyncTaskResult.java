package com.iitp.util;

/**
 * AsyncTask 의 결과
 * @param <T> 결과 객체
 */
public class AsyncTaskResult<T> {
    /** 결과 */
    private T result;

    /** 처리시 에러 */
    private Exception error;

    /**
     * result 로 객체를 생성
     * @param result 결과
     */
    public AsyncTaskResult(T result) {
        this.result = result;
    }

    /**
     * Exception 으로 객체 생성
     * @param error Exception
     */
    public AsyncTaskResult(Exception error) {
        this.error = error;
    }

    /**
     * 에러 확인.<br/> null 이며 처리가 성공임
     * @return Exception
     */
    public Exception getError() {
        return error;
    }

    /**
     * 처리 결과 반환
     * @return 처리 결과
     */
    public T getResult() {
        return result;
    }
}
