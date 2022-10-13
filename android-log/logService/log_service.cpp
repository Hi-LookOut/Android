#include <iostream>
#include <unistd.h>
#include <string.h>

#include "include/LogService.h"

using namespace std;


LogService::LogService():log_socket_head(){}



bool LogService::onDataAvailable(log_message* message){
 
   printf("LogService onDataAvailable()\n");
   char buffer[2048];
   struct iovec iov = { buffer, sizeof(buffer) };  
   alignas(4) char control[CMSG_SPACE(sizeof(struct ucred))];
   struct msghdr hdr = {
        nullptr, 0, &iov,1, control, sizeof(control), 0,
   };

   ssize_t n = recvmsg(mSockId, &hdr, 0);
   cout<< "LogService recvmsg.size =" << n <<endl;

   if (n < (ssize_t)sizeof(android_log_header_t)) {
      return false;
   }

   buffer[n] = 0;

   android_log_header_t* header =
              reinterpret_cast<android_log_header_t*>(buffer);
   
   int logId = static_cast<int>(header->id);
   int headSize = sizeof(android_log_header_t);

   message->name = "log";

   // 使用深拷贝
   char* msg = ((char*)buffer) + headSize;
   message->msg = (char*)malloc(n-headSize);
   memcpy(message->msg, msg, strlen(msg));
   
   android_log_header_t* newHeader = (android_log_header_t*)malloc(sizeof(android_log_header_t*));
   newHeader->id = header->id;
   newHeader->realtime = header->realtime;
   newHeader->tid = header->tid;
   message->head = newHeader;
   
   // cout<<"message.name = "<<message->name<<endl;
   // cout<<"message.id = "<<message->head->id<<endl;
   // cout<<"message.msg = "<<message->msg<<endl;

   return true;  
}