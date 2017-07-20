package test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

	public static void main(String[] args) throws Exception {

		// 第一个线程组是用于接受client端连接的
		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
		// 第二个线程组是用于实际的业务处理操作的
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		// 3.创建一个辅助类ServerBootStrap，对serve进行一系列的配置
		ServerBootstrap b = new ServerBootstrap();
		// 把每个工作线程加入进来
		b.group(bossGroup, workerGroup)
				// 指定使用NioServerSocketChannel这种通道
				.channel(NioServerSocketChannel.class) // (3)
				// 一定要用childHandler去绑定具体的事件处理器
				.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ServerHandler());
					}
					/**
					 * 服务器端TCP内核模块维护有两个队列，我们称之为A B
					 * 客户端向服务器端connect的时候，会发送带有SYN标志的包(第一次握手)
					 * 服务器端收到客户端发来的SYN时，向客户端发送SYN ACK确认(第二次握手)
					 * 此时TCP内核模块把客户端连接加入到A队列中，然后服务端收到客户端发来的ACK时，(第三次握手)
					 * TCP内核模块把客户端连接从A队列移到B队列，连接完成， 应用程序的accept会返回
					 * 也就是说accept从B队列中取出完成三次握手的连接 A队列和B队列的长度之和
					 * 是backlog，当A，B队列之和大于backlog时，新连接将会被TCP内核拒绝
					 * 所以，如果backlog过小可能会出现accept速度跟不上，A,B队列满了，导致新的客户端连接不上
					 * 要注意：backlog影响的是还没有被accept取出的连接，对程序支持的连接数没有影响
					 */
				})
				// 设置tcp连接的缓冲区
				// .option(ChannelOption.SO_BACKLOG, 128) // (5)
				// 保持连接
				.childOption(ChannelOption.SO_KEEPALIVE, true);
		// 绑定指定端口进行监听
		ChannelFuture f = b.bind(8765).sync();

		f.channel().closeFuture().sync();//阻塞  

		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();

	}

}
