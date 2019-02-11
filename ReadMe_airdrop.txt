1、编译
    得到Air_drop.jar

2、配置文件
    config-air.conf
  需要配置
    fullnode.ip.list    节点IP地址
    address             空投的转出地址
    privateKey          空投的转出私钥,在线的不需要提供
    assertId            空投的通证ID
    TRX_MIN             至少拥有TRX数量(sun)才参与空投
    TRX_NUM、BTT_NUM	    拥有TRX_NUM sun 可以获得 BTT_NUM 最小单位个 BTT
    timestamp           交易的统一时间戳，超时时间在此基础上+24小时，注意设置本时区当前时间
    SEND_START_LINE     广播起始交易条数
    SEND_LINE_NUMS      广播交易条数，用于多个进程同时广播

3、jar包和配置文件在线、离线各1份，在线的配置文件不提供私钥

4、准备输入文件
   account.csv		    空投对象地址
   blacklist.txt	    黑名单地址，不空投

5、空投流程
   5.1 在线创建交易 java -jar Air_drop.jar create
   5.2 将生成的transaction.txt拷贝到离线环境
   5.3 离线签名交易 java -jar Air_drop.jar sign
   5.4 将transactionSigned.txt拷贝到在线环境
   5.3 在线广播交易 java -jar Air_drop.jar send
   5.4 在线查询交易 java -jar Air_drop.jar query