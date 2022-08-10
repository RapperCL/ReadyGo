package com.cy.readygo.core.RPCCommand;


import com.cy.readygo.core.encode.SafeEncoder;
import com.cy.readygo.core.exception.RedisXConnectionException;
import com.cy.readygo.core.exception.RedisXDataException;
import com.cy.readygo.core.stream.RedisInputStream;
import com.cy.readygo.core.stream.RedisOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RPCProtocol {
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_SENTINEL_PORT = 26379;
    public static final int DEFAULT_TIMEOUT = 2000;
    public static final int DEFAULT_DATABASE = 0;
    public static final String CHARSET = "UTF-8";
    public static final byte DOLLAR_BYTE = 36;
    public static final byte ASTERISK_BYTE = 42;
    public static final byte PLUS_BYTE = 43;
    public static final byte MINUS_BYTE = 45;
    public static final byte COLON_BYTE = 58;
    public static final byte[] BYTES_TRUE = toByteArray(1);
    public static final byte[] BYTES_FALSE = toByteArray(0);
    public static final byte[] BYTES_TILDE = SafeEncoder.encode("~");
    public static final byte[] POSITIVE_INFINITY_BYTES = "+inf".getBytes();
    public static final byte[] NEGATIVE_INFINITY_BYTES = "-inf".getBytes();

    public static final byte[] toByteArray(int value) {
        return SafeEncoder.encode(String.valueOf(value));
    }

    public static void sendCommand(RedisOutputStream os, RPCProtocolCommand command, byte[]... args){
        sendCommand(os, command.getRaw(), args);
    }

    /**
     * 基于redis协议，对发送数据进行编码
     * @param os
     * @param command
     * @param args
     */
    private static void sendCommand(RedisOutputStream os, byte[] command, byte[]... args){
        try{
            //*
            os.write((byte)42);
            // 参数个数
            os.writeIntCrLf(args.length + 1);
            // $ 参数长度
            os.write((byte)36);
            // 写入长度，并写入\r\n 换行符
            os.writeIntCrLf(command.length);
            //写入命令
            os.write(command);
            // 写入换行符号
            os.writeCrLf();

            byte[][] var3 = args;
            int var4 = args.length;
            // 写入对应数据 $长度 换行符 数据
            for(int var5 = 0; var5 < var4; ++var5){
                byte[] arg = var3[var5];
                // $
                os.write((byte)36);
                // 参数长度 & 换行符号
                os.writeIntCrLf(arg.length);
                // 数据
                os.write(arg);
                os.writeCrLf();
            }

        } catch (IOException var7){
            throw new RedisXConnectionException(var7);
        }
    }

    /**
     * 处理响应
     * @param rs
     * @return
     */
    private static Object process(RedisInputStream rs){
        byte b = rs.readByte();
        switch (b){
                // $
            case 36:
                return processBulkReply(rs);
                // *
            case 42:
                return processMultiBulkReply(rs);
                //+
            case 43:
                return processStatusCodeReply(rs);
                // -
            case 45:
                processError(rs);
                return null;
                //:
            case 58:
                return processInteger(rs);
            default:
                throw new RedisXConnectionException("Unknow reply: "+ (char) b);
        }
    }
    private static void processError(RedisInputStream rs) {
        String message = rs.readLine();
        throw new RedisXDataException(message);
    }

    private static byte[] processStatusCodeReply(RedisInputStream rs) {
        return rs.readLineBytes();
    }

    private static byte[] processBulkReply(RedisInputStream rs){
        int len = rs.readIntCrLf();
        if(len == -1){
            return null;
        } else{
            byte[] read = new byte[len];

            int size ;
            for(int offset = 0; offset < len; offset += size){
                size = rs.read(read, offset, len - offset);
                if( size == - 1){
                    throw new RedisXConnectionException("It seems like server has closed the connection.");
                }
            }

            rs.readByte();
            rs.readByte();
            return read;
        }
    }

    private static Long processInteger(RedisInputStream rs){
        return rs.readLongCrLf();
    }

    private static List<Object> processMultiBulkReply(RedisInputStream rs){
        int num = rs.readIntCrLf();
        if(num == -1){
            return null;
        }
        List<Object> ret = new ArrayList<>(num);

        for(int i = 0;i<num ;i++){
            try{
                ret.add(process(rs));
            } catch (RedisXDataException var5){
                ret.add(var5);
            }
        }
        return ret;
    }


    public static Object read(RedisInputStream rs) {
        return process(rs);
    }

    public static String readErrorLineIfPossible(RedisInputStream is) {
        byte b = is.readByte();
        return b != 45 ? null : is.readLine();
    }

    public static final byte[] toByteArray(boolean value){
        return value ? BYTES_TRUE : BYTES_FALSE;
    }

    public static final byte[] toByteArray(double value){
        if(value == 1.00 / 0.0){
            return POSITIVE_INFINITY_BYTES;
        } else{
            return value == -1.00 /0.0 ? NEGATIVE_INFINITY_BYTES : SafeEncoder.encode(String.valueOf(value));
        }
    }

    public static enum Command implements RPCProtocolCommand{
        PING,
        RPC,
        AUTH,
        SET,
        GET,
        QUIT,
        EXISTS,
        DEL,
        UNLINK,
        TYPE,
        FLUSHDB,
        KEYS,
        RANDOMKEY,
        RENAME,
        RENAMENX,
        RENAMEX,
        DBSIZE,
        EXPIRE,
        EXPIREAT,
        TTL,
        SELECT,
        MOVE,
        FLUSHALL,
        GETSET,
        MGET,
        SETNX,
        SETEX,
        MSET,
        MSETNX,
        DECRBY,
        DECR,
        INCRBY,
        INCR,
        APPEND,
        SUBSTR,
        MULTI,
        DISCARD,
        SAVE,
        BGSAVE,
        BGREWRITEAOF,
        LASTSAVE,
        SHUTDOWN,
        INFO,
        MONITOR,
        SLAVEOF,
        CONFIG,
        STRLEN,
        SYNC,
        LPUSHX,
        PERSIST,
        RPUSHX,
        ECHO,
        LINSERT,
        DEBUG,
        BRPOPLPUSH,
        OBJECT,
        BITCOUNT,
        BITOP,
        SENTINEL,
        DUMP,
        RESTORE,
        PEXPIRE,
        PEXPIREAT,
        PTTL,
        INCRBYFLOAT,
        PSETEX,
        CLIENT,
        TIME,
        MIGRATE,
        HINCRBYFLOAT,
        SCAN,
        HSCAN,
        SSCAN,
        ZSCAN,
        WAIT,
        CLUSTER,
        ASKING,
        PFADD,
        MODULE,
        BITFIELD,
        HSTRLEN,
        TOUCH,
        SWAPDB,
        MEMORY,
        XGROUP,
        XREADGROUP,
        XPENDING,
        XCLAIM,
        ACL,
        XINFO,
        BITFIELD_RO;

        private final byte[] raw = SafeEncoder.encode(this.name());

        private Command() {
        }

        public byte[] getRaw() {
            return this.raw;
        }
    }


    public static enum Keyword {
        AGGREGATE,
        ALPHA,
        ASC,
        BY,
        DESC,
        GET,
        LIMIT,
        MESSAGE,
        NO,
        NOSORT,
        PMESSAGE,
        PSUBSCRIBE,
        PUNSUBSCRIBE,
        OK,
        ONE,
        QUEUED,
        SET,
        STORE,
        SUBSCRIBE,
        UNSUBSCRIBE,
        WEIGHTS,
        WITHSCORES,
        RESETSTAT,
        REWRITE,
        RESET,
        FLUSH,
        EXISTS,
        LOAD,
        KILL,
        LEN,
        REFCOUNT,
        ENCODING,
        IDLETIME,
        GETNAME,
        SETNAME,
        LIST,
        MATCH,
        COUNT,
        PING,
        PONG,
        UNLOAD,
        REPLACE,
        KEYS,
        PAUSE,
        DOCTOR,
        BLOCK,
        NOACK,
        STREAMS,
        KEY,
        CREATE,
        MKSTREAM,
        SETID,
        DESTROY,
        DELCONSUMER,
        MAXLEN,
        GROUP,
        IDLE,
        TIME,
        RETRYCOUNT,
        FORCE,
        STREAM,
        GROUPS,
        CONSUMERS,
        HELP,
        FREQ,
        SETUSER,
        GETUSER,
        DELUSER,
        WHOAMI,
        CAT,
        GENPASS,
        USERS;

        public final byte[] raw;

        private Keyword() {
            this.raw = SafeEncoder.encode(this.name().toLowerCase(Locale.ENGLISH));
        }
    }
}
