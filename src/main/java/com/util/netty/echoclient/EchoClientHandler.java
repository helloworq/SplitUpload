package com.util.netty.echoclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Listing 2.3 ChannelHandler for the client
 * <p>
 * channelActive ()——在到服务器的连接已经建立之后将被调用;
 * channelRead0 () ——当从服务器接收到一条消息时被调用;
 * exceptionCaught ()——在处理过程中引发异常时被调用。
 * <p>
 * ===收发消息只能传递ByteBuf对象===
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Slf4j
@Sharable
public class EchoClientHandler
        extends SimpleChannelInboundHandler<ByteBuf> {

    //===收发消息只能传递ByteBuf对象===

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\12733\\Desktop\\ttt.jpg"));

        ctx.writeAndFlush(Unpooled.copiedBuffer(bytes));
        log.info("完毕");
        /*ctx.writeAndFlush(
                Unpooled.copiedBuffer("客户端启动初始化，发送时间: " + LocalTime.now(),
                        CharsetUtil.UTF_8)
        );*///当被通知Channel是活跃的时候,发送一条消息
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println("客户端收到消息: " + in.toString(CharsetUtil.UTF_8));
        //记录已接受消息的转储
        System.out.println("请输入你的消息");
        Scanner input = new Scanner(System.in);
        ctx.writeAndFlush(
                Unpooled.copiedBuffer(input.next() + "  发送时间: " + LocalTime.now(),
                        CharsetUtil.UTF_8)
        );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();//关闭Channel
    }

}
