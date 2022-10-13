#include <iostream>
#include <string.h>
#include <sys/uio.h>
#include <unistd.h>
#include "include/LogReader.h"
using namespace std;


LogReader::LogReader():log_socket_head(){}


bool LogReader::onDataAvailable(log_message* message){

   printf("LogReader onDataAvailable()\n");

   int fd = accept4(mSockId,nullptr,nullptr,SOCK_CLOEXEC);
            
   if(fd<0){
      printf("LogReader accept4 failed (%s)\n", strerror(errno));
      return false;
   }

   char buffer[2048];
   memset(buffer, 0, 2048);
   int ret = read(fd, buffer, sizeof(buffer));

   if (ret<0){
      printf("LogReader read falied(%s)\n", strerror(errno));
      return false;
   }
   
   printf("LogReader read buffer = %s\n",buffer);
   char* data = message->msg;
   if (!data||strlen(data)<=0){
      data = "log is null";
   }
   cout<<"message.msg = "<<data<<endl;
   printf("LogReader data= %s\n",data);
   ret = write(fd, data, strlen(data)+1);

   if (ret<0){
      printf("LogReader write falied(%s)\n", strerror(errno));
   }
   return true;
   
}





