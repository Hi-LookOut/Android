#include <stdatomic.h>

typedef struct __attribute__((__packed__)) {
  int id;
  int tid;
  int realtime;
} android_log_header_t;

struct android_log_transport_write {
    
     const char* name;
     atomic_int sock;
     int (*available)(int logId); /* Does not cause resources to be taken */
     int (*open)();   /* can be called multiple times, reusing current resources */
     void (*close)(); /* free up resources */
      /* write log to transport, returns number of bytes propagated, or -errno */
     int (*write)(struct iovec* vec,size_t nr);
};

