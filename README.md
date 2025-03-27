# UncaughtExceptionHandler异常捕获，并存储到本地目录
项目反馈使用过程中奔溃退出了，但是却没有在第三方SDK收集到。所以考虑使用本地收集异常的方式，当产生异常时，可以把  
记录保存到磁盘中的位置，方便开发取出日志直接定位问题。  
android 使用Thread.UncaughtExceptionHandler进行全局异常捕获，该接口只有一个方法需要实现：  
`void uncaughtException(Thread t, Throwable e); `
文档连接：
https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.UncaughtExceptionHandler.html
以下是官网文档对于该接口的描述  
当一个线程即将因未捕获的异常而终止时，Java虚拟机将使用thread.getUncaughtExceptionHandler（）查询该线程的  
uncaughtException处理程序，并调用该处理程序的uncaughtException方法，将线程和异常作为参数传递。如果一个  
线程没有显式设置其UncaughtExceptionHandler，则其ThreadGroup对象充当其Uncaught ExceptionHandler。如果  
ThreadGroup对象对处理异常没有特殊要求，它可以将调用转发给默认的未捕获异常处理程序。  

# 使用UncaughtExceptionHandler捕获异常  
1.自定义UncaughtExceptionHandler，实现UncaughtExceptionHandler接口  
2.记录系统默认的UncaughtExceptionHandler，通过Thread.getDefaultUncaughtExceptionHandler()获取默认的异常处理器  
3.在uncaughtException()方法中处理异常，这里只需要保存到磁盘中的文件  
4.如果自定义处理器没有处理的情况才继续使用默认的处理  
5.产生异常保存文件路劲在外部存储目录\Android\data\包名\\files\exception目录下  

# 参考文档
多线程的未捕获异常类 UncaughtExceptionHandler 的使用  
https://www.cnblogs.com/javazhizhe/p/17453741.html