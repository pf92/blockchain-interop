package com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a json rpc request.
 */
public class JsonRpcRequest {

    private String method;
    private List<String> params = new ArrayList<>();
    private int id;
    private String jsonrpc;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    @Override
    public String toString() {
        return "JsonRpcRequest{" +
                "method='" + method + '\'' +
                ", params=" + params +
                ", id=" + id +
                ", jsonrpc='" + jsonrpc + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcRequest)) return false;
        JsonRpcRequest that = (JsonRpcRequest) o;
        return id == that.id &&
                Objects.equals(method, that.method) &&
                Objects.equals(params, that.params) &&
                Objects.equals(jsonrpc, that.jsonrpc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, params, id, jsonrpc);
    }

}
