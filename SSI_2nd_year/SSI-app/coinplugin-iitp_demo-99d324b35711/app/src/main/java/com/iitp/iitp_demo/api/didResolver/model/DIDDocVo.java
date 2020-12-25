package com.iitp.iitp_demo.api.didResolver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DIDDocVo{
    @SerializedName("redirect")
    @Expose
    public Object redirect;
    @SerializedName("didDocument")
    @Expose
    public DidDocumentVo didDocument;
    @SerializedName("resolverMetadata")
    @Expose
    public Object resolverMetadata;
    @SerializedName("methodMetadata")
    @Expose
    public Object methodMetadata;

    public Object getRedirect(){
        return redirect;
    }

    public void setRedirect(Object redirect){
        this.redirect = redirect;
    }

    public DidDocumentVo getDidDocument(){
        return didDocument;
    }

    public void setDidDocument(DidDocumentVo didDocument){
        this.didDocument = didDocument;
    }

    public Object getResolverMetadata(){
        return resolverMetadata;
    }

    public void setResolverMetadata(Object resolverMetadata){
        this.resolverMetadata = resolverMetadata;
    }

    public Object getMethodMetadata(){
        return methodMetadata;
    }

    public void setMethodMetadata(Object methodMetadata){
        this.methodMetadata = methodMetadata;
    }
}
