package com.example.yangj.wordmangementandroid.common;

import java.io.Serializable;

/**
 * Created by yangjiajia on 2018/6/19.
 */
public class OssTokenInfo implements Serializable {

    /**
     * status : 200
     * AccessKeyId :
     * AccessKeySecret :
     * SecurityToken :
     * Expiration :
     * endpoint : oss-cn-beijing.aliyuncs.com
     * bucket :
     * cdnEndpoint :
     */

    private String status;
    private String AccessKeyId;
    private String AccessKeySecret;
    private String SecurityToken;
    private String Expiration;
    private String endpoint;
    private String bucket;
    private String cdnEndpoint;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setAccessKeyId(String AccessKeyId) {
        this.AccessKeyId = AccessKeyId;
    }

    public String getAccessKeySecret() {
        return AccessKeySecret;
    }

    public void setAccessKeySecret(String AccessKeySecret) {
        this.AccessKeySecret = AccessKeySecret;
    }

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String SecurityToken) {
        this.SecurityToken = SecurityToken;
    }

    public String getExpiration() {
        return Expiration;
    }

    public void setExpiration(String Expiration) {
        this.Expiration = Expiration;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getCdnEndpoint() {
        return cdnEndpoint;
    }

    public void setCdnEndpoint(String cdnEndpoint) {
        this.cdnEndpoint = cdnEndpoint;
    }

    @Override
    public String toString() {
        return "OssTokenInfo{" +
                "status='" + status + '\'' +
                ", AccessKeyId='" + AccessKeyId + '\'' +
                ", AccessKeySecret='" + AccessKeySecret + '\'' +
                ", SecurityToken='" + SecurityToken + '\'' +
                ", Expiration='" + Expiration + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", bucket='" + bucket + '\'' +
                ", cdnEndpoint='" + cdnEndpoint + '\'' +
                '}';
    }
}
