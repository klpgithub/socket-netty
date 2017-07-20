package test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// ((ByteBuf)msg).release();
		try {
			// do something msg
			ByteBuf buf = (ByteBuf) msg;
			byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			String result = new String(data, "UTF-8");
			System.out.println("server ：　" + result);

			// 返回给客户端的数据
			String response = "我是反馈的信息";
			ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
			f.addListener(ChannelFutureListener.CLOSE);// 添加监听 ，客户端接到到数据，断开连接

		} finally {
			// 有write 就不用 释放 write时默认释放
			// ReferenceCountUtil.release(msg);

		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
