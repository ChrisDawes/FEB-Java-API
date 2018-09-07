package com.hcl.feb.api;

/**
 * Custom exception object for use with the FEB REST API.
 * 
 * @author ChristopherDawes
 *
 */
public class FEBAPIException extends Exception {
	 
   private static final long serialVersionUID = -6373599471361606136L;

   public FEBAPIException(String pMessage)
   {
       super(pMessage);
   }

   public FEBAPIException(Throwable pCause)
   {
       super(pCause);
   }

   public FEBAPIException(String pMessage, Throwable pCause)
   {
       super(pMessage, pCause);
   }
}
