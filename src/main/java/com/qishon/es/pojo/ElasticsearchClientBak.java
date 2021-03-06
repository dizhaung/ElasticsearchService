package com.qishon.es.pojo;/**
 * Created by shuting.wu on 2017/3/13.
 */

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * @author shuting.wu
 * @date 2017-03-2017/3/13 17:39
 **/
public class ElasticsearchClientBak {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchClientBak.class);
    private TransportClient client = null;
    private Settings settings = null;

    private String hostUrl;
    private int port;
    private String clusterName;

    /**
     * @Descrption 非集群连接
     * @param hostUrl
     * @param port
     * @return
     * @author shuting.wu
     * @date 2017/3/13
    **/
    public ElasticsearchClientBak(String hostUrl, int port) {
        this.hostUrl = hostUrl;
        this.port = port;
        //this.settings = ImmutableSettings.settingsBuilder().build();
        this.settings = Settings.EMPTY;
    }

    /**
     *@Descrption 集群连接,根据集群名称自动搜索同一网段集群索引
     * @param hostUrl
     * @param port
     * @param clusterName
     *@return
     *@author shuting.wu
     *@date 2017/3/13
     */
    public ElasticsearchClientBak(String hostUrl, int port, String clusterName) {
        this.hostUrl = hostUrl;
        this.port = port;
        this.clusterName = clusterName;
        this.settings = Settings.builder()
                .put("clusterName",this.clusterName) //集群名称
                .put("client.transport.sniff", true) //自动嗅探，获取集群所有节点
                .build();
    }

    /**
    *@Descrption 获取 TransportClient
    *@param
    *@return
    *@author shuting.wu
    *@date 2017/3/13
    **/
   public Client getTransportClient(){
        try{
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostUrl), port));
        }catch (Exception e) {
            LOGGER.error("elasticsearch","connet es fail",e);
        }
        return client;
    }

    /**
     * 关闭 TransportClient
     */
    public void closeTransportClient(){
        if(null != client) {
            try{
                client.close();
            }catch (Exception e) {
                LOGGER.error("elasticsearch","closeEsFail",e);
            }
        }
    }

}
