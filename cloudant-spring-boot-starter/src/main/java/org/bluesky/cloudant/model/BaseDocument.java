package org.bluesky.cloudant.model;

import java.util.UUID;

public class BaseDocument {
    protected String _id;
    protected String _rev;
    protected boolean _deleted;

    public BaseDocument() {}

    public BaseDocument(String _id) {
        this._id = _id;
    }

    public BaseDocument(String _id, String partition, boolean useAutoId, boolean usePartition) {
        if(useAutoId) {
            _id = generateUUID();
        }
        if(usePartition) {
            this._id= new StringBuilder().append(partition).append(":").append(_id).toString();
        } else {
            this._id = _id;
        }
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getId() {
        return _id;
    }

    public void setRev(String _rev) {
        this._rev = _rev;
    }

    public String getRev() {
        return _rev;
    }

    public void setDeleted(boolean _deleted) {
        this._deleted = _deleted;
    }

    public boolean getDeleted() {
        return _deleted;
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

}
