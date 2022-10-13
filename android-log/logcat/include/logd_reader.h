#include <stdatomic.h>


struct android_log_transport_read {
     const char* name;
     atomic_int sock;
     int (*available)(int logId); /* Does not cause resources to be taken */
     int (*open)();   /* can be called multiple times, reusing current resources */
     void (*close)(); /* free up resources */
      /* write log to transport, returns number of bytes propagated, or -errno */
     int (*read)(char* buffer);
};
