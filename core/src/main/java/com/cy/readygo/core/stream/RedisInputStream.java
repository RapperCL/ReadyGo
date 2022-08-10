package com.cy.readygo.core.stream;



import com.cy.readygo.core.exception.RedisXConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RedisInputStream extends FilterInputStream {
    protected final byte[] buf;
    protected int count;
    protected int limit;

    public RedisInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        } else {
            this.buf = new byte[size];
        }
    }

    public RedisInputStream(InputStream in) {
        this(in, 8192);
    }

    public byte readByte() throws RedisXConnectionException {
        this.ensureFill();
        return this.buf[this.count++];
    }

    public String readLine() throws RedisXConnectionException {
        StringBuilder sb = new StringBuilder();

        while(true) {
            while(true) {
                this.ensureFill();
                byte b = this.buf[this.count++];
                if (b == 13) {
                    this.ensureFill();
                    byte c = this.buf[this.count++];
                    if (c == 10) {
                        String reply = sb.toString();
                        if (reply.length() == 0) {
                            throw new RedisXConnectionException("It seems like server has closed the connection.");
                        }

                        return reply;
                    }

                    sb.append((char)b);
                    sb.append((char)c);
                } else {
                    sb.append((char)b);
                }
            }
        }
    }

    public byte[] readLineBytes() throws RedisXConnectionException {
        this.ensureFill();
        int pos = this.count;
        byte[] buf = this.buf;

        while(pos != this.limit) {
            if (buf[pos++] == 13) {
                if (pos == this.limit) {
                    return this.readLineBytesSlowly();
                }

                if (buf[pos++] == 10) {
                    int N = pos - this.count - 2;
                    byte[] line = new byte[N];
                    System.arraycopy(buf, this.count, line, 0, N);
                    this.count = pos;
                    return line;
                }
            }
        }

        return this.readLineBytesSlowly();
    }

    private byte[] readLineBytesSlowly() throws RedisXConnectionException {
        ByteArrayOutputStream bout = null;

        while(true) {
            while(true) {
                this.ensureFill();
                byte b = this.buf[this.count++];
                if (b == 13) {
                    this.ensureFill();
                    byte c = this.buf[this.count++];
                    if (c == 10) {
                        return bout == null ? new byte[0] : bout.toByteArray();
                    }

                    if (bout == null) {
                        bout = new ByteArrayOutputStream(16);
                    }

                    bout.write(b);
                    bout.write(c);
                } else {
                    if (bout == null) {
                        bout = new ByteArrayOutputStream(16);
                    }

                    bout.write(b);
                }
            }
        }
    }

    public int readIntCrLf()  {
        return (int)this.readLongCrLf();
    }
    // 解析出当前结果中有多少个参数 $
    public long readLongCrLf()  {
        byte[] buf = this.buf;
        this.ensureFill();
        // 此时的count = 1， 返回- 代表失败
        boolean isNeg = buf[this.count] == 45;
        if (isNeg) {
            ++this.count;
        }

        long value = 0L;

        while(true) {
            this.ensureFill();
            // count =1
            int b = buf[this.count++];
            if (b == 13) {
                this.ensureFill();
                if (buf[this.count++] != 10) {
                    throw new RedisXConnectionException("Unexpected character!");
                } else {
                    return isNeg ? -value : value;
                }
            }
            // 0 的二进制是48，于是要用对应的值-48 = 参数个数
            // *2
            value = value * 10L + (long)b - 48L;
        }
    }

    public int read(byte[] b, int off, int len)  {
        this.ensureFill();
        int length = Math.min(this.limit - this.count, len);
        System.arraycopy(this.buf, this.count, b, off, length);
        this.count += length;
        return length;
    }

    private void ensureFill() throws RedisXConnectionException {
        if (this.count >= this.limit) {
            try {
                this.limit = this.in.read(this.buf);
                this.count = 0;
                if (this.limit == -1) {
                    throw new IOException("Unexpected end of stream.");
                }
            } catch (IOException var2) {
                throw new RedisXConnectionException(var2);
            }
        }

    }
}
