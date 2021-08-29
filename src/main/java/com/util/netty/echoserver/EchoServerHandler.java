package com.util.netty.echoserver;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listing 2.1 EchoServerHandler
 * <p>
 * 1.channelRead ()——对于每个传入的消息都要调用;
 * 2.channelReadComplete()——通知ChannelInboundHandler最后一次对channel-Read()的调用是当前批量读取中的最后一条消息;
 * 3.exceptionCaught ()——在读取操作期间，有异常抛出时会调用。
 * <p>
 * ===收发消息只能传递ByteBuf对象===
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Slf4j
@Sharable//标示一个ChanneHandler可以被多个Channel安全地共享
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    public static ConcurrentHashMap<Channel, String> concurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf in = (ByteBuf) msg;
        byte[] bytes = ByteBufUtil.getBytes(in);

        //FileUtils.writeByteArrayToFile(new File("C:\\Users\\12733\\Desktop\\a.jpg"), bytes, true);

        log.info("服务器完毕");
        /*ByteBuf in = (ByteBuf) msg;
        System.out.println(
                "服务器收到消息: " + in.toString(CharsetUtil.UTF_8)
                        + "  接收时间 " + LocalTime.now());
        concurrentHashMap.put(ctx.channel(), in.toString(CharsetUtil.UTF_8));
        ctx.writeAndFlush(in);*///将接收到的消息写给发送者,而不冲刷出站消息
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("读取完成");
//        ctx.writeAndFlush(
//                Unpooled.copiedBuffer("服务器接收完成，发送时间: " + LocalTime.now(),
//                        CharsetUtil.UTF_8)
//        );

        log.info("当前线程池: " + JSON.toJSONString(concurrentHashMap));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();//关闭Channel
    }
}
