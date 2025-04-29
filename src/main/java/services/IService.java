package services;
import models.User;

import java.util.List;
public interface IService <T> {
    void Create(T t) throws Exception;
    void update(T t) throws Exception;
    void delete(T t) throws Exception;
    List<T> DisplayAll() throws Exception;

}
