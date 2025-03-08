package org.newdawn.slick.util;

public interface LogSystem {

   /**
    * Log an error
    *
    * @param message The message describing the error
    * @param e The exception causing the error
    */
   void error(String message, Throwable e);

   /**
    * Log an error
    *
    * @param e The exception causing the error
    */
   void error(Throwable e);

   /**
    * Log an error
    *
    * @param message The message describing the error
    */
   void error(String message);

   /**
    * Log a warning
    *
    * @param message The message describing the warning
    */
   void warn(String message);

   /**
    * Log a warning
    *
    * @param message The message describing the warning
    * @param e The cause of the warning
    */
   void warn(String message, Throwable e);

   /**
    * Log an information message
    *
    * @param message The message describing the infomation
    */
   void info(String message);

   /**
    * Log a debug message
    *
    * @param message The message describing the debug
    */
   void debug(String message);
}
