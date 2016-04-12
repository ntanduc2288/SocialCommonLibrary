package vinasource.com.commonlibrary.interfaces;

/**
 * Created by user on 4/12/16.
 */
public interface IFacebookCallback<T, E> {
    public void responseData(T t);
    public void error(E error);
    public void requestCanceled();
}
