package orm.repos;

public interface CRUDrepo<T> {
    void save(T newObj);

}
