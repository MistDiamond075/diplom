package com.diplom.diplom.exception;

import org.springframework.http.HttpStatus;

public class EntityException extends BaseException{
    private final Object entity;

    public EntityException(HttpStatus status, Integer code, String description, String msg_for_user, Object entity) {
        super(status, code, description, msg_for_user);
        this.entity = entity;
    }

    public EntityException(HttpStatus status, String description, String msg_for_user, Object entity) {
        super(status, description, msg_for_user);
        this.entity = entity;
    }

    public EntityException(HttpStatus status, Integer code, String description, Object entity) {
        super(status, code, description);
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    public String getEntityClassName(){
        return entity.getClass().getName();
    }

    @SuppressWarnings("uncheked")
    public <T> T getEntity(Class<T> type){
        if (type.isInstance(entity)) {
            return type.cast(entity);
        }
        throw new ClassCastException("entity is not of type: " + type.getName());
    }

}
