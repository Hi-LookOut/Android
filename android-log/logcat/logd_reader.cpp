#include <iostream>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/uio.h>
#include <time.h>
#include "include/logd_reader.h"
using namespace std;



static int logdAvailable(int LogId);
static int logdOpen();
static void logdClose();
static int logdRead(char* buffer);


struct android_log_transport_read logdLoggerRead = {
   .name = "logd",
   .sock = -EBADF,
   .available = logdAvailable,
   .open = logdOpen,
   .close = logdClose,
   .read = logdRead,
};

int testInt=100;

static int logdAvailable(int LogId){
   
   // 如果sock 小于0，表示有问题
   if (atomic_load(&logdLoggerRead.sock) < 0) {
      // 如果该文件可以使用，可以补救
      if (access("../socket/logdr", W_OK) == 0) {
         return 0;
      }
      return -EBADF;
   }

 

   return 1;
}


static int logdOpen(){
     
      int ret = 0;
      
      // PF_UNIX 和 AF_UNIX一样，UNIX域协议
      // SOCK_DGRAM 数据包，即UDP 与之对应的是SOCK_STREAM 数据流，TCP
      // SOCK_RDM：表示想使用原始网络通信（如当domain参数设置为PF_INET 时就表示直接使用TCP/IP协议族中的ip协议）
      // SOCK_NONBLOCK：用于将socket函数返回的文件描述符指定为非阻塞
      // SOCK_CLOEXEC：一旦进程exec执行新程序时，自动关闭socket返回的套接字文件描述符

      int i =  atomic_load(&logdLoggerRead.sock);
      if (i<0)
      {
         // 表示还未使用过
         int sock = socket(PF_UNIX, SOCK_SEQPACKET | SOCK_CLOEXEC, 0);
         if (sock<0){
            perror("socket create failed");
            ret = -errno;
         }else{
            struct sockaddr_un un;
            memset(&un, 0, sizeof(struct sockaddr_un));
            un.sun_family = AF_UNIX;
            strcpy(un.sun_path, "../socket/logdr");
            
            if (connect(sock, (struct sockaddr*)&un, sizeof(struct sockaddr_un)) < 0) {
               perror("socket connect failed");
               ret = -errno;
               printf("errno=%d\n",ret);
               i = atomic_exchange(&logdLoggerRead.sock, ret);   
               
               close(sock);
            }else{
               cout<<"socket succeeded"<<endl;
               
               // 记录 sock
               ret = atomic_exchange(&logdLoggerRead.sock, sock);
               if ((ret >= 0) && (ret != sock)) {
                  close(ret);
               }
               ret =0; 
            }
         }
      }
      
     
   
   return ret;
}



static void logdClose(){
   int sock = atomic_exchange(&logdLoggerRead.sock, -EBADF);
   if (sock >= 0) {
     close(sock);
   }
} 



static int logdRead(char* buffer) {
   int ret = -EBADF;
   int sock = atomic_load(&logdLoggerRead.sock);
 
   if (sock < 0){
     perror("logdWrite  failed");
     ret = sock;
   }else{
      ret = write(sock, buffer, sizeof(buffer));
   } 

   int fd = ret;
   if (ret >0){
      while (true)
      {
         char data[2048];
         ret = read(sock, data, sizeof(data));

         if (ret>0)
         {
            printf("******************************\n"); 
            printf("logcat read log successed\n"); 
            printf("log content: %s\n",data);    
            printf("******************************\n"); 
            break;
         }
         
      }
   }
   
   close(sock);
   return ret;
}